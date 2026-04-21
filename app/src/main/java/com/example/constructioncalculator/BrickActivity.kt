package com.example.constructioncalculator

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.ceil

class BrickActivity : AppCompatActivity() {

    data class BrickType(
        val name: String,
        val lengthCm: Double,
        val heightCm: Double,
        val imageRes: Int
    )

    lateinit var bricks: List<BrickType>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_brick)

        val wallW = findViewById<EditText>(R.id.wallWidth)
        val wallH = findViewById<EditText>(R.id.wallHeight)
        val openings = findViewById<EditText>(R.id.openings)
        val price = findViewById<EditText>(R.id.price)

        val wallImage = findViewById<ImageView>(R.id.wallimage)
        val brickImage = findViewById<ImageView>(R.id.brickImage)

        val spinnerBrick = findViewById<Spinner>(R.id.spinnerBrick)
        val spinnerThickness = findViewById<Spinner>(R.id.spinnerThickness)
        val spinnerRatio = findViewById<Spinner>(R.id.spinnerRatio)

        val result = findViewById<TextView>(R.id.result)
        val btn = findViewById<Button>(R.id.btnCalc)

        // 🧱 BRICKS (الأبعاد الحقيقية 40×20)
        bricks = listOf(
            BrickType("8 Holes (8 cm)", 40.0, 20.0, R.drawable.brick8),
            BrickType("12 Holes (12 cm)", 40.0, 20.0, R.drawable.brick12),
            BrickType("15 Holes (15 cm)", 40.0, 20.0, R.drawable.brick15)
        )

        spinnerBrick.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            bricks.map { it.name }
        )

        spinnerThickness.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            arrayOf("5 cm", "10 cm", "15 cm", "20 cm")
        )

        spinnerRatio.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            arrayOf("1:3", "1:4", "1:5")
        )

        wallImage.setImageResource(R.drawable.wall)

        spinnerBrick.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    brickImage.setImageResource(bricks[position].imageRes)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

        btn.setOnClickListener {

            // 📐 Inputs
            val w = wallW.text.toString().toDoubleOrNull() ?: 0.0
            val h = wallH.text.toString().toDoubleOrNull() ?: 0.0
            val o = openings.text.toString().toDoubleOrNull() ?: 0.0
            val p = price.text.toString().toDoubleOrNull() ?: 0.0

            val brick = bricks[spinnerBrick.selectedItemPosition]

            // 📊 Wall area
            val area = (w * h) - o

            // 🧱 REAL brick size with mortar joints (1cm)
            val L = (brick.lengthCm + 1) / 100
            val H = (brick.heightCm + 1) / 100

            val bricksPerM2 = 1 / (L * H)

            // 📏 Thickness factor (realistic approximation)
            val thickness = spinnerThickness.selectedItem.toString()
                .replace(" cm", "")
                .toInt()

            val factor = when (thickness) {
                5 -> 0.8
                10 -> 1.0
                15 -> 1.3
                20 -> 1.6
                else -> 1.0
            }

            // 🧱 Total bricks
            val totalBricks = ceil(area * bricksPerM2 * factor).toInt()

            // 💰 Cost
            val cost = totalBricks * p

            // 🧪 Mortar volume (realistic)
            val mortar = area * 0.023
            // 🧪 Ratio
            val ratio = spinnerRatio.selectedItem.toString().split(":")
            val cementPart = ratio[0].toDouble()
            val sandPart = ratio[1].toDouble()
            val totalParts = cementPart + sandPart

            val cement = (mortar * cementPart / totalParts) / 0.035
            val sand = mortar * sandPart / totalParts

            // 📊 Result
            result.text =
                "=== RESULT ===\n" +
                        "Area: ${"%.2f".format(area)} m²\n" +
                        "Bricks: $totalBricks pcs\n" +
                        "Brick: ${brick.name}\n" +
                        "Ratio: ${spinnerRatio.selectedItem}\n" +
                        "Cement: ${"%.2f".format(cement)} bags\n" +
                        "Sand: ${"%.2f".format(sand)} m³\n" +
                        "Cost: ${"%.2f".format(cost)} DZD"
        }
    }
}