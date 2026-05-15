package com.example.constructioncalculator

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs

class IrregularShapeActivity : AppCompatActivity() {

    private val allPoints = mutableListOf<Pair<Float, Float>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_irregular_shape)

        val shapeView    = findViewById<IrregularShapeView>(R.id.shapeView)
        val result       = findViewById<TextView>(R.id.txtArea)
        val etX          = findViewById<EditText>(R.id.etX)
        val etY          = findViewById<EditText>(R.id.etY)
        val tvCount      = findViewById<TextView>(R.id.tvPointCount)
        val tvPointsList = findViewById<TextView>(R.id.tvPointsList)
        val etEditIndex  = findViewById<EditText>(R.id.etEditIndex)
        val etEditX      = findViewById<EditText>(R.id.etEditX)
        val etEditY      = findViewById<EditText>(R.id.etEditY)

        fun updateCount() {
            tvCount.text = "Points added: ${allPoints.size}"
        }

        fun updateList() {
            tvPointsList.text = if (allPoints.isEmpty()) {
                "No points yet"
            } else {
                allPoints.mapIndexed { i, (x, y) ->
                    "P${i + 1}:  X = $x  |  Y = $y"
                }.joinToString("\n")
            }
        }

        // ➕ ADD
        findViewById<Button>(R.id.btnAddPoint).setOnClickListener {
            val x = etX.text.toString().toFloatOrNull()
            val y = etY.text.toString().toFloatOrNull()
            if (x == null || y == null) {
                Toast.makeText(this, "Enter valid X and Y", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            allPoints.add(x to y)
            etX.text.clear()
            etY.text.clear()
            updateCount()
            updateList()
            Toast.makeText(this, "Point ${allPoints.size} added", Toast.LENGTH_SHORT).show()
        }

        // ❌ DELETE LAST
        findViewById<Button>(R.id.btnDeletePoint).setOnClickListener {
            if (allPoints.isNotEmpty()) {
                allPoints.removeAt(allPoints.lastIndex)
                updateCount()
                updateList()
                Toast.makeText(this, "Last point removed", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No points to remove", Toast.LENGTH_SHORT).show()
            }
        }

        // ♻️ CLEAR
        findViewById<Button>(R.id.btnClearPoints).setOnClickListener {
            allPoints.clear()
            shapeView.points = emptyList()
            result.text = "Area ="
            updateCount()
            updateList()
        }

        // ✏️ EDIT
        findViewById<Button>(R.id.btnEditPoint).setOnClickListener {
            val index = etEditIndex.text.toString().toIntOrNull()
            val newX  = etEditX.text.toString().toFloatOrNull()
            val newY  = etEditY.text.toString().toFloatOrNull()

            when {
                index == null || newX == null || newY == null ->
                Toast.makeText(this, "Enter point number, X, and Y", Toast.LENGTH_SHORT).show()

                index < 1 || index > allPoints.size ->
                    Toast.makeText(this, "Point number must be between 1 and ${allPoints.size}", Toast.LENGTH_SHORT).show()

                else -> {
                    allPoints[index - 1] = newX to newY
                    etEditIndex.text.clear()
                    etEditX.text.clear()
                    etEditY.text.clear()
                    updateList()
                    Toast.makeText(this, "Point $index updated ✅", Toast.LENGTH_SHORT).show()
                }
            }
        }
        // 📐 CALCULATE
        findViewById<Button>(R.id.btnCalculate).setOnClickListener {
            if (allPoints.size < 3) {
                result.text = "Need at least 3 points"
                return@setOnClickListener
            }
            shapeView.points = allPoints.toList()
            val area = calculateArea(allPoints)
            result.text = "Area = %.2f m²".format(area)
        }
    }

    private fun calculateArea(points: List<Pair<Float, Float>>): Double {
        var sum = 0.0
        val n = points.size
        for (i in 0 until n) {
            val (x1, y1) = points[i]
            val (x2, y2) = points[(i + 1) % n]
            sum += (x1 * y2) - (x2 * y1)
        }
        return abs(sum / 2.0)
    }
}