package com.example.constructioncalculator

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class RectangularTankActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rectangular_tank)

        val L = findViewById<EditText>(R.id.editLength)
        val W = findViewById<EditText>(R.id.editWidth)
        val H = findViewById<EditText>(R.id.editHeight)
        val fill = findViewById<EditText>(R.id.editFill)

        val radioFull = findViewById<RadioButton>(R.id.radioFull)
        val result = findViewById<TextView>(R.id.txtResult)
        val btn = findViewById<Button>(R.id.btnCalculate)

        btn.setOnClickListener {

            val length = L.text.toString().toDoubleOrNull() ?: 0.0
            val width = W.text.toString().toDoubleOrNull() ?: 0.0
            val height = H.text.toString().toDoubleOrNull() ?: 0.0

            if (length <= 0 || width <= 0  ||height <= 0) {
            result.text = "❌ Enter valid dimensions"
            return@setOnClickListener
        }

            val volume = if (radioFull.isChecked) {

                // 🟢 FULL TANK
                length * width * height

            } else {

                // 🟡 PARTIAL TANK (CORRECT FORMULA)
                val f = fill.text.toString().toDoubleOrNull() ?: 0.0

                if (f <= 0 || f > height) {
                    result.text = "❌ Fill height must be between 0 and tank height"
                    return@setOnClickListener
                }

                length * width * f
            }

            // ✔️ Conversion
            val liters = volume * 1000

            // ✔️ Engineering check (simplified)
            val baseArea = length * width
            val pressure = (liters / 1000) / baseArea

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