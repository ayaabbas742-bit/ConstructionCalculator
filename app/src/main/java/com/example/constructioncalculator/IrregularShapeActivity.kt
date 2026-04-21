package com.example.constructioncalculator

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs

class IrregularShapeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_irregular_shape)

        val shapeView = findViewById<IrregularShapeView>(R.id.shapeView)
        val result = findViewById<TextView>(R.id.txtArea)
        val btn = findViewById<Button>(R.id.btnCalculate)

        val inputs = listOf(
            findViewById<EditText>(R.id.p1x) to findViewById<EditText>(R.id.p1y),
            findViewById<EditText>(R.id.p2x) to findViewById<EditText>(R.id.p2y),
            findViewById<EditText>(R.id.p3x) to findViewById<EditText>(R.id.p3y),
            findViewById<EditText>(R.id.p4x) to findViewById<EditText>(R.id.p4y)
        )

        btn.setOnClickListener {

            val points = inputs.map {
                val x = it.first.text.toString().toFloatOrNull() ?: 0f
                val y = it.second.text.toString().toFloatOrNull() ?: 0f
                x to y
            }

            if (points.size < 3) {
                result.text = "Need at least 3 points"
                return@setOnClickListener
            }

            // رسم الشكل
            shapeView.points = points

            // حساب المساحة
            val area = calculateArea(points)

            result.text = "Area = %.2f m²".format(area)
        }
    }

    // ✔ Shoelace Formula (صحيحة 100%)
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