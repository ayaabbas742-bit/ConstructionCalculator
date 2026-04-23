package com.example.constructioncalculator

import android.graphics.*
import kotlin.math.abs
import kotlin.math.sqrt


abstract class FloorPlanShape {
    abstract fun draw(canvas: Canvas)
    abstract fun getBounds(): RectF?
    abstract fun moveTo(x: Float, y: Float)
    abstract fun contains(x: Float, y: Float): Boolean
}

class WallShape(
    var x1: Float, var y1: Float,
    var x2: Float, var y2: Float,
    private val color: Int = Color.BLACK
) : FloorPlanShape() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 8f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    override fun draw(canvas: Canvas) { paint.color = color; canvas.drawLine(x1, y1, x2, y2, paint) }
    override fun getBounds() = RectF(minOf(x1,x2)-5f, minOf(y1,y2)-5f, maxOf(x1,x2)+5f, maxOf(y1,y2)+5f)
    override fun moveTo(x: Float, y: Float) {
        val dx = x - minOf(x1,x2); val dy = y - minOf(y1,y2)
        x1+=dx; y1+=dy; x2+=dx; y2+=dy
    }
    override fun contains(px: Float, py: Float): Boolean {
        val len = sqrt(((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1)).toDouble()).toFloat()
        if (len < 1f) return false
        return abs((y2-y1)*px-(x2-x1)*py+x2*y1-y2*x1)/len < 15f
    }
}

class RoomShape(
    var left: Float, var top: Float,
    var right: Float, var bottom: Float,
    private val color: Int = Color.BLACK
) : FloorPlanShape() {
    private val wallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 8f; style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F5F5F5"); style = Paint.Style.FILL
    }
    override fun draw(canvas: Canvas) {
        val rect = RectF(minOf(left,right), minOf(top,bottom), maxOf(left,right), maxOf(top,bottom))
        canvas.drawRect(rect, fillPaint)
        wallPaint.color = color
        canvas.drawRect(rect, wallPaint)
    }
    override fun getBounds() = RectF(minOf(left,right), minOf(top,bottom), maxOf(left,right), maxOf(top,bottom))
    override fun moveTo(x: Float, y: Float) {
        val w = abs(right-left); val h = abs(bottom-top)
        left=x; top=y; right=x+w; bottom=y+h
    }
    override fun contains(px: Float, py: Float) = getBounds()!!.contains(px, py)
}

class DoorShape(
    var x: Float, var y: Float,
    private val size: Float = 60f,
    private val rotation: Int = 0
) : FloorPlanShape() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#795548"); strokeWidth = 5f
        style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND
    }
    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#795548"); strokeWidth = 3f; style = Paint.Style.STROKE
    }
    override fun draw(canvas: Canvas) {
        canvas.save()
        canvas.rotate(rotation.toFloat(), x, y)
        canvas.drawLine(x, y, x+size, y, paint)
        canvas.drawArc(RectF(x, y-size, x+size*2, y+size), 180f, 90f, false, arcPaint)
        canvas.drawLine(x, y, x, y-size, arcPaint)
        canvas.restore()
    }
    override fun getBounds() = RectF(x-10f, y-size-10f, x+size*2+10f, y+10f)
    override fun moveTo(nx: Float, ny: Float) { x=nx; y=ny }
    override fun contains(px: Float, py: Float) = getBounds()!!.contains(px, py)
}

class WindowShape(
    var x: Float, var y: Float,
    private val w: Float = 80f
) : FloorPlanShape() {
    private val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1565C0"); strokeWidth = 5f; style = Paint.Style.STROKE
    }
    private val glassPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#90CAF9"); style = Paint.Style.FILL; alpha = 150
    }
    override fun draw(canvas: Canvas) {
        val rect = RectF(x, y-8f, x+w, y+8f)
        canvas.drawRect(rect, glassPaint)
        canvas.drawRect(rect, framePaint)
        canvas.drawLine(x+w/2, y-8f, x+w/2, y+8f, framePaint)
    }
    override fun getBounds() = RectF(x-5f, y-13f, x+w+5f, y+13f)
    override fun moveTo(nx: Float, ny: Float) { x=nx; y=ny }
    override fun contains(px: Float, py: Float) = getBounds()!!.contains(px, py)
}

class StairsShape(
    var x: Float, var y: Float,
    private val stepW: Float = 90f,
    private val stepH: Float = 20f,
    private val count: Int = 5
) : FloorPlanShape() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#546E7A"); strokeWidth = 3f; style = Paint.Style.STROKE
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#ECEFF1"); style = Paint.Style.FILL
    }
    override fun draw(canvas: Canvas) {
        for (i in 0 until count) {
            val sx = x + i*(stepW/count)
            val rect = RectF(sx, y, x+stepW, y+stepH*(i+1))
            canvas.drawRect(rect, fillPaint)
            canvas.drawRect(rect, paint)
        }
        val ax = x+stepW/2; val ay = y+(stepH*count)/2
        canvas.drawLine(ax, ay+15f, ax, ay-15f, paint)
        canvas.drawLine(ax-8f, ay-7f, ax, ay-15f, paint)
        canvas.drawLine(ax+8f, ay-7f, ax, ay-15f, paint)
    }
    override fun getBounds() = RectF(x-5f, y-5f, x+stepW+5f, y+stepH*count+5f)
    override fun moveTo(nx: Float, ny: Float) { x=nx; y=ny }
    override fun contains(px: Float, py: Float) = getBounds()!!.contains(px, py)
}

class DimensionShape(
    var x1: Float, var y1: Float,
    var x2: Float, var y2: Float
) : FloorPlanShape() {
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E53935"); strokeWidth = 2f; style = Paint.Style.STROKE
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E53935"); textSize = 28f
        textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD
    }
    private val bgPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
    override fun draw(canvas: Canvas) {
        canvas.drawLine(x1, y1, x2, y2, linePaint)
        val dx = x2-x1; val dy = y2-y1
        val len = sqrt((dx*dx+dy*dy).toDouble()).toFloat()
        if (len < 1f) return
        val nx = -dy/len*10f; val ny = dx/len*10f
        canvas.drawLine(x1+nx, y1+ny, x1-nx, y1-ny, linePaint)
        canvas.drawLine(x2+nx, y2+ny, x2-nx, y2-ny, linePaint)
        val cm = (len/30f*20f).toInt()
        val label = "$cm سم"
        val mx = (x1+x2)/2; val my = (y1+y2)/2
        val tw = textPaint.measureText(label)
        canvas.drawRect(mx-tw/2-4f, my-28f, mx+tw/2+4f, my+6f, bgPaint)
        canvas.drawText(label, mx, my, textPaint)
    }
    override fun getBounds() = RectF(minOf(x1,x2)-10f, minOf(y1,y2)-30f, maxOf(x1,x2)+10f, maxOf(y1,y2)+10f)
    override fun moveTo(nx: Float, ny: Float) {
        val dx = nx-minOf(x1,x2); val dy = ny-minOf(y1,y2)
        x1+=dx; y1+=dy; x2+=dx; y2+=dy
    }
    override fun contains(px: Float, py: Float) = getBounds()!!.contains(px, py)
}

class LabelShape(
    var x: Float, var y: Float,
    val text: String,
    private val color: Int = Color.parseColor("#212121")
) : FloorPlanShape() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 32f; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
    }
    override fun draw(canvas: Canvas) { paint.color = color; canvas.drawText(text, x, y, paint) }
    override fun getBounds(): RectF {
        val w = paint.measureText(text)
        return RectF(x-w/2-5f, y-35f, x+w/2+5f, y+5f)
    }
    override fun moveTo(nx: Float, ny: Float) { x=nx; y=ny }
    override fun contains(px: Float, py: Float) = getBounds()!!.contains(px, py)
}