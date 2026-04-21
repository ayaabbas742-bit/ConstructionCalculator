package com.example.constructioncalculator

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.PI

class VerticalCylinderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vertical_cylinder)

        val rInput = findViewById<EditText>(R.id.editRadius)
        val hInput = findViewById<EditText>(R.id.editHeight)
        val fillInput = findViewById<EditText>(R.id.editFill) // hf
        val radioFull = findViewById<RadioButton>(R.id.radioFull)
        val result = findViewById<TextView>(R.id.txtResult)
        val btn = findViewById<Button>(R.id.btnCalculate)

        btn.setOnClickListener {

            val r = rInput.text.toString().toFloatOrNull() ?: 0f
            val h = hInput.text.toString().toFloatOrNull() ?: 0f

            if (r <= 0f || h <= 0f) {
                result.text = "❌ Enter valid values"
                return@setOnClickListener
            }

            val volume = if (radioFull.isChecked) {
                // 🟢 FULL TANK
                PI * r * r * h
            } else {
                // 🟡 PARTIAL TANK
                val hf = fillInput.text.toString().toFloatOrNull() ?: 0f
                PI * r * r * hf
            }

            // ✔️ Areas (always same geometry)
            val baseArea = PI * r * r
            val lateralArea = 2 * PI * r * h

            // ✔️ Water weight
            val liters = volume * 1000
            val weightKg = liters

            // ✔️ Simplified load index
            val weightKN = weightKg / 1000
            val pressure = weightKN / baseArea

            // ✔️ Safety logic (approximation)
            val safety = when {
                pressure < 5 -> "🟢 SAFE"
                pressure < 15 -> "🟡 CHECK STRUCTURE"
                else -> "🔴 DANGER"
            }

            result.text = """
                Volume = %.2f m³
                Volume = %.0f L
                Base Area = %.2f m²
                Lateral Area = %.2f m²
                Pressure Index = %.2f kN/m²
                Safety = %s
            """.trimIndent().format(
                volume,
                liters,
                baseArea,
                lateralArea,
                pressure,
                safety
            )
        }
    }
}