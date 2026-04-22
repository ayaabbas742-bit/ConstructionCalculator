package com.example.constructioncalculator

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs

class IrregularShapeActivity : AppCompatActivity() {

    private val extraPoints = mutableListOf<Pair<Float, Float>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_irregular_shape)

        val shapeView = findViewById<IrregularShapeView>(R.id.shapeView)
        val result = findViewById<TextView>(R.id.txtArea)

        val inputs = listOf(
            findViewById<EditText>(R.id.p1x) to findViewById<EditText>(R.id.p1y),
            findViewById<EditText>(R.id.p2x) to findViewById<EditText>(R.id.p2y),
            findViewById<EditText>(R.id.p3x) to findViewById<EditText>(R.id.p3y),
            findViewById<EditText>(R.id.p4x) to findViewById<EditText>(R.id.p4y)
        )

        val etX = findViewById<EditText>(R.id.etX)
        val etY = findViewById<EditText>(R.id.etY)

        // ➕ ADD
        findViewById<Button>(R.id.btnAddPoint).setOnClickListener {

            val x = etX.text.toString().toFloatOrNull()
            val y = etY.text.toString().toFloatOrNull()

            if (x == null || y == null) return@setOnClickListener

            extraPoints.add(x to y)

            etX.text.clear()
            etY.text.clear()
        }

        // ❌ DELETE LAST
        findViewById<Button>(R.id.btnDeletePoint).setOnClickListener {
            if (extraPoints.isNotEmpty()) {
                extraPoints.removeAt(extraPoints.lastIndex)
            }
        }

        // ♻️ CLEAR
        findViewById<Button>(R.id.btnClearPoints).setOnClickListener {
            extraPoints.clear()
            result.text = "Area ="
        }

        // 📐 CALCULATE
        findViewById<Button>(R.id.btnCalculate).setOnClickListener {

            val basePoints = inputs.mapNotNull {
                val x = it.first.text.toString().toFloatOrNull()
                val y = it.second.text.toString().toFloatOrNull()
                if (x != null && y != null) x to y else null
            }

            val allPoints = basePoints + extraPoints

            if (allPoints.size < 3) {
                result.text = "Need at least 3 points"
                return@setOnClickListener
            }

            shapeView.points = allPoints

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