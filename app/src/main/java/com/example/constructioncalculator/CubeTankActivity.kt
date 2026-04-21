package com.example.constructioncalculator

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class CubeTankActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cube_tank)

        val l = findViewById<EditText>(R.id.editLength)
        val w = findViewById<EditText>(R.id.editWidth)
        val h = findViewById<EditText>(R.id.editHeight)

        val fill = findViewById<EditText>(R.id.editFill)
        val radioFull = findViewById<RadioButton>(R.id.radioFull)
        val result = findViewById<TextView>(R.id.txtResult)
        val btn = findViewById<Button>(R.id.btnCalculate)

        btn.setOnClickListener {

            val L = l.text.toString().toFloatOrNull() ?: 0f
            val W = w.text.toString().toFloatOrNull() ?: 0f
            val H = h.text.toString().toFloatOrNull() ?: 0f

            if (L <= 0f || W <= 0f || H <= 0f) {
            result.text = "❌ Enter valid values"
            return@setOnClickListener
        }

            // ✔ Full volume
            val volumeFull = L * W * H

            // ✔ Check type
            val volumeFinal = if (radioFull.isChecked) {
                volumeFull
            } else {
                val f = fill.text.toString().toFloatOrNull() ?: 0f
                val percent = f / 100f
                volumeFull * percent
            }

            // ✔ conversions
            val liters = volumeFinal * 1000
            val weight = liters

            // ✔ safety simple check
            val baseArea = L * W
            val pressure = weight / baseArea

            val safety = when {
                pressure < 5 -> "🟢 SAFE"
                pressure < 15 -> "🟡 CHECK STRUCTURE"
                else -> "🔴 DANGER"
            }

            result.text = """
                Volume = %.2f m³
                Volume = %.0f L
                Safety = %s
            """.trimIndent().format(volumeFinal, liters, safety)
        }
    }
}