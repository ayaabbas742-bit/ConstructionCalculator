package com.example.constructioncalculator

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class PaintActivity : AppCompatActivity() {

    data class Wall(
        val length: Double,
        val height: Double,
        val doors: Double,
        val windows: Double
    )

    private val walls = mutableListOf<Wall>()
    lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paint)

        // ================= UI =================
        val img = findViewById<ImageView>(R.id.imgPaint)

        val sp1 = findViewById<Spinner>(R.id.spPaintType)
        val sp2 = findViewById<Spinner>(R.id.spPaintType2)
        val rg = findViewById<RadioGroup>(R.id.rgPaintMode)

        val msg = findViewById<TextView>(R.id.tvModeMsg) // ⭐ MESSAGE

        val etL = findViewById<EditText>(R.id.etLength)
        val etH = findViewById<EditText>(R.id.etHeight)

        val etDL = findViewById<EditText>(R.id.etDoorL)
        val etDW = findViewById<EditText>(R.id.etDoorW)
        val etWL = findViewById<EditText>(R.id.etWindowL)
        val etWW = findViewById<EditText>(R.id.etWindowW)

        val etP1 = findViewById<EditText>(R.id.etPercent1)
        val etP2 = findViewById<EditText>(R.id.etPercent2)

        val tv = findViewById<TextView>(R.id.tvResult)
        val tvWalls = findViewById<TextView>(R.id.tvWalls)

        db = DatabaseHelper(this)

        // ================= MESSAGE MODE =================
        rg.setOnCheckedChangeListener { _, id ->
            if (id == R.id.rbDual) {
                msg.text = "Dual Mode: You can use TWO paints (mix %)"
            } else {
                msg.text = "Single Mode: One paint only"
            }
        }

        // ================= TYPES =================
        val types = arrayOf(
            "Acrylic",
            "Oil",
            "Decorative",
            "Anti-moisture",
            "Exterior",
            "Epoxy",
            "Primer"
        )

        sp1.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types)
        sp2.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types)

        // ================= IMAGE CHANGE =================
        sp1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {

                val image = when (position) {
                    0 -> R.drawable.paint_acrylic
                    1 -> R.drawable.paint_oil
                    2 -> R.drawable.paint_decorative
                    3 -> R.drawable.paint_moisture
                    4 -> R.drawable.paint_exterior
                    5 -> R.drawable.paint_epoxy
                    6 -> R.drawable.paint_primer
                    else -> R.drawable.paint_acrylic
                }

                img.setImageResource(image)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // ================= ADD WALL =================
        findViewById<Button>(R.id.btnAddWall).setOnClickListener {

            val wall = Wall(
                etL.text.toString().toDoubleOrNull() ?: 0.0,
                etH.text.toString().toDoubleOrNull() ?: 0.0,
                (etDL.text.toString().toDoubleOrNull() ?: 0.0) *
                        (etDW.text.toString().toDoubleOrNull() ?: 0.0),
                (etWL.text.toString().toDoubleOrNull() ?: 0.0) *
                        (etWW.text.toString().toDoubleOrNull() ?: 0.0)
            )

            walls.add(wall)
            updateWalls(tvWalls)

            Toast.makeText(this, "Wall Added", Toast.LENGTH_SHORT).show()
        }
        // ================= DELETE =================
        findViewById<Button>(R.id.btnDeleteWall).setOnClickListener {
            if (walls.isNotEmpty()) {
                walls.removeAt(walls.lastIndex)
                updateWalls(tvWalls)
            }
        }

        // ================= RESET =================
        findViewById<Button>(R.id.btnReset).setOnClickListener {
            walls.clear()
            updateWalls(tvWalls)
            tv.text = ""
        }

        // ================= CALCULATE =================
        findViewById<Button>(R.id.btnCalc).setOnClickListener {

            val netArea = walls.sumOf {
                (it.length * it.height) - (it.doors + it.windows)
            }.coerceAtLeast(0.0)

            val coverage = when (sp1.selectedItemPosition) {
                0 -> 10.0
                1 -> 8.0
                2 -> 6.0
                3 -> 9.0
                4 -> 10.0
                5 -> 5.0
                6 -> 10.0
                else -> 10.0
            }

            val coats = when (sp1.selectedItemPosition) {
                2, 4 -> 3
                6 -> 1
                else -> 2
            }

            val basePaint = (netArea / coverage) * coats * 1.10

            val isDual = rg.checkedRadioButtonId == R.id.rbDual

            val p1 = if (isDual) etP1.text.toString().toDoubleOrNull() ?: 100.0 else 100.0
            val p2 = if (isDual) etP2.text.toString().toDoubleOrNull() ?: 0.0 else 0.0

            val paint1 = basePaint * (p1 / 100.0)
            val paint2 = basePaint * (p2 / 100.0)

            val finalPaint = paint1 + paint2

            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            db.insertPaint(
                sp1.selectedItem.toString(),
                netArea,
                coats,
                finalPaint,
                date
            )

            if (isDual) {
                tv.text = """
                    Walls: ${walls.size}
                    Net Area: %.2f m²
                    Coats: $coats

                    Paint 1: %.2f L
                    Paint 2: %.2f L
                    Total: %.2f L
                """.trimIndent().format(netArea, paint1, paint2, finalPaint)
            } else {
                tv.text = """
                    Walls: ${walls.size}
                    Net Area: %.2f m²
                    Coats: $coats

                    Paint: %.2f L
                """.trimIndent().format(netArea, finalPaint)
            }
        }
    }

    private fun updateWalls(tv: TextView) {
        tv.text = if (walls.isEmpty()) {
            "Walls: none"
        } else {
            walls.mapIndexed { i, w ->
                "W${i + 1}: ${w.length} x ${w.height}"
            }.joinToString("\n")
        }
    }
}