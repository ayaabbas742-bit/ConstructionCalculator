package com.example.constructioncalculator

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.PI
import androidx.appcompat.app.AlertDialog

class VerticalCylinderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vertical_cylinder)

        val rInput = findViewById<EditText>(R.id.editRadius)
        val hInput = findViewById<EditText>(R.id.editHeight)
        val fillInput = findViewById<EditText>(R.id.editFill)
        val radioFull = findViewById<RadioButton>(R.id.radioFull)
        val result = findViewById<TextView>(R.id.txtResult)
        val btn = findViewById<Button>(R.id.btnCalculate)
        val btnHistory = findViewById<Button>(R.id.btnHistory)

        val db = DatabaseHelper(this)

        btn.setOnClickListener {
            val r = rInput.text.toString().toFloatOrNull() ?: 0f
            val h = hInput.text.toString().toFloatOrNull() ?: 0f

            if (r <= 0f || h <= 0f) {
                result.text = "❌ Enter valid values"
                return@setOnClickListener
            }

            val volume = if (radioFull.isChecked) {
                PI * r * r * h
            } else {
                val hf = fillInput.text.toString().toFloatOrNull() ?: 0f
                PI * r * r * hf
            }

            val baseArea = PI * r * r
            val lateralArea = 2 * PI * r * h
            val liters = volume * 1000
            val pressure = (liters / 1000) / baseArea

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
                Pressure = %.2f kN/m²
                Safety = %s
            """.trimIndent().format(
                volume, liters, baseArea, lateralArea, pressure, safety
            )

            // ✅ حفظ في قاعدة البيانات
            val date = java.text.SimpleDateFormat(
                "yyyy-MM-dd", java.util.Locale.getDefault()
            ).format(java.util.Date())

            db.insertTankHistory(
                type = "Vertical Cylinder",
                volumeM3 = volume,
                volumeLiters = liters,
                safety = safety,
                date = date
            )
        }

        btnHistory.setOnClickListener {
            val db = DatabaseHelper(this)
            val history = db.getAllTankHistory()

            if (history.isEmpty()) {
                AlertDialog.Builder(this)
                    .setTitle("📋 Tank History")
                    .setMessage("No history yet.")
                    .setPositiveButton("OK", null)
                    .show()
                return@setOnClickListener
            }

            val items = history.map {
                "🪣 ${it["type"]} | ${it["date"]}\n" +
                        "📦 ${it["volume_m3"]} m³ / ${it["volume_liters"]} L\n" +
                        "⚠️ ${it["safety"]}"
            }.toTypedArray()

            AlertDialog.Builder(this)
                .setTitle("📋 Tank History")
                .setItems(items, null)
                .setNegativeButton("Close", null)
                .setNeutralButton("🗑️ Clear") { _, _ ->
                    db.clearTankHistory()
                    Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show()
                }
                .show()
        }
    }
}