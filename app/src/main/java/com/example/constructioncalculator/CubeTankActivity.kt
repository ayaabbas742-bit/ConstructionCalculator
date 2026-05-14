package com.example.constructioncalculator

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog

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
        val btnHistory = findViewById<Button>(R.id.btnHistory)

        val db = DatabaseHelper(this)

        btn.setOnClickListener {
            val L = l.text.toString().toDoubleOrNull() ?: 0.0
            val W = w.text.toString().toDoubleOrNull() ?: 0.0
            val H = h.text.toString().toDoubleOrNull() ?: 0.0

            if (L <= 0.0 || W <= 0.0 || H <= 0.0) {
            result.text = "❌ Enter valid values"
            return@setOnClickListener
        }

            val volume = if (radioFull.isChecked) {
                // 🟢 FULL TANK
                L * W * H
            } else {
                // 🟡 PARTIAL TANK
                val f = fill.text.toString().toDoubleOrNull() ?: 0.0

                if (f <= 0.0 || f > H) {
                    result.text = "❌ Fill height must be between 0 and tank height"
                    return@setOnClickListener
                }

                // ✅ الصحيح: ارتفاع الماء الفعلي
                L * W * f
            }

            // ✅ الحسابات الدقيقة
            val liters = volume * 1000
            val weightKg = liters // 1L ماء = 1kg
            val weightKN = weightKg / 1000 // تحويل لـ kN
            val baseArea = L * W
            val pressure = weightKN / baseArea // kN/m²

            // ✅ السطوح
            val baseAreaTotal = 2 * (L * W) // قاعدة + سقف
            val lateralArea = 2 * (L + W) * H // الجوانب الأربعة
            val totalSurface = baseAreaTotal + lateralArea

            val safety = when {
                pressure < 5 -> "🟢 SAFE"
                pressure < 15 -> "🟡 CHECK STRUCTURE"
                else -> "🔴 DANGER"
            }

            result.text = """
                ─── Volume ───
                Volume        = %.4f m³
                Volume        = %.2f L
                Water Weight  = %.2f kg

                ─── Surface ───
                Base Area     = %.4f m²
                Lateral Area  = %.4f m²
                Total Surface = %.4f m²

                ─── Safety ───
                Pressure      = %.4f kN/m²
                Safety        = %s
            """.trimIndent().format(
                volume,
                liters,
                weightKg,
                baseArea,
                lateralArea,
                totalSurface,
                pressure,
                safety
            )

            // ✅ حفظ في قاعدة البيانات
            val date = java.text.SimpleDateFormat(
                "yyyy-MM-dd", java.util.Locale.getDefault()
            ).format(java.util.Date())

            db.insertTankHistory(
                type = "Cube",
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