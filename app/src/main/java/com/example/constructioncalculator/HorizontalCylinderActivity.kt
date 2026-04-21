package com.example.constructioncalculator

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.*

class HorizontalCylinderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_horizontal_cylinder)

        val rInput = findViewById<EditText>(R.id.editRadius)
        val lInput = findViewById<EditText>(R.id.editLength)
        val fInput = findViewById<EditText>(R.id.editFill)
        val radioFull = findViewById<RadioButton>(R.id.radioFull)
        val result = findViewById<TextView>(R.id.txtResult)
        val btn = findViewById<Button>(R.id.btnCalculate)

        btn.setOnClickListener {

            // ✔ Convert to Double (IMPORTANT)
            val r = rInput.text.toString().toDoubleOrNull() ?: 0.0
            val L = lInput.text.toString().toDoubleOrNull() ?: 0.0

            if (r <= 0.0 || L <= 0.0) {
                result.text = "❌ Enter valid values"
                return@setOnClickListener
            }

            val volume = if (radioFull.isChecked) {

                // 🟢 FULL TANK
                Math.PI * r * r * L

            } else {

                // 🟡 PARTIAL TANK (REAL FORMULA)
                val f = fInput.text.toString().toDoubleOrNull() ?: 0.0

                if (f <= 0.0 || f >= 2 * r) {
                    result.text = "❌ Fill height must be between 0 and 2R"
                    return@setOnClickListener
                }

                val term1 = r * r * acos((r - f) / r)
                val term2 = (r - f) * sqrt(2 * r * f - f * f)

                val areaSegment = term1 - term2

                areaSegment * L
            }

            // ✔ Conversions
            val liters = volume * 1000
            val weightKg = liters

            // ✔ Simplified engineering check
            val baseArea = Math.PI * r * r
            val pressure = (weightKg / 1000) / baseArea

            val safety = when {
                pressure < 5 -> "🟢 SAFE"
                pressure < 15 -> "🟡 CHECK STRUCTURE"
                else -> "🔴 DANGER"
            }

            result.text = """
                Volume = %.2f m³
                Volume = %.0f L
                Safety = %s
            """.trimIndent().format(volume, liters, safety)
        }
    }
}