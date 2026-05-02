package com.example.constructioncalculator

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil

class TilesActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    data class TilePreset(
        val name: String,
        val lengthCm: Double,
        val widthCm: Double,
        val wastePct: Double,
        val drawableId: Int
    )

    private val tilePresets = listOf(
        TilePreset("Ceramic 30×30 cm",    30.0, 30.0, 0.05, R.drawable.tile_ceramic),
        TilePreset("Porcelain 40×40 cm",  40.0, 40.0, 0.07, R.drawable.tile_porcelain),
        TilePreset("Porcelain 60×60 cm",  60.0, 60.0, 0.08, R.drawable.tile_porcelain),
        TilePreset("Porcelain 80×80 cm",  80.0, 80.0, 0.10, R.drawable.tile_porcelain),
        TilePreset("Marble 30×60 cm",     30.0, 60.0, 0.10, R.drawable.tile_marble),
        TilePreset("Granite 60×60 cm",    60.0, 60.0, 0.10, R.drawable.tile_granite),
        TilePreset("Mosaic 20×20 cm",     20.0, 20.0, 0.15, R.drawable.tile_mosaic),
        TilePreset("Custom (manual input)", 0.0, 0.0, 0.08, R.drawable.tile_ceramic)
    )

    private val installTypes = arrayOf(
        "Straight (Droit)   — +5% waste",
        "Diagonal 45°       — +12% waste",
        "Offset (Décalé)    — +8% waste"
    )
    private val installWaste = doubleArrayOf(0.05, 0.12, 0.08)

    // ── Locale-safe formatter ──
    private fun Double.f(d: Int = 2) = String.format(Locale.US, "%.${d}f", this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tiles)

        db = DatabaseHelper(this)

        val img       = findViewById<ImageView>(R.id.imgTiles)
        val spType    = findViewById<Spinner>(R.id.spTileType)
        val spInstall = findViewById<Spinner>(R.id.spInstallType)
        val etFL      = findViewById<EditText>(R.id.etFloorL)
        val etFW      = findViewById<EditText>(R.id.etFloorW)
        val etTL      = findViewById<EditText>(R.id.etTileL)
        val etTW      = findViewById<EditText>(R.id.etTileW)
        val tvResult  = findViewById<TextView>(R.id.tvTilesResult)
        val btnCalc   = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCalc)
        val btnHistory = findViewById<Button>(R.id.btnHistory)

        // ── Spinners ──
        spType.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item,
            tilePresets.map { it.name })

        spInstall.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item, installTypes)

        spType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                val preset = tilePresets[pos]
                img.setImageResource(preset.drawableId)
                if (preset.lengthCm > 0) {
                    etTL.setText(preset.lengthCm.toInt().toString())
                    etTW.setText(preset.widthCm.toInt().toString())
                    etTL.isEnabled = false
                    etTW.isEnabled = false
                } else {
                    etTL.setText(""); etTW.setText("")
                    etTL.isEnabled = true; etTW.isEnabled = true
                }
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        // ════════════════════════════════════════════════════════════════
        //  CALCULATE
        // ════════════════════════════════════════════════════════════════
        btnCalc.setOnClickListener {

            val floorL = etFL.text.toString().toDoubleOrNull()
            val floorW = etFW.text.toString().toDoubleOrNull()
            val tileL  = etTL.text.toString().toDoubleOrNull()
            val tileW  = etTW.text.toString().toDoubleOrNull()
            if (floorL == null  ||floorW == null
                ||  tileL  == null || tileW  == null
                ||  floorL <= 0 || floorW <= 0
                || tileL  <= 0 || tileW  <= 0) {
            Toast.makeText(this,
                "⚠️ Please fill all fields with valid values",
                Toast.LENGTH_SHORT).show()
            return@setOnClickListener
        }

            val pos          = spType.selectedItemPosition
            val preset       = tilePresets[pos]
            val installPos   = spInstall.selectedItemPosition
            val installExtra = installWaste.getOrElse(installPos) { 0.05 }
            val totalWastePct = preset.wastePct + installExtra

            val date = SimpleDateFormat("dd/MM/yyyy HH:mm",
                Locale.getDefault()).format(Date())

            // ── Step 1 — Floor Area ──
            val floorArea = floorL * floorW

            // ── Step 2 — Tile Area ──
            val tileLm    = tileL / 100.0
            val tileWm    = tileW / 100.0
            val tileAreaM2 = tileLm * tileWm

            // ── Step 3 — Base count (no waste) ──
            val baseTiles  = floorArea / tileAreaM2

            // ── Step 4 — Total with waste ──
            val totalTiles = ceil(baseTiles * (1.0 + totalWastePct)).toInt()

            // ── Step 5 — Layout ──
            val tilesAlongL = ceil(floorL / tileLm).toInt()
            val tilesAlongW = ceil(floorW / tileWm).toInt()

            // ── Step 6 — Boxes ──
            val tilesPerBox = when {
                tileL >= 80 -> 4
                tileL >= 60 -> 4
                tileL >= 40 -> 6
                else        -> 9
            }
            val boxesNeeded = ceil(totalTiles.toDouble() / tilesPerBox).toInt()

            // ── Room check ──
            val roomCheck = when {
                floorArea >= 22 -> "✅ Suitable for living room (≥ 22 m²)"
                floorArea >= 12 -> "✅ Suitable for bedroom (≥ 12 m²)"
                floorArea >= 8  -> "ℹ️ Suitable for bathroom / small kitchen"
                else            -> "ℹ️ Small area (< 8 m²)"
            }

            // ── Build result (step-by-step like Paint) ──
            val sb = StringBuilder()
            sb.append("🧱  Tile Calculator\n")
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            sb.append("📥  Inputs\n")
            sb.append("    Tile Type        : ${preset.name}\n")
            sb.append("    Install Pattern  : ${installTypes[installPos]}\n")
            sb.append("    Floor Length (L) : ${floorL.f(2)} m\n")
            sb.append("    Floor Width  (W) : ${floorW.f(2)} m\n")
            sb.append("    Tile Size        : ${tileL.toInt()} × ${tileW.toInt()} cm\n")
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            sb.append("📐  Step 1 — Floor Area\n")
            sb.append("    A = L × W = ${floorL.f(2)} × ${floorW.f(2)}\n")
            sb.append("    A = ${floorArea.f(4)} m²\n")
            sb.append("    $roomCheck\n")
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            sb.append("🧩  Step 2 — Tile Area\n")
            sb.append("    t = ${tileL.toInt()} cm ÷ 100 = ${tileLm.f(2)} m\n")
            sb.append("    w = ${tileW.toInt()} cm ÷ 100 = ${tileWm.f(2)} m\n")
            sb.append("    Tile area = ${tileLm.f(2)} × ${tileWm.f(2)}\n")
            sb.append("             = ${tileAreaM2.f(4)} m²\n")
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            sb.append("🔢  Step 3 — Base Count (no waste)\n")
            sb.append("    N = Floor area ÷ Tile area\n")
            sb.append("    N = ${floorArea.f(4)} ÷ ${tileAreaM2.f(4)}\n")
            sb.append("    N = ${baseTiles.f(2)}  →  ${ceil(baseTiles).toInt()} tiles\n")
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            sb.append("♻️  Step 4 — Waste Factor\n")
            sb.append("    Tile waste   : ${(preset.wastePct * 100).toInt()}%\n")
            sb.append("    Install waste: ${(installExtra * 100).toInt()}%\n")
            sb.append("    Total waste  : ${(totalWastePct * 100).toInt()}%\n")
            sb.append("    Total = ${ceil(baseTiles).toInt()} × (1 + ${(totalWastePct * 100).toInt()}%)\n")
            sb.append("          = ${ceil(baseTiles).toInt()} × ${(1.0 + totalWastePct).f(2)}\n")
            sb.append("          = $totalTiles tiles\n")
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            sb.append("📊  Step 5 — Layout\n")
            sb.append("    Along L : ceil(${floorL.f(2)} ÷ ${tileLm.f(2)}) = $tilesAlongL tiles\n")
            sb.append("    Along W : ceil(${floorW.f(2)} ÷ ${tileWm.f(2)}) = $tilesAlongW tiles\n")
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            sb.append("📦  Step 6 — Boxes\n")
            sb.append("    Tiles per box : $tilesPerBox\n")
            sb.append("    Boxes needed  : ceil($totalTiles ÷ $tilesPerBox)\n")
            sb.append("                  = $boxesNeeded boxes\n")
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            sb.append("✅  TOTAL TILES  = $totalTiles tiles\n")
            sb.append("📦  TOTAL BOXES  = $boxesNeeded boxes\n")
            sb.append("💾  Saved ($date)")

            tvResult.text = sb.toString()

            // ── Save ──
            db.insertTileHistory(
                tileType    = preset.name,
                floorArea   = floorArea,
                tileLcm     = tileL,
                tileWcm     = tileW,
                baseTiles   = ceil(baseTiles).toInt(),
                totalTiles  = totalTiles,
                wastePct    = totalWastePct * 100,
                installType = installTypes[installPos],
                date        = date
            )
            Toast.makeText(this, "✅ Saved to history", Toast.LENGTH_SHORT).show()
        }

        // ════════════════════════════════════════════════════════════════
        //  HISTORY — AlertDialog like Paint
        // ════════════════════════════════════════════════════════════════
        btnHistory.setOnClickListener {
            val records = db.getAllTileHistory()
            if (records.isEmpty()) {
                Toast.makeText(this, "No calculations yet", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sb = StringBuilder()
            records.forEachIndexed { i, h ->
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━\n")
                sb.append("#${i + 1}  📅 ${h["date"]}\n")
                sb.append("Tile Type   : ${h["tile_type"]}\n")
                sb.append("Install     : ${h["install_type"]}\n")
                sb.append("Floor Area  : ${h["floor_area"]} m²\n")
                sb.append("Tile Size   : ${h["tile_l_cm"]} × ${h["tile_w_cm"]} cm\n")
                sb.append("Base Count  : ${h["base_tiles"]} tiles\n")
                sb.append("Total       : ${h["total_tiles"]} tiles\n")
                sb.append("Waste       : ${h["waste_pct"]}%\n")
            }

            AlertDialog.Builder(this)
                .setTitle("📜 Tiles History")
                .setMessage(sb.toString())
                .setPositiveButton("OK", null)
                .setNegativeButton("🗑 Clear History") { _, _ ->
                    db.clearTileHistory()
                    Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show()
                }
                .show()
        }
    }
}