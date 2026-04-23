package com.example.constructioncalculator

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import kotlin.math.roundToInt
import kotlin.math.sqrt

class FloorPlanView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val GRID_SIZE = 30f
    private val MAJOR_EVERY = 5

    var currentTool: Tool = Tool.DRAW_LINE
    var pendingShapeType: String = "door1"
    var onShapeAdded: (() -> Unit)? = null
    private var drawColor: Int = Color.BLACK
    private var showGrid: Boolean = true

    private val shapes = mutableListOf<FloorPlanShape>()
    private val undoStack = ArrayDeque<List<FloorPlanShape>>()
    private val redoStack = ArrayDeque<List<FloorPlanShape>>()

    private var isDrawing = false
    private var startX = 0f;
    private var startY = 0f
    private var currentX = 0f;
    private var currentY = 0f

    private var selectedShape: FloorPlanShape? = null
    private var dragOffsetX = 0f;
    private var dragOffsetY = 0f
    private var isDragging = false

    private var scaleFactor = 1f
    private var translateX = 0f;
    private var translateY = 0f
    private var lastTouchX = 0f;
    private var lastTouchY = 0f
    private var activePointerCount = 0
    private var isScaling = false

    private val gridMinorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E0E0E0"); strokeWidth = 0.7f; style = Paint.Style.STROKE
    }
    private val gridMajorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#BDBDBD"); strokeWidth = 1.2f; style = Paint.Style.STROKE
    }
    private val previewPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 6f; style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND
        pathEffect = DashPathEffect(floatArrayOf(15f, 8f), 0f); alpha = 200
    }
    private val selectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF5722"); strokeWidth = 3f; style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
    }
    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF5722"); style = Paint.Style.FILL
    }

    private val scaleDetector = ScaleGestureDetector(
        context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                isScaling = true; isDrawing = false; return true
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val newScale = (scaleFactor * detector.scaleFactor).coerceIn(0.25f, 8f)
                translateX =
                    detector.focusX - (detector.focusX - translateX) * (newScale / scaleFactor)
                translateY =
                    detector.focusY - (detector.focusY - translateY) * (newScale / scaleFactor)
                scaleFactor = newScale; invalidate(); return true
            }

            override fun onScaleEnd(detector: ScaleGestureDetector) {
                isScaling = false
            }
        })

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.WHITE)
        canvas.save()
        canvas.translate(translateX, translateY)
        canvas.scale(scaleFactor, scaleFactor)
        if (showGrid) drawGrid(canvas)
        for (shape in shapes) shape.draw(canvas)
        if (isDrawing) drawPreview(canvas)
        drawSelectionHandles(canvas)
        canvas.restore()
    }

    private fun drawGrid(canvas: Canvas) {
        val left = (-translateX / scaleFactor)
        val top = (-translateY / scaleFactor)
        val right = left + width / scaleFactor
        val bottom = top + height / scaleFactor
        val startCol = (left / GRID_SIZE).toInt() - 1
        val endCol = (right / GRID_SIZE).toInt() + 1
        val startRow = (top / GRID_SIZE).toInt() - 1
        val endRow = (bottom / GRID_SIZE).toInt() + 1
        for (col in startCol..endCol) {
            val x = col * GRID_SIZE
            canvas.drawLine(x, top, x, bottom, if (col % MAJOR_EVERY == 0) gridMajorPaint else gridMinorPaint)
        }
        for (row in startRow..endRow) {
            val y = row * GRID_SIZE
            canvas.drawLine(left, y, right, y, if (row % MAJOR_EVERY == 0) gridMajorPaint else gridMinorPaint)
        }
    }

    private fun drawPreview(canvas: Canvas) {
        previewPaint.color = drawColor
        when (currentTool) {
            Tool.DRAW_LINE -> canvas.drawLine(startX, startY, currentX, currentY, previewPaint)
            Tool.DRAW_RECT -> canvas.drawRect(
                RectF(minOf(startX,currentX), minOf(startY,currentY),
                    maxOf(startX,currentX), maxOf(startY,currentY)), previewPaint)
            Tool.ADD_DIMENSION -> canvas.drawLine(startX, startY, currentX, currentY, previewPaint)
            else -> {}
        }
    }

    private fun drawSelectionHandles(canvas: Canvas) {
        val b = selectedShape?.getBounds() ?: return
        val ex = RectF(b.left-8f, b.top-8f, b.right+8f, b.bottom+8f)
        canvas.drawRect(ex, selectionPaint)
        listOf(PointF(ex.left,ex.top), PointF(ex.right,ex.top),
            PointF(ex.left,ex.bottom), PointF(ex.right,ex.bottom))
            .forEach { canvas.drawCircle(it.x, it.y, 8f, handlePaint) }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        if (isScaling) return true

        val worldX = (event.x - translateX) / scaleFactor
        val worldY = (event.y - translateY) / scaleFactor
        val snappedX = snapToGrid(worldX)
        val snappedY = snapToGrid(worldY)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                activePointerCount = 1
                lastTouchX = event.x; lastTouchY = event.y; isDragging = false
                when (currentTool) {
                    Tool.SELECT -> {
                        val found = findShapeAt(worldX, worldY)
                        if (found != null) {
                            selectedShape = found
                            val b = found.getBounds()
                            dragOffsetX = worldX - (b?.left ?: 0f)
                            dragOffsetY = worldY - (b?.top ?: 0f)
                            isDragging = true
                        } else { selectedShape = null }
                        invalidate()
                    }
                    Tool.ADD_SHAPE -> {

                        if (pendingShapeType.isEmpty()) return true

                        saveState()

                        shapes.add(
                            createPredefinedShape(
                                pendingShapeType,
                                snappedX,
                                snappedY
                            )
                        )

                        redoStack.clear()
                        onShapeAdded?.invoke()

                        // يرجع تلقائياً للـ Select بعد الإضافة
                        currentTool = Tool.SELECT

                        invalidate()
                    }
                    else -> {
                        startX = snappedX; startY = snappedY
                        currentX = snappedX; currentY = snappedY; isDrawing = true
                    }
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> { activePointerCount = event.pointerCount; isDrawing = false }
            MotionEvent.ACTION_MOVE -> {
                if (activePointerCount >= 2) {
                    translateX += (event.x - lastTouchX) * 0.3f
                    translateY += (event.y - lastTouchY) * 0.3f
                    lastTouchX = event.x; lastTouchY = event.y; invalidate(); return true
                }
                when {
                    currentTool == Tool.SELECT && isDragging && selectedShape != null -> {
                    selectedShape!!.moveTo(worldX - dragOffsetX, worldY - dragOffsetY); invalidate()
                    }
                    isDrawing -> { currentX = snappedX; currentY = snappedY; invalidate() }
                    else -> {
                        translateX += event.x - lastTouchX; translateY += event.y - lastTouchY
                        lastTouchX = event.x; lastTouchY = event.y; invalidate()
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                activePointerCount = 0
                if (isDrawing) {
                    currentX = snappedX; currentY = snappedY; isDrawing = false
                    // إضافة زر تكبير/تصغير للشكل المحدد
                    fun scaleSelected(factor: Float) {
                        val shape = selectedShape
                        if (shape is FurnitureShape) {
                            shape.scale = (shape.scale * factor).coerceIn(0.3f, 5f)
                            invalidate()
                        }
                    }
                    if (distance(startX, startY, currentX, currentY) > 10f) {
                        saveState(); shapes.add(createShape())
                        redoStack.clear(); onShapeAdded?.invoke()
                    }
                    invalidate()
                } else { isDragging = false }
            }
            MotionEvent.ACTION_POINTER_UP -> { activePointerCount = 1 }
        }
        return true
    }

    private fun snapToGrid(v: Float) = (v / GRID_SIZE).roundToInt() * GRID_SIZE
    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float) =
        sqrt(((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1)).toDouble()).toFloat()
    private fun findShapeAt(x: Float, y: Float) = shapes.lastOrNull { it.contains(x, y) }

    private fun createShape(): FloorPlanShape = when (currentTool) {
        Tool.DRAW_LINE     -> WallShape(startX, startY, currentX, currentY, drawColor)
        Tool.DRAW_RECT     -> RoomShape(startX, startY, currentX, currentY, drawColor)
        Tool.ADD_DIMENSION -> DimensionShape(startX, startY, currentX, currentY)
        else               -> WallShape(startX, startY, currentX, currentY, drawColor)
    }

    private fun createPredefinedShape(type: String, x: Float, y: Float): FloorPlanShape {
        if (type.startsWith("label:")) {
            val text = type.removePrefix("label:")
            return LabelShape(x, y, text)
        }

        return when (type) {
            "door1"  -> DoorShape(x, y, 60f, 0)
            "door2"  -> DoorShape(x, y, 60f, 90)
            "door3"  -> DoorShape(x, y, 60f, 180)
            "door4"  -> DoorShape(x, y, 60f, 270)

            "window" -> WindowShape(x, y, 80f)
            "stairs" -> StairsShape(x, y)

            "sofa" -> FurnitureShape(
                x,
                y,
                "SOFA",
                BitmapFactory.decodeResource(resources, R.drawable.sofa)
            )

            "table" -> FurnitureShape(
                x,
                y,
                "TABLE",
                BitmapFactory.decodeResource(resources, R.drawable.table)
            )

            "kitchen" -> FurnitureShape(
                x,
                y,
                "KITCHEN",
                BitmapFactory.decodeResource(resources, R.drawable.kitchen)
            )

            "fridge" -> FurnitureShape(
                x,
                y,
                "FRIDGE",
                BitmapFactory.decodeResource(resources, R.drawable.fridge)
            )

            "car" -> FurnitureShape(
                x,
                y,
                "CAR",
                BitmapFactory.decodeResource(resources, R.drawable.car)
            )

            "wardrobe" -> FurnitureShape(
                x,
                y,
                "WARDROBE",
                BitmapFactory.decodeResource(resources, R.drawable.wardrobe)
            )

            else -> WallShape(x, y, x + 60f, y, drawColor)
        }
    }

    private fun saveState() {
        undoStack.addLast(shapes.toList())
        if (undoStack.size > 50) undoStack.removeFirst()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.addLast(shapes.toList())
            shapes.clear(); shapes.addAll(undoStack.removeLast())
            selectedShape = null; invalidate()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.addLast(shapes.toList())
            shapes.clear(); shapes.addAll(redoStack.removeLast()); invalidate()
        }
    }

    fun clearAll() {
        saveState(); shapes.clear(); selectedShape = null; redoStack.clear(); invalidate()
    }

    fun deleteSelected() {
        selectedShape?.let { saveState(); shapes.remove(it); selectedShape = null; invalidate() }
    }

    fun toggleGrid() { showGrid = !showGrid; invalidate() }
    fun setDrawColor(color: Int) { drawColor = color }
    fun getShapeCount() = shapes.size

    fun getShapesData(): String {
        val sb = StringBuilder()
        for (s in shapes) {
            when (s) {
                is WallShape      -> sb.append("WALL,${s.x1},${s.y1},${s.x2},${s.y2}\n")
                is RoomShape      -> sb.append("ROOM,${s.left},${s.top},${s.right},${s.bottom}\n")
                is DoorShape      -> sb.append("DOOR,${s.x},${s.y}\n")
                is WindowShape    -> sb.append("WINDOW,${s.x},${s.y}\n")
                is StairsShape    -> sb.append("STAIRS,${s.x},${s.y}\n")
                is DimensionShape -> sb.append("DIM,${s.x1},${s.y1},${s.x2},${s.y2}\n")
                is LabelShape     -> sb.append("LABEL,${s.x},${s.y},${s.text}\n")
            }
        }
        return sb.toString()
    }

    fun loadShapesData(data: String) {
        shapes.clear()
        for (line in data.lines()) {
            if (line.isBlank()) continue
            val p = line.split(",")
            try {
                when (p[0]) {
                    "WALL"   -> shapes.add(WallShape(p[1].toFloat(), p[2].toFloat(), p[3].toFloat(), p[4].toFloat()))
                    "ROOM"   -> shapes.add(RoomShape(p[1].toFloat(), p[2].toFloat(), p[3].toFloat(), p[4].toFloat()))
                    "DOOR"   -> shapes.add(DoorShape(p[1].toFloat(), p[2].toFloat()))
                    "WINDOW" -> shapes.add(WindowShape(p[1].toFloat(), p[2].toFloat()))
                    "STAIRS" -> shapes.add(StairsShape(p[1].toFloat(), p[2].toFloat()))
                    "DIM"    -> shapes.add(DimensionShape(p[1].toFloat(), p[2].toFloat(), p[3].toFloat(), p[4].toFloat()))
                    "LABEL"  -> shapes.add(LabelShape(p[1].toFloat(), p[2].toFloat(), p[3]))
                }
            } catch (e: Exception) { /* تخطي */ }
        }
        invalidate()
    }
    class DimensionShape(
        var x1: Float,
        var y1: Float,
        var x2: Float,
        var y2: Float
    ) : FloorPlanShape() {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLUE
            strokeWidth = 3f
            textSize = 30f
        }

        override fun draw(canvas: Canvas) {
            canvas.drawLine(x1, y1, x2, y2, paint)

            val dx = x2 - x1
            val dy = y2 - y1
            val distance = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

            val meters = distance / 30f

            val midX = (x1 + x2) / 2
            val midY = (y1 + y2) / 2

            canvas.drawText("${"%.2f".format(meters)} m", midX, midY, paint)
        }

        override fun contains(px: Float, py: Float) = false
        override fun moveTo(nx: Float, ny: Float) {}
        override fun getBounds() = RectF(x1, y1, x2, y2)
    }
    fun scaleSelected(factor: Float) {
        val shape = selectedShape
        if (shape is FurnitureShape) {
            shape.scale = (shape.scale * factor).coerceIn(0.3f, 5f)
            invalidate()
        }
    }

}

