package com.example.constructioncalculator

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.*
import androidx.appcompat.app.AlertDialog

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
        val btnHistory = findViewById<Button>(R.id.btnHistory)

        val db = DatabaseHelper(this)

        btn.setOnClickListener {
            val r = rInput.text.toString().toDoubleOrNull() ?: 0.0
            val L = lInput.text.toString().toDoubleOrNull() ?: 0.0

            if (r <= 0.0 || L <= 0.0) {
                result.text = "❌ Enter valid values"
                return@setOnClickListener
            }

            val volume = if (radioFull.isChecked) {
                Math.PI * r * r * L
            } else {
                val f = fInput.text.toString().toDoubleOrNull() ?: 0.0
                if (f <= 0.0 || f >= 2 * r) {
                    result.text = "❌ Fill height must be between 0 and 2R"
                    return@setOnClickListener
                }
                val term1 = r * r * acos((r - f) / r)
                val term2 = (r - f) * sqrt(2 * r * f - f * f)
                (term1 - term2) * L
            }

            val liters = volume * 1000
            val baseArea = Math.PI * r * r
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
                type = "Horizontal Cylinder",
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