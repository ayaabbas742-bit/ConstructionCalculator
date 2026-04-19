package com.example.constructioncalculator

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.ceil

class BrickActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_brick)

        // ================= VIEWS =================
        val spinnerBrick = findViewById<Spinner>(R.id.spinnerBrick)
        val spinnerUnit = findViewById<Spinner>(R.id.spinnerUnit)
        val spinnerThickness = findViewById<Spinner>(R.id.spinnerThickness)
        val spinnerMortar = findViewById<Spinner>(R.id.spinnerMortar)

        val imgBrick = findViewById<ImageView>(R.id.imgBrick)

        val etLength = findViewById<EditText>(R.id.etLength)
        val etHeight = findViewById<EditText>(R.id.etHeight)
        val etDoors = findViewById<EditText>(R.id.etDoors)
        val etWindows = findViewById<EditText>(R.id.etWindows)

        val tvBrick = findViewById<TextView>(R.id.tvBrick)
        val tvCement = findViewById<TextView>(R.id.tvCement)
        val tvSand = findViewById<TextView>(R.id.tvSand)
        val tvArea = findViewById<TextView>(R.id.tvArea)
        val tvVolume = findViewById<TextView>(R.id.tvVolume)
        val tvTotal = findViewById<TextView>(R.id.tvTotal)

        val btnCalc = findViewById<Button>(R.id.btnCalc)

        // ================= DATA =================
        val bricks = arrayOf("Standard Brick", "Hollow Brick", "Concrete Block")
        val units = arrayOf("Meter (m)", "Centimeter (cm)")
        val thicknessList = arrayOf("5 cm", "10 cm", "15 cm", "20 cm")
        val mortarList = arrayOf("1:3", "1:4", "1:5")

        // ================= ADAPTERS =================
        spinnerBrick.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, bricks)
        spinnerUnit.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, units)
        spinnerThickness.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, thicknessList)
        spinnerMortar.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, mortarList)

        // ================= IMAGE CHANGE FIX =================
        spinnerBrick.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {

                when (bricks[position]) {
                    "Standard Brick" -> imgBrick.setImageResource(R.drawable.brick_standard)
                    "Hollow Brick" -> imgBrick.setImageResource(R.drawable.brick_hollow)
                    "Concrete Block" -> imgBrick.setImageResource(R.drawable.brick_block)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // ================= CALC =================
        btnCalc.setOnClickListener {

            var L = etLength.text.toString().toDoubleOrNull()
            var H = etHeight.text.toString().toDoubleOrNull()

            val doors = etDoors.text.toString().toDoubleOrNull() ?: 0.0
            val windows = etWindows.text.toString().toDoubleOrNull() ?: 0.0

            if (L == null || H == null) {
                Toast.makeText(this, "أدخل القيم", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // UNIT
            if (spinnerUnit.selectedItem.toString() == "Centimeter (cm)") {
                L /= 100
                H /= 100
            }

            // AREA (net)
            val grossArea = L * H
            val netArea = grossArea - (doors + windows)

            // BRICK TYPE
            val (l, h, pricePer1000) = when (spinnerBrick.selectedItem.toString()) {
                "Standard Brick" -> Triple(0.26, 0.07, 2600.0)
                "Hollow Brick" -> Triple(0.40, 0.20, 3000.0)
                "Concrete Block" -> Triple(0.40, 0.20, 2800.0)
                else -> Triple(0.26, 0.07, 2600.0)
            }

            val joint = 0.01
            val brickArea = (l + joint) * (h + joint)

            var bricksCount = netArea / brickArea
            bricksCount *= 1.05 // waste 5%
            val finalBricks = ceil(bricksCount).toInt()

            // THICKNESS
            val thickness = when (spinnerThickness.selectedItem.toString()) {
                "5 cm" -> 0.05
                "10 cm" -> 0.10
                "15 cm" -> 0.15
                "20 cm" -> 0.20
                else -> 0.10
            }

            val volume = netArea * thickness

            // ================= MORTAR FIX (IMPORTANT) =================
            val mortarRatio = when (spinnerMortar.selectedItem.toString()) {
                "1:3" -> 0.30
                "1:4" -> 0.25
                "1:5" -> 0.20
                else -> 0.25
            }

            val mortarVolume = volume * mortarRatio

            val cementBags = mortarVolume * 6.5
            val sand = mortarVolume * 0.8

            // COST
            val brickCost = (finalBricks / 1000.0) * pricePer1000
            val cementCost = cementBags * 500
            val sandCost = sand * 1200

            val totalCost = brickCost + cementCost + sandCost

            // ================= OUTPUT =================
            tvBrick.text = "$finalBricks pcs"
            tvArea.text = "%.2f m²".format(netArea)
            tvVolume.text = "%.2f m³".format(volume)
            tvCement.text = "%.1f bags".format(cementBags)
            tvSand.text = "%.2f m³".format(sand)
            tvTotal.text = "%.0f DA".format(totalCost)
        }
    }
}
