package com.example.constructioncalculator

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class BrickActivity : AppCompatActivity() {

    data class Opening(val length: Double, val width: Double) {
        val area: Double get() = length * width
    }

    data class Wall(
        val length: Double,
        val height: Double,
        val doors: List<Opening>,
        val windows: List<Opening>
    ) {
        val grossArea: Double  get() = length * height
        val doorArea: Double   get() = doors.sumOf { it.area }
        val windowArea: Double get() = windows.sumOf { it.area }
        val netArea: Double    get() = (grossArea - doorArea - windowArea).coerceAtLeast(0.0)
    }

    data class BrickType(
        val name: String,
        val lengthCm: Double,
        val heightCm: Double,
        val thicknessCm: Double,
        val imageRes: Int
    )

    companion object {
        const val MORTAR_JOINT_CM        = 1.5
        const val BRICK_WASTE            = 1.05
        const val MORTAR_RATIO_OF_WALL   = 0.30
        const val DRY_VOLUME_FACTOR      = 1.33
        const val CEMENT_DENSITY         = 1500.0
        const val MORTAR_WASTE           = 1.10
    }

    private val walls       = mutableListOf<Wall>()
    private var modifyIndex = -1
    private val tempDoors   = mutableListOf<Opening>()
    private val tempWindows = mutableListOf<Opening>()
    private lateinit var db: DatabaseHelper
    private lateinit var bricks: List<BrickType>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_brick)

        db = DatabaseHelper(this)

        // ── Brick types (Algeria standard) ─────────────────────────────
        bricks = listOf(
            BrickType("Brique 8 trous  (40×20×8 cm)",  40.0, 20.0,  8.0, R.drawable.brick8),
            BrickType("Brique 12 trous (40×20×12 cm)", 40.0, 20.0, 12.0, R.drawable.brick12),
            BrickType("Brique 15 trous (40×20×15 cm)", 40.0, 20.0, 15.0, R.drawable.brick15),
            BrickType("Brique 20 trous (40×20×20 cm)", 40.0, 20.0, 20.0, R.drawable.brick8),
            BrickType("Parpaing 20     (40×20×20 cm)", 40.0, 20.0, 20.0, R.drawable.brick12)
        )

        // ── Views ───────────────────────────────────────────────────────
        val imgWall      = findViewById<ImageView>(R.id.wallimage)
        val imgBrick     = findViewById<ImageView>(R.id.brickImage)
        val spBrick      = findViewById<Spinner>(R.id.spinnerBrick)
        val spLayers     = findViewById<Spinner>(R.id.spinnerThickness)
        val spRatio      = findViewById<Spinner>(R.id.spinnerRatio)
        val etL          = findViewById<EditText>(R.id.etLength)
        val etH          = findViewById<EditText>(R.id.etHeight)
        val etDL         = findViewById<EditText>(R.id.etDoorL)
        val etDW         = findViewById<EditText>(R.id.etDoorW)
        val etWL         = findViewById<EditText>(R.id.etWindowL)
        val etWW         = findViewById<EditText>(R.id.etWindowW)
        val etPrice      = findViewById<EditText>(R.id.price)
        val tvWalls      = findViewById<TextView>(R.id.tvWalls)
        val tvDoors      = findViewById<TextView>(R.id.tvDoors)
        val tvWindows    = findViewById<TextView>(R.id.tvWindows)
        val tvResult     = findViewById<TextView>(R.id.result)
        val btnAddDoor   = findViewById<Button>(R.id.btnAddDoor)
        val btnAddWindow = findViewById<Button>(R.id.btnAddWindow)
        val btnAddWall   = findViewById<Button>(R.id.btnAddWall)
        val btnModify    = findViewById<Button>(R.id.btnModifyWall)
        val btnDelete    = findViewById<Button>(R.id.btnDeleteWall)
        val btnReset     = findViewById<Button>(R.id.btnReset)
        val btnCalc      = findViewById<Button>(R.id.btnCalc)
        val btnHistory   = findViewById<Button>(R.id.btnHistory)
        // ── Spinners ────────────────────────────────────────────────────
        spBrick.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item,
            bricks.map { it.name }
        )
        spLayers.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item,
            arrayOf(
                "نصف طوبة (Half brick)",
                "طوبة واحدة (Single brick)",
                "طوبتان (Double brick)"
            )
        )
        spRatio.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item,
            arrayOf("1:3", "1:4", "1:5", "1:6")
        )

        imgWall.setImageResource(R.drawable.wall)

        spBrick.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>, v: View?, pos: Int, id: Long) {
                imgBrick.setImageResource(bricks[pos].imageRes)
            }
            override fun onNothingSelected(p: AdapterView<*>) {}
        }

        // ── Add Door ────────────────────────────────────────────────────
        btnAddDoor.setOnClickListener {
            val l = etDL.text.toString().toDoubleOrNull()
            val w = etDW.text.toString().toDoubleOrNull()
            if (l == null ||w == null  ||l <= 0 || w <= 0) {
            Toast.makeText(this, "Enter valid door dimensions", Toast.LENGTH_SHORT).show()
            return@setOnClickListener
        }
            tempDoors.add(Opening(l, w))
            etDL.text.clear(); etDW.text.clear()
            updateOpeningsDisplay(tvDoors, tvWindows)
        }

        // ── Add Window ──────────────────────────────────────────────────
        btnAddWindow.setOnClickListener {
            val l = etWL.text.toString().toDoubleOrNull()
            val w = etWW.text.toString().toDoubleOrNull()
            if (l == null || w == null || l <= 0 || w <= 0) {
            Toast.makeText(this, "Enter valid window dimensions", Toast.LENGTH_SHORT).show()
            return@setOnClickListener
        }
            tempWindows.add(Opening(l, w))
            etWL.text.clear(); etWW.text.clear()
            updateOpeningsDisplay(tvDoors, tvWindows)
        }

        // ── Add / Save Wall ─────────────────────────────────────────────
        btnAddWall.setOnClickListener {
            val length = etL.text.toString().toDoubleOrNull()
            val height = etH.text.toString().toDoubleOrNull()
            if (length == null||  length <= 0 || height == null || height <= 0) {
            Toast.makeText(this, "Enter valid wall Length and Height", Toast.LENGTH_SHORT).show()
            return@setOnClickListener
        }
            val openingsArea = tempDoors.sumOf { it.area } + tempWindows.sumOf { it.area }
            if (openingsArea >= length * height) {
                Toast.makeText(
                    this,
                    "⚠️ Openings (%.2f m²) ≥ Wall (%.2f m²)".format(openingsArea, length * height),
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            val wall = Wall(length, height, tempDoors.toList(), tempWindows.toList())
            if (modifyIndex >= 0) {
                walls[modifyIndex] = wall
                modifyIndex = -1
                btnAddWall.text = "ADD WALL"
                Toast.makeText(this, "✅ Wall updated", Toast.LENGTH_SHORT).show()
            } else {
                walls.add(wall)
            }
            tempDoors.clear(); tempWindows.clear()
            clearInputs(etL, etH, etDL, etDW, etWL, etWW)
            updateOpeningsDisplay(tvDoors, tvWindows)
            updateWallsDisplay(tvWalls)
        }

// ── Modify Wall ─────────────────────────────────────────────────
        btnModify.setOnClickListener {
            if (walls.isEmpty()) {
                Toast.makeText(this, "No walls to modify", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            AlertDialog.Builder(this)
                .setTitle("✏️ Select Wall to Modify")
                .setItems(wallLabels()) { _, which ->
                    val w = walls[which]
                    etL.setText(w.length.toString())
                    etH.setText(w.height.toString())
                    etDL.text.clear(); etDW.text.clear()
                    etWL.text.clear(); etWW.text.clear()
                    tempDoors.clear();   tempDoors.addAll(w.doors)
                    tempWindows.clear(); tempWindows.addAll(w.windows)
                    updateOpeningsDisplay(tvDoors, tvWindows)
                    modifyIndex = which
                    btnAddWall.text = "💾 SAVE WALL ${which + 1}"
                    Toast.makeText(this, "Editing Wall ${which + 1}", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null).show()
        }

        // ── Delete Wall ─────────────────────────────────────────────────
        btnDelete.setOnClickListener {
            if (walls.isEmpty()) {
                Toast.makeText(this, "No walls to delete", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            AlertDialog.Builder(this)
                .setTitle("🗑 Select Wall to Delete")
                .setItems(wallLabels()) { _, which ->
                    walls.removeAt(which)
                    if (modifyIndex == which) {
                        modifyIndex = -1
                        btnAddWall.text = "ADD WALL"
                        tempDoors.clear(); tempWindows.clear()
                        clearInputs(etL, etH, etDL, etDW, etWL, etWW)
                        updateOpeningsDisplay(tvDoors, tvWindows)
                    }
                    updateWallsDisplay(tvWalls)
                    Toast.makeText(this, "Wall ${which + 1} deleted", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null).show()
        }

        // ── Reset ───────────────────────────────────────────────────────
        btnReset.setOnClickListener {
            walls.clear(); tempDoors.clear(); tempWindows.clear()
            modifyIndex = -1; btnAddWall.text = "ADD WALL"
            clearInputs(etL, etH, etDL, etDW, etWL, etWW)
            updateWallsDisplay(tvWalls)
            updateOpeningsDisplay(tvDoors, tvWindows)
            tvResult.text = ""
        }

        // ═══════════════════════════════════════════════════════════════
        // CALCULATE — ALGERIAN ENGINEERING STANDARD
        // ═══════════════════════════════════════════════════════════════
        btnCalc.setOnClickListener {
            if (walls.isEmpty()) {
                Toast.makeText(this, "Add at least one wall", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val brick    = bricks[spBrick.selectedItemPosition]
            val price    = etPrice.text.toString().toDoubleOrNull() ?: 0.0
            val layerStr = spLayers.selectedItem.toString()
            val ratioStr = spRatio.selectedItem.toString()

            // ── 1. Total net area ──────────────────────────────────────
            val totalNetArea = walls.sumOf { it.netArea }

            // ── 2. Brick calculation ───────────────────────────────────
            val lEff = (brick.lengthCm + MORTAR_JOINT_CM) / 100.0
            val hEff = (brick.heightCm + MORTAR_JOINT_CM) / 100.0
            val bricksPerM2 = 1.0 / (lEff * hEff)

            val layerMultiplier = when {
                layerStr.contains("Half")   -> 0.5
                layerStr.contains("Double") -> 2.0
                else                        -> 1.0
            }
            val totalBricks = (totalNetArea * bricksPerM2 * layerMultiplier * BRICK_WASTE).toInt()
            // ── 3. Mortar volume ───────────────────────────────────────
            val mortarVolume        = totalNetArea * MORTAR_RATIO_OF_WALL
            val dryMortar           = mortarVolume * DRY_VOLUME_FACTOR
            val dryMortarWithWaste  = dryMortar * MORTAR_WASTE

            // Read ratio from spinner e.g. "1:4" → sandPart = 4
            val sandPart   = ratioStr.removePrefix("1:").toIntOrNull() ?: 4
            val totalParts = 1 + sandPart

            val cementVolume = dryMortarWithWaste * (1.0 / totalParts)
            val sandVol      = dryMortarWithWaste * (sandPart.toDouble() / totalParts)
            val cementBags   = cementVolume * CEMENT_DENSITY / 50.0

            // ── 4. Cost ────────────────────────────────────────────────
            val cost = totalBricks * price

            // ── 5. Display result ──────────────────────────────────────
            val sb = StringBuilder()
            sb.append("📐 Net Area = %.3f m²\n".format(totalNetArea))
            sb.append("🧱 Bricks = $totalBricks pcs\n\n")
            sb.append("🧪 Mortar ($ratioStr)\n")
            sb.append("   Cement = %.2f bags\n".format(cementBags))
            sb.append("   Sand   = %.3f m³\n\n".format(sandVol))
            if (price > 0) sb.append("💰 Cost = %.2f DZD\n".format(cost))
            tvResult.text = sb.toString()

            // ── 6. Save to DB ──────────────────────────────────────────
            val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            db.insertBrickHistory(
                wallLength  = walls.first().length,
                wallHeight  = walls.first().height,
                wallThick   = brick.thicknessCm,
                brickL      = brick.lengthCm,
                brickH      = brick.heightCm,
                brickW      = brick.thicknessCm,
                mortarRatio = ratioStr,
                bricks      = totalBricks,
                cementBags  = cementBags,
                sandM3      = sandVol,
                wallArea    = totalNetArea,
                wallVolume  = mortarVolume,
                status      = layerStr,
                date        = dateStr
            )
            Toast.makeText(this, "✅ Saved to history", Toast.LENGTH_SHORT).show()
        }

        // ── History ─────────────────────────────────────────────────────
        btnHistory.setOnClickListener {
            val records = db.getAllBrickHistory()
            if (records.isEmpty()) {
                Toast.makeText(this, "No calculations yet", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sb = StringBuilder()
            records.forEachIndexed { i, h ->
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━\n")
                sb.append("#${i + 1}  📅 ${h["date"]}\n")
                sb.append("Area     : ${h["wall_area"]} m²\n")
                sb.append("Brick    : ${h["brick_l"]}×${h["brick_h"]}×${h["brick_w"]} cm\n")
                sb.append("Ratio    : ${h["mortar_ratio"]}\n")
                sb.append("Layers   : ${h["status"]}\n")
                sb.append("Bricks   : ${h["bricks"]} pcs\n")
                sb.append("Cement   : ${"%.2f".format(h["cement_bags"]?.toDoubleOrNull() ?: 0.0)} bags\n")
                sb.append("Sand     : ${"%.3f".format(h["sand_m3"]?.toDoubleOrNull() ?: 0.0)} m³\n")
            }

            AlertDialog.Builder(this)
                .setTitle("📜 Calculation History")
                .setMessage(sb.toString())
                .setPositiveButton("OK", null)
                .setNegativeButton("🗑 Clear History") { _, _ ->
                    db.clearBrickHistory()
                    Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show()
                }
                .show()
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private fun wallLabels() = walls.mapIndexed { i, w ->
        "Wall ${i + 1}: ${w.length}×${w.height} m  |  Net: %.3f m²".format(w.netArea)
    }.toTypedArray()
    private fun clearInputs(vararg fields: EditText) = fields.forEach { it.text.clear() }

    private fun updateWallsDisplay(tv: TextView) {
        tv.text = if (walls.isEmpty()) "Walls: none"
        else walls.mapIndexed { i, w ->
            val d   = if (w.doorArea   > 0) "  D:%.2fm²".format(w.doorArea)   else ""
            val win = if (w.windowArea > 0) "  W:%.2fm²".format(w.windowArea) else ""
            "W${i + 1}: ${w.length}×${w.height}m$d$win  →  Net: %.3f m²".format(w.netArea)
        }.joinToString("\n")
    }

    private fun updateOpeningsDisplay(tvDoors: TextView, tvWindows: TextView) {
        tvDoors.text = if (tempDoors.isEmpty()) "Doors: none"
        else tempDoors.mapIndexed { i, d ->
            "D${i + 1}: ${d.length}×${d.width}=%.2fm²".format(d.area)
        }.joinToString("  ", "🚪 ")

        tvWindows.text = if (tempWindows.isEmpty()) "Windows: none"
        else tempWindows.mapIndexed { i, w ->
            "W${i + 1}: ${w.length}×${w.width}=%.2fm²".format(w.area)
        }.joinToString("  ", "🪟 ")
    }
}