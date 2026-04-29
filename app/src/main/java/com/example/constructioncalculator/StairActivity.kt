package com.example.constructioncalculator

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.*

class StairActivity : AppCompatActivity() {

    lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stair_case)

        db = DatabaseHelper(this)

        val spinner = findViewById<Spinner>(R.id.spinnerType)
        val img = findViewById<ImageView>(R.id.imgStair)

        val etH = findViewById<EditText>(R.id.etH)
        val etW = findViewById<EditText>(R.id.etW)
        val etL = findViewById<EditText>(R.id.etL)

        val etD1 = findViewById<EditText>(R.id.etD1)
        val etD2 = findViewById<EditText>(R.id.etD2)
        val etA = findViewById<EditText>(R.id.etAlpha)

        val layoutSpiral = findViewById<LinearLayout>(R.id.layoutSpiral)

        val btnCalc = findViewById<Button>(R.id.btnCalc)
        val btnHistory = findViewById<Button>(R.id.btnHistory)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        val types = arrayOf("Straight", "L Shape", "U Shape", "Spiral")

        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            types
        )

        // 🖼 IMAGE + UI CONTROL
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {

                val type = types[pos]

                img.setImageResource(
                    when (type) {
                        "Straight" -> R.drawable.straight
                        "L Shape" -> R.drawable.l_shape
                        "U Shape" -> R.drawable.u_shape
                        else -> R.drawable.spiral
                    }
                )

                layoutSpiral.visibility =
                    if (type == "Spiral") View.VISIBLE else View.GONE
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 🧠 ENGINEERING CORE
        btnCalc.setOnClickListener {

            val type = spinner.selectedItem.toString()

            val H = etH.text.toString().toDoubleOrNull()
            val W = etW.text.toString().toDoubleOrNull()
            val Lavailable = etL.text.toString().toDoubleOrNull()

            if (H == null || W == null || Lavailable == null) {
            Toast.makeText(this, "Fill all main fields", Toast.LENGTH_SHORT).show()
            return@setOnClickListener
        }

            // 🧠 DESIGN ENGINE
            val idealR = 0.17
            val N = max(1, (H / idealR).toInt())

            val R = H / N
            val T = 0.63 - (2 * R)
            val blondel = 2 * R + T

            val validBlondel = blondel in 0.60..0.64

            val Lneeded = (N - 1) * T

            val spaceStatus = when {
                Lneeded <= Lavailable -> "SPACE OK ✅"
                Lneeded <= Lavailable * 1.5 -> "LIMITED SPACE ⚠️"
                else -> "NOT ENOUGH SPACE ❌"
            }

            val designStatus = when {
                blondel < 0.60 -> "TOO COMPACT ❌"
                blondel > 0.64 -> "TOO LARGE ❌"
                else -> "COMFORTABLE ✅"
            }

            var length = 0.0
            var area = 0.0
            var result = ""

            when (type) {

                "Straight", "L Shape", "U Shape" -> {

                    length = Lneeded
                    area = length * W

                    result = """
                        🪜 $type Stair
                        Steps: $N
                        Riser: %.3f m
                        Tread: %.3f m
                        Blondel: %.3f m
                        
                        Required Length: %.2f m
                        Available: %.2f m
                        
                        Space: $spaceStatus
                        Comfort: $designStatus
                        
                        Area: %.2f m²
                    """.trimIndent().format(R, T, blondel, Lneeded, Lavailable, area)
                }

                "Spiral" -> {

                    val D1 = etD1.text.toString().toDoubleOrNull()
                    val D2 = etD2.text.toString().toDoubleOrNull()
                    val alpha = etA.text.toString().toDoubleOrNull()

                    if (D1 == null || D2 == null || alpha == null) {
                        Toast.makeText(this, "Fill spiral fields", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val Rw = (D1 + D2) / 4
                    val stepAngle = alpha / N

                    val tread = 2 * Math.PI * Rw * (stepAngle / 360.0)
                    length = 2 * Math.PI * Rw * (alpha / 360.0)
                    area = length * W

                    result = """
                        🌀 Spiral Stair
                        Steps: $N
                        Riser: %.3f m
                        Radius: %.2f m
                        Step Angle: %.2f°
                        Tread: %.3f m
                        Run: %.2f m
                        Comfort: $designStatus
                    """.trimIndent().format(R, Rw, stepAngle, tread, length)
                }
            }

            tvResult.text = result

            db.insert(type, R, N, length, area)
        }

        // 📜 HISTORY
        btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }
}