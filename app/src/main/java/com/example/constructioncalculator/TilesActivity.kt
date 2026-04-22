package com.example.constructioncalculator

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class TilesActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tiles)

        db = DatabaseHelper(this)

        val img = findViewById<ImageView>(R.id.imgTiles)
        val spType = findViewById<Spinner>(R.id.spTileType)

        val etFL = findViewById<EditText>(R.id.etFloorL)
        val etFW = findViewById<EditText>(R.id.etFloorW)

        val etTL = findViewById<EditText>(R.id.etTileL)
        val etTW = findViewById<EditText>(R.id.etTileW)

        val tv = findViewById<TextView>(R.id.tvTilesResult)

        val types = arrayOf(
            "Ceramic (Small)",
            "Porcelain (Medium)",
            "Marble (Large)",
            "Granite",
            "Mosaic"
        )

        spType.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            types
        )

        // 🖼 IMAGE CHANGE
        spType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {

                val image = when (position) {
                    0 -> R.drawable.tile_ceramic
                    1 -> R.drawable.tile_porcelain
                    2 -> R.drawable.tile_marble
                    3 -> R.drawable.tile_granite
                    4 -> R.drawable.tile_mosaic
                    else -> R.drawable.tile_ceramic
                }

                img.setImageResource(image)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // 🧮 CALCULATE BUTTON
        findViewById<Button>(R.id.btnCalcTiles).setOnClickListener {

            val floorL = etFL.text.toString().toDoubleOrNull() ?: 0.0
            val floorW = etFW.text.toString().toDoubleOrNull() ?: 0.0

            val tileL = etTL.text.toString().toDoubleOrNull() ?: 1.0
            val tileW = etTW.text.toString().toDoubleOrNull() ?: 1.0

            val floorArea = floorL * floorW

            val tileArea = (tileL / 100.0) * (tileW / 100.0)

            val baseTiles = if (tileArea > 0) floorArea / tileArea else 0.0

            val waste = when (spType.selectedItemPosition) {
                0 -> 1.05
                1 -> 1.08
                2 -> 1.12
                3 -> 1.10
                4 -> 1.15
                else -> 1.10
            }

            val totalTiles = baseTiles * waste

            // 📅 DATE
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // 💾 SAVE SQLITE
            db.insertTile(
                spType.selectedItem.toString(),
                floorArea,
                totalTiles,
                date
            )

            // 📊 RESULT
            tv.text = """
                🧱 Floor Area: %.2f m²
                🧩 Tiles Needed: %.0f pcs
                ➕ With Waste: %.0f pcs
                ✔ Saved
            """.trimIndent().format(floorArea, baseTiles, totalTiles)
        }
    }
}