package com.example.constructioncalculator

import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class PlasterActivity : AppCompatActivity() {

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

    companion object {
        const val DRY_VOLUME_FACTOR     = 1.33
        const val CEMENT_DENSITY_KG_M3  = 1500.0
        const val BAG_WEIGHT_KG         = 50.0
        const val WASTE_FACTOR          = 1.05
    }

    private val walls       = mutableListOf<Wall>()
    private var modifyIndex = -1
    private val tempDoors   = mutableListOf<Opening>()
    private val tempWindows = mutableListOf<Opening>()
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plaster)

        db = DatabaseHelper(this)

        // ── Views ───────────────────────────────────────────────────────
        val etLength         = findViewById<EditText>(R.id.etLength)
        val etHeight         = findViewById<EditText>(R.id.etHeight)
        val etDoorL          = findViewById<EditText>(R.id.etDoorL)
        val etDoorW          = findViewById<EditText>(R.id.etDoorW)
        val etWinL           = findViewById<EditText>(R.id.etWindowL)
        val etWinW           = findViewById<EditText>(R.id.etWindowW)
        val spinnerThickness = findViewById<Spinner>(R.id.spinnerThickness)
        val spinnerMortar    = findViewById<Spinner>(R.id.spinnerMortar)
        val btnAddDoor       = findViewById<Button>(R.id.btnAddDoor)
        val btnAddWindow     = findViewById<Button>(R.id.btnAddWindow)
        val btnAddWall       = findViewById<Button>(R.id.btnAddWall)
        val btnModify        = findViewById<Button>(R.id.btnModifyWall)
        val btnDelete        = findViewById<Button>(R.id.btnDeleteWall)
        val btnReset         = findViewById<Button>(R.id.btnReset)
        val btnCalc          = findViewById<Button>(R.id.btnCalcPlaster)
        val btnHistory       = findViewById<Button>(R.id.btnHistory)
        val tvDoors          = findViewById<TextView>(R.id.tvDoors)
        val tvWindows        = findViewById<TextView>(R.id.tvWindows)
        val tvWalls          = findViewById<TextView>(R.id.tvWalls)
        val tvArea           = findViewById<TextView>(R.id.tvArea)
        val tvVolume         = findViewById<TextView>(R.id.tvVolume)
        val tvCement         = findViewById<TextView>(R.id.tvCement)
        val tvSand           = findViewById<TextView>(R.id.tvSand)

        // ── Spinners ────────────────────────────────────────────────────
        spinnerThickness.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item,
            arrayOf(
                "Ceiling (6 mm)",
                "Internal Wall 1st coat (12 mm)",
                "Internal Wall 2nd coat (15 mm)",
                "External Wall (20 mm)"
            )
        )
        spinnerMortar.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item,
            arrayOf("1:3", "1:4", "1:5", "1:6")
        )

        // ── Add Door ────────────────────────────────────────────────────
         btnAddDoor.setOnClickListener {
            val l = etDoorL.text.toString().toDoubleOrNull()
            val w = etDoorW.text.toString().toDoubleOrNull()
            if (l == null || w == null || l <= 0 || w <= 0) {
            Toast.makeText(this, "Enter valid door dimensions", Toast.LENGTH_SHORT).show()
            return@setOnClickListener
        }
            tempDoors.add(Opening(l, w))
            etDoorL.text.clear(); etDoorW.text.clear()
            updateOpeningsDisplay(tvDoors, tvWindows)
        }

        // ── Add Window ──────────────────────────────────────────────────
        btnAddWindow.setOnClickListener {
            val l = etWinL.text.toString().toDoubleOrNull()
            val w = etWinW.text.toString().toDoubleOrNull()
            if (l == null || w == null || l <= 0 || w <= 0) {
            Toast.makeText(this, "Enter valid window dimensions", Toast.LENGTH_SHORT).show()
            return@setOnClickListener
        }
            tempWindows.add(Opening(l, w))
            etWinL.text.clear(); etWinW.text.clear()
            updateOpeningsDisplay(tvDoors, tvWindows)
        }

        // ── Add / Save Wall ─────────────────────────────────────────────
        btnAddWall.setOnClickListener {
            val length = etLength.text.toString().toDoubleOrNull()
            val height = etHeight.text.toString().toDoubleOrNull()
            if (length == null || length <= 0 || height == null || height <= 0) {
            Toast.makeText(this, "Enter valid wall Length and Height", Toast.LENGTH_SHORT).show()
            return@setOnClickListener
        }
            val openingsArea = tempDoors.sumOf { it.area } + tempWindows.sumOf { it.area }
            if (openingsArea >= length * height) {
                Toast.makeText(
                    this,
                    "⚠️ Openings area (%.2f m²) ≥ Wall area (%.2f m²)".format(openingsArea, length * height),
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
            clearInputs(etLength, etHeight, etDoorL, etDoorW, etWinL, etWinW)
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
                    etLength.setText(w.length.toString())
                    etHeight.setText(w.height.toString())
                    etDoorL.text.clear(); etDoorW.text.clear()
                    etWinL.text.clear();  etWinW.text.clear()
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
                        clearInputs(etLength, etHeight, etDoorL, etDoorW, etWinL, etWinW)
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
            clearInputs(etLength, etHeight, etDoorL, etDoorW, etWinL, etWinW)
            updateWallsDisplay(tvWalls); updateOpeningsDisplay(tvDoors, tvWindows)
            tvArea.text = ""; tvVolume.text = ""; tvCement.text = ""; tvSand.text = ""
        }

        // ═══════════════════════════════════════════════════════════════
        // CALCULATE
        // ═══════════════════════════════════════════════════════════════
        btnCalc.setOnClickListener {
            if (walls.isEmpty()) {
                Toast.makeText(this, "Add at least one wall", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // STEP 1 — Total Net Area
            val totalArea = walls.sumOf { it.netArea }

            // STEP 2 — Thickness
            val thicknessStr = spinnerThickness.selectedItem.toString()
            val thickness: Double = when {
                thicknessStr.contains("6 mm")  -> 0.006
                thicknessStr.contains("12 mm") -> 0.012
                thicknessStr.contains("15 mm") -> 0.015
                thicknessStr.contains("20 mm") -> 0.020
                else                           -> 0.012
            }

            // STEP 3 — Wet Volume
            val wetVolume = totalArea * thickness

            // STEP 4 — Dry Volume
            val dryVolume = wetVolume * DRY_VOLUME_FACTOR

            // STEP 5 — Parse Mortar Ratio
            val mortarStr  = spinnerMortar.selectedItem.toString()
            val parts      = mortarStr.split(":")
            val cParts     = parts[0].trim().toDouble()
            val sParts     = parts[1].trim().toDouble()
            val totalParts = cParts + sParts

            // STEP 6 — Cement & Sand Volumes
            val cementVol = dryVolume * (cParts / totalParts)
            val sandVol   = dryVolume * (sParts / totalParts)

            // STEP 7 — Cement Bags
            val cementMass = cementVol * CEMENT_DENSITY_KG_M3
            val cementBags = cementMass / BAG_WEIGHT_KG

            // STEP 8 — Add 5% Wastage
            val cementBagsFinal = cementBags * WASTE_FACTOR
            val sandVolFinal    = sandVol    * WASTE_FACTOR

            // STEP 9 — Display
            tvArea.text = buildString {
                append("🧱  Walls: ${walls.size}\n")
                walls.forEachIndexed { i, w ->
                    append("  W${i+1}: ${w.length}×${w.height} m  →  Net: %.3f m²\n".format(w.netArea))
                }
                append("📐  Total Net Area = %.4f m²".format(totalArea))
            }
            tvVolume.text = buildString {
                append("💧 Wet Volume\n")
                append("   = Area × Thickness\n")
                append("   = %.4f × %.3f\n".format(totalArea, thickness))
                append("   = %.5f m³\n\n".format(wetVolume))
                append("🏗  Dry Volume\n")
                append("   = Wet Volume × 1.33\n")
                append("   = %.5f × 1.33\n".format(wetVolume))
                append("   = %.5f m³".format(dryVolume))
            }
            tvCement.text = buildString {
                append("🪣  Mortar Ratio: $mortarStr  (parts: %.0f+%.0f=%.0f)\n\n".format(cParts, sParts, totalParts))
                append("Cement Volume\n")
                append("   = %.5f × (%.0f/%.0f)\n".format(dryVolume, cParts, totalParts))
                append("   = %.5f m³\n\n".format(cementVol))
                append("Cement Mass\n")
                append("   = %.5f × 1500 kg/m³\n".format(cementVol))
                append("   = %.3f kg\n\n".format(cementMass))
                append("Bags (50 kg each)\n")
                append("   = %.3f ÷ 50\n".format(cementMass))
                append("   = %.2f bags\n\n".format(cementBags))
                append("+ 5% Wastage\n")
                append("   = %.2f × 1.05\n".format(cementBags))
                append("   ✅ = %.2f bags".format(cementBagsFinal))
            }
            tvSand.text = buildString {
                append("🏖  Sand Volume\n")
                append("   = %.5f × (%.0f/%.0f)\n".format(dryVolume, sParts, totalParts))
                append("   = %.5f m³\n\n".format(sandVol))
                append("+ 5% Wastage\n")
                append("   = %.5f × 1.05\n".format(sandVol))
                append("   ✅ = %.5f m³".format(sandVolFinal))
            }

            // STEP 10 — Save to DB
            val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            db.insertPlasterHistory(
                surface    = "Walls: ${walls.size}",
                area       = totalArea,
                thickness  = thickness * 1000, // تحويل لـ mm
                ratio      = mortarStr,
                cementBags = cementBagsFinal,
                sandM3     = sandVolFinal,
                waterL     = 0.0,
                volumeM3   = wetVolume,
                coats      = 1,
                date       = dateStr
            )
            Toast.makeText(this, "✅ Saved to history", Toast.LENGTH_SHORT).show()
        }

        // ── History ─────────────────────────────────────────────────────
        btnHistory.setOnClickListener {
            val records = db.getAllPlasterHistory()
            if (records.isEmpty()) {
                Toast.makeText(this, "No calculations yet", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val sb = StringBuilder()
            records.forEachIndexed { i, h ->
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━\n")
                sb.append("#${i + 1}  📅 ${h["date"]}\n")
                sb.append("Surface  : ${h["surface"]}\n")
                sb.append("Area     : ${h["area"]} m²\n")
                sb.append("Thickness: ${h["thickness"]} mm\n")
                sb.append("Ratio    : ${h["ratio"]}\n")
                sb.append("Cement   : ${"%.2f".format(h["cement_bags"]?.toDoubleOrNull() ?: 0.0)} bags\n")
                sb.append("Sand     : ${"%.5f".format(h["sand_m3"]?.toDoubleOrNull() ?: 0.0)} m³\n")
            }
            AlertDialog.Builder(this)
                .setTitle("📜 Calculation History")
                .setMessage(sb.toString())
                .setPositiveButton("OK", null)
                .setNegativeButton("🗑 Clear History") { _, _ ->
                    db.clearPlasterHistory()
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
            "W${i+1}: ${w.length}×${w.height}m$d$win  →  Net: %.3f m²".format(w.netArea)
        }.joinToString("\n")
    }

    private fun updateOpeningsDisplay(tvDoors: TextView, tvWindows: TextView) {
        tvDoors.text = if (tempDoors.isEmpty()) "Doors: none"
        else tempDoors.mapIndexed { i, d ->
            "D${i+1}: ${d.length}×${d.width}=%.2fm²".format(d.area)
        }.joinToString("  ", "🚪 ")

        tvWindows.text = if (tempWindows.isEmpty()) "Windows: none"
        else tempWindows.mapIndexed { i, w ->
            "W${i+1}: ${w.length}×${w.width}=%.2fm²".format(w.area)
        }.joinToString("  ", "🪟 ")
    }

    override fun onDestroy() {
        super.onDestroy()
        db.close()
    }
}