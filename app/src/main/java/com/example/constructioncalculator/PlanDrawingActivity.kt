package com.example.constructioncalculator

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class PlanDrawingActivity : AppCompatActivity() {

    private lateinit var floorPlanView: FloorPlanView
    private lateinit var tvToolName: TextView
    private lateinit var tvShapeCount: TextView
    private lateinit var tvPlanName: TextView
    private lateinit var db: DatabaseHelper

    private var currentPlanId: Long = -1L
    private var currentPlanName: String = "New Plan"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plan_drawing)

        db            = DatabaseHelper(this)
        floorPlanView = findViewById(R.id.floorPlanView)
        tvToolName    = findViewById(R.id.tvToolName)
        tvShapeCount  = findViewById(R.id.tvShapeCount)
        tvPlanName    = findViewById(R.id.tvPlanName)

        tvPlanName.text = currentPlanName
        floorPlanView.onShapeAdded = { updateShapeCount() }

        setupShapeToolbar()
        setupBottomToolbar()
        setupTopToolbar()

        tvPlanName.setOnClickListener { renamePlan() }

        selectTool(Tool.DRAW_LINE)
        updateShapeCount()

        val planId = intent.getLongExtra("plan_id", -1L)
        if (planId != -1L) loadPlan(planId)
    }

    private fun selectTool(tool: Tool) {
        floorPlanView.currentTool = tool
        tvToolName.text = when (tool) {
            Tool.DRAW_LINE     -> "Line Tool"
            Tool.DRAW_RECT     -> "Room Tool"
            Tool.SELECT        -> "Select Tool"
            Tool.ADD_DIMENSION -> "Dimension Tool"
            Tool.ADD_SHAPE     -> "Insert Tool"
        }
    }

    private fun updateShapeCount() {
        tvShapeCount.text = "Items: ${floorPlanView.getShapeCount()}"
    }

    private fun setupTopToolbar() {
        findViewById<View>(R.id.btnBackground).setOnClickListener { floorPlanView.toggleGrid() }
        findViewById<View>(R.id.btnColor).setOnClickListener { showColorPicker() }
        findViewById<View>(R.id.btnClear).setOnClickListener { showClearDialog() }
        findViewById<View>(R.id.btnDelete).setOnClickListener {
            floorPlanView.deleteSelected()
            updateShapeCount()
        }
        findViewById<View>(R.id.btnSave).setOnClickListener { savePlanDialog() }
        findViewById<View>(R.id.btnNew).setOnClickListener { resetPlan() }
        findViewById<View>(R.id.btnPlans).setOnClickListener { showSavedPlans() }
        findViewById<View>(R.id.btnUndo).setOnClickListener {
            floorPlanView.undo()
            updateShapeCount()
        }
        findViewById<View>(R.id.btnRedo).setOnClickListener {
            floorPlanView.redo()
            updateShapeCount()
        }
        findViewById<View>(R.id.btnZoomIn).setOnClickListener { floorPlanView.scaleSelected(1.2f) }
        findViewById<View>(R.id.btnZoomOut).setOnClickListener { floorPlanView.scaleSelected(0.8f) }
    }

    private fun setupShapeToolbar() {
        val map = mapOf(
            R.id.btnShapeDoor1    to "door1",
            R.id.btnShapeDoor2    to "door2",
            R.id.btnShapeDoor3    to "door3",
            R.id.btnShapeDoor4    to "door4",
            R.id.btnShapeWindow   to "window",
            R.id.btnShapeStairs   to "stairs",
            R.id.btnShapeSofa     to "sofa",
            R.id.btnShapeTable    to "table",
            R.id.btnShapeKitchen  to "kitchen",
            R.id.btnShapeFridge   to "fridge",
            R.id.btnShapeCar      to "car",
            R.id.btnShapewardrobe to "wardrobe"
        )
        map.forEach { (id, type) ->
            findViewById<View>(id).setOnClickListener {
                floorPlanView.pendingShapeType = type
                selectTool(Tool.ADD_SHAPE)
                tvToolName.text = "Place: $type"
            }
        }
        findViewById<View>(R.id.btnAddLabel).setOnClickListener { showLabelDialog() }
    }

    private fun setupBottomToolbar() {
        findViewById<View>(R.id.btnToolDraw).setOnClickListener { selectTool(Tool.DRAW_LINE) }
        findViewById<View>(R.id.btnToolRect).setOnClickListener { selectTool(Tool.DRAW_RECT) }
        findViewById<View>(R.id.btnToolSelect).setOnClickListener { selectTool(Tool.SELECT) }
        findViewById<View>(R.id.btnToolDimension).setOnClickListener { selectTool(Tool.ADD_DIMENSION) }
    }

    private fun showColorPicker() {
        val names  = arrayOf("Black", "Red", "Blue", "Green", "Orange")
        val colors = arrayOf(Color.BLACK, Color.RED, Color.BLUE, Color.GREEN, Color.parseColor("#FF6600"))
        AlertDialog.Builder(this)
            .setTitle("Choose Color")
            .setItems(names) { _, i -> floorPlanView.setDrawColor(colors[i]) }
            .show()
    }

    private fun showLabelDialog() {
        val input = EditText(this)
        input.hint = "Enter text"
        AlertDialog.Builder(this)
            .setTitle("Add Text")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val text = input.text.toString().trim()
                if (text.isNotEmpty()) {
                    floorPlanView.pendingShapeType = "label:$text"
                    selectTool(Tool.ADD_SHAPE)
                    tvToolName.text = "Tap to place text"
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showClearDialog() {
        AlertDialog.Builder(this)
            .setTitle("Clear")
            .setMessage("Delete all shapes?")
            .setPositiveButton("Yes") { _, _ ->
                floorPlanView.clearAll()
                updateShapeCount()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun savePlanDialog() {
        val input = EditText(this)
        input.setText(currentPlanName)
        AlertDialog.Builder(this)
            .setTitle("Save Plan")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isBlank()) {
                    Toast.makeText(this, "Enter a name", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val data = floorPlanView.getShapesData()
                if (currentPlanId == -1L) {
                    currentPlanId = db.insertPlan(name, data)
                } else {
                    db.updatePlan(currentPlanId, name, data)
                }
                currentPlanName = name
                tvPlanName.text = name
                Toast.makeText(this, "✅ Saved: $name", Toast.LENGTH_SHORT).show()
                saveImage()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadPlan(id: Long) {
        val plan = db.getPlanById(id) ?: return
        currentPlanId   = plan.id
        currentPlanName = plan.name
        tvPlanName.text = plan.name
        floorPlanView.loadShapesData(plan.data)
        updateShapeCount()
        Toast.makeText(this, "Loaded: ${plan.name}", Toast.LENGTH_SHORT).show()
    }

    private fun resetPlan() {
        AlertDialog.Builder(this)
            .setTitle("New Plan")
            .setMessage("Save current plan first?")
            .setPositiveButton("Save") { _, _ ->
                savePlanDialog()
                clearCurrentPlan()
            }
            .setNegativeButton("Discard") { _, _ -> clearCurrentPlan() }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun clearCurrentPlan() {
        floorPlanView.clearAll()
        currentPlanId   = -1L
        currentPlanName = "New Plan"
        tvPlanName.text = currentPlanName
        updateShapeCount()
    }

    private fun saveImage() {
        if (floorPlanView.width == 0 || floorPlanView.height == 0) return
        val bitmap = Bitmap.createBitmap(floorPlanView.width, floorPlanView.height, Bitmap.Config.ARGB_8888)
        floorPlanView.draw(Canvas(bitmap))
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "plan_${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        }
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        uri?.let {
            contentResolver.openOutputStream(it)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
            Toast.makeText(this, "Image saved", Toast.LENGTH_SHORT).show()
        }
    }

    private fun renamePlan() {
        val input = EditText(this)
        input.setText(currentPlanName)
        AlertDialog.Builder(this)
            .setTitle("Rename Plan")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    currentPlanName = name
                    tvPlanName.text = name
                    if (currentPlanId != -1L) {
                        db.updatePlan(currentPlanId, name, floorPlanView.getShapesData())
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSavedPlans() {
        val plans = db.getAllPlans()
        if (plans.isEmpty()) {
            Toast.makeText(this, "No saved plans", Toast.LENGTH_SHORT).show()
            return
        }
        val names = plans.map { "📋 ${it.name}" }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Saved Plans")
            .setItems(names) { _, i -> showPlanOptions(plans[i]) }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showPlanOptions(plan: SavedPlan) {
        AlertDialog.Builder(this)
            .setTitle(plan.name)
            .setItems(arrayOf("📂 Load", "🗑️ Delete")) { _, which ->
                when (which) {
                    0 -> loadPlan(plan.id)
                    1 -> {
                        db.deletePlan(plan.id)
                        if (currentPlanId == plan.id) clearCurrentPlan()
                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }

    override fun onPause() {
        super.onPause()
        if (floorPlanView.getShapeCount() > 0) {
            val data = floorPlanView.getShapesData()
            if (currentPlanId == -1L) {
                currentPlanId = db.insertPlan(currentPlanName, data)
            } else {
                db.updatePlan(currentPlanId, currentPlanName, data)
            }
        }
    }
}