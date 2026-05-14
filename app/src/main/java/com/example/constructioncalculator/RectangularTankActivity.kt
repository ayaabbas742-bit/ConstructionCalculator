package com.example.constructioncalculator

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog

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
        val btnHistory = findViewById<Button>(R.id.btnHistory)

        val db = DatabaseHelper(this)

        btn.setOnClickListener {
            val length = L.text.toString().toDoubleOrNull() ?: 0.0
            val width = W.text.toString().toDoubleOrNull() ?: 0.0
            val height = H.text.toString().toDoubleOrNull() ?: 0.0

            if (length <= 0 || width <= 0 || height <= 0) {
            result.text = "❌ Enter valid dimensions"
            return@setOnClickListener
        }

            val volume = if (radioFull.isChecked) {
                length * width * height
            } else {
                val f = fill.text.toString().toDoubleOrNull() ?: 0.0
                if (f <= 0 || f > height) {
                    result.text = "❌ Fill height must be between 0 and tank height"
                    return@setOnClickListener
                }
                length * width * f
            }

            val liters = volume * 1000
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

            // ✅ حفظ في قاعدة البيانات
            val date = java.text.SimpleDateFormat(
                "yyyy-MM-dd", java.util.Locale.getDefault()
            ).format(java.util.Date())

            db.insertTankHistory(
                type = "Rectangular",
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
