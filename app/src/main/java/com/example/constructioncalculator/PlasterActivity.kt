package com.example.constructioncalculator

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class PlasterActivity : AppCompatActivity() {

    // 🧱 Wall Model
    data class Wall(
        val length: Double,
        val height: Double,
        val doorArea: Double,
        val windowArea: Double
    )

    private val walls = mutableListOf<Wall>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plaster)

        // 📌 INPUTS
        val etLength = findViewById<EditText>(R.id.etLength)
        val etHeight = findViewById<EditText>(R.id.etHeight)
        val etDoorL = findViewById<EditText>(R.id.etDoorL)
        val etDoorW = findViewById<EditText>(R.id.etDoorW)

        val etWindowL = findViewById<EditText>(R.id.etWindowL)
        val etWindowW = findViewById<EditText>(R.id.etWindowW)

        // 📌 SPINNERS
        val spinnerThickness = findViewById<Spinner>(R.id.spinnerThickness)
        val spinnerMortar = findViewById<Spinner>(R.id.spinnerMortar)

        // 📌 BUTTONS
        val btnAdd = findViewById<Button>(R.id.btnAddWall)
        val btnDelete = findViewById<Button>(R.id.btnDeleteWall)
        val btnReset = findViewById<Button>(R.id.btnReset)
        val btnCalc = findViewById<Button>(R.id.btnCalcPlaster)

        // 📌 OUTPUT
        val tvWalls = findViewById<TextView>(R.id.tvWalls)
        val tvArea = findViewById<TextView>(R.id.tvArea)
        val tvVolume = findViewById<TextView>(R.id.tvVolume)
        val tvCement = findViewById<TextView>(R.id.tvCement)
        val tvSand = findViewById<TextView>(R.id.tvSand)

        // 📏 THICKNESS
        val thicknessArray = arrayOf(
            "Ceiling (8 mm)",
            "Internal Wall (12 mm)",
            "Internal Wall (15 mm)",
            "External Wall (20 mm)"
        )

        spinnerThickness.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            thicknessArray
        )

        // 🧪 MORTAR
        val mortarArray = arrayOf("1:3", "1:4", "1:5", "1:6")

        spinnerMortar.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            mortarArray
        )

        // ➕ ADD WALL
        btnAdd.setOnClickListener {

            val length = etLength.text.toString().toDoubleOrNull() ?: 0.0
            val height = etHeight.text.toString().toDoubleOrNull() ?: 0.0

            val doorArea =
                (etDoorL.text.toString().toDoubleOrNull() ?: 0.0) *
                        (etDoorW.text.toString().toDoubleOrNull() ?: 0.0)

            val windowArea =
                (etWindowL.text.toString().toDoubleOrNull() ?: 0.0) *
                        (etWindowW.text.toString().toDoubleOrNull() ?: 0.0)

            if (length > 0 && height > 0) {

                walls.add(
                    Wall(
                        length = length,
                        height = height,
                        doorArea = doorArea,
                        windowArea = windowArea
                    )
                )

                etLength.text.clear()
                etHeight.text.clear()
                etDoorL.text.clear()
                etDoorW.text.clear()
                etWindowL.text.clear()
                etWindowW.text.clear()

                updateWalls(tvWalls)
            }
        }

        // 🗑 DELETE LAST WALL
        btnDelete.setOnClickListener {
            if (walls.isNotEmpty()) {
                walls.removeAt(walls.lastIndex)
                updateWalls(tvWalls)
            }
        }

        // 🔄 RESET
        btnReset.setOnClickListener {
            walls.clear()
            updateWalls(tvWalls)

            tvArea.text = ""
            tvVolume.text = ""
            tvCement.text = ""
            tvSand.text = ""
        }

        // 📊 CALCULATE
        btnCalc.setOnClickListener {
            val area = walls.sumOf { wall ->
                (wall.length * wall.height) - (wall.doorArea + wall.windowArea)
            }.coerceAtLeast(0.0)

            val thickness = when (spinnerThickness.selectedItem.toString()) {
                "Ceiling (8 mm)" -> 0.008
                "Internal Wall (12 mm)" -> 0.012
                "Internal Wall (15 mm)" -> 0.015
                "External Wall (20 mm)" -> 0.020
                else -> 0.012
            }

            val wetVolume = area * thickness
            val dryVolume = wetVolume * 1.33

            val ratio = spinnerMortar.selectedItem.toString().split(":")
            val c = ratio[0].toDouble()
            val s = ratio[1].toDouble()
            val total = c + s

            val cementVol = dryVolume * (c / total)
            val sandVol = dryVolume * (s / total)

            val cementBags = (cementVol * 1440) / 50

            tvArea.text = "Walls: ${walls.size}\nArea: %.2f m²".format(area)

            tvVolume.text =
                "Wet Volume: %.3f m³\nDry Volume: %.3f m³".format(wetVolume, dryVolume)

            tvCement.text =
                "Cement: %.2f bags".format(cementBags)

            tvSand.text =
                "Sand: %.3f m³".format(sandVol)
        }
    }

    // 📌 DISPLAY WALLS
    private fun updateWalls(tv: TextView) {
        tv.text = if (walls.isEmpty()) {
            "Walls: none"
        } else {
            walls.mapIndexed { i, w ->
                "W${i + 1}: ${w.length}×${w.height} | Doors:${w.doorArea} | Windows:${w.windowArea}"
            }.joinToString("\n")
        }
    }
}
