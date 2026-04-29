package com.example.constructioncalculator

import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class PlasterActivity : AppCompatActivity() {

    // ═══════════════════════════════════════════════════
    // 🧱 MODELS
    // ═══════════════════════════════════════════════════
    data class Opening(val length: Double, val width: Double) {
        val area: Double get() = length * width
    }

    data class Wall(
        val length: Double,
        val height: Double,
        val doors: List<Opening>,
        val windows: List<Opening>
    ) {
        val grossArea: Double   get() = length * height
        val doorArea: Double    get() = doors.sumOf { it.area }
        val windowArea: Double  get() = windows.sumOf { it.area }
        val netArea: Double     get() = (grossArea - doorArea - windowArea).coerceAtLeast(0.0)
    }

    data class HistoryEntry(
        val date: String,
        val wallCount: Int,
        val totalArea: Double,
        val thickness: String,
        val mortar: String,
        val wetVolume: Double,
        val dryVolume: Double,
        val cementBags: Double,
        val sandVol: Double
    )

    // ═══════════════════════════════════════════════════
    // 🔢 ENGINEERING CONSTANTS
    // ═══════════════════════════════════════════════════
    companion object {
        // Mortar shrinks ~25% when water is added → dry = wet × 1.33
        const val DRY_VOLUME_FACTOR = 1.33

        // OPC Portland Cement bulk density
        const val CEMENT_DENSITY_KG_M3 = 1500.0

        // Standard bag weight
        const val BAG_WEIGHT_KG = 50.0

        // 5% wastage for spillage and uneven surfaces
        const val WASTE_FACTOR = 1.05
    }

    private val walls   = mutableListOf<Wall>()
    private val history = mutableListOf<HistoryEntry>()
    private var modifyIndex = -1

    // Temporary openings while building a wall entry
    private val tempDoors   = mutableListOf<Opening>()
    private val tempWindows = mutableListOf<Opening>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plaster)

        // ── Inputs ─────────────────────────────────────
        val etLength = findViewById<EditText>(R.id.etLength)
        val etHeight = findViewById<EditText>(R.id.etHeight)
        val etDoorL  = findViewById<EditText>(R.id.etDoorL)
        val etDoorW  = findViewById<EditText>(R.id.etDoorW)
        val etWinL   = findViewById<EditText>(R.id.etWindowL)
        val etWinW   = findViewById<EditText>(R.id.etWindowW)

        // ── Spinners ───────────────────────────────────
        val spinnerThickness = findViewById<Spinner>(R.id.spinnerThickness)
        val spinnerMortar    = findViewById<Spinner>(R.id.spinnerMortar)

        // ── Buttons ────────────────────────────────────
        val btnAddDoor   = findViewById<Button>(R.id.btnAddDoor)
        val btnAddWindow = findViewById<Button>(R.id.btnAddWindow)
        val btnAddWall   = findViewById<Button>(R.id.btnAddWall)
        val btnModify    = findViewById<Button>(R.id.btnModifyWall)
        val btnDelete    = findViewById<Button>(R.id.btnDeleteWall)
        val btnReset     = findViewById<Button>(R.id.btnReset)
        val btnCalc      = findViewById<Button>(R.id.btnCalcPlaster)
        val btnHistory   = findViewById<Button>(R.id.btnHistory)

        // ── TextViews ──────────────────────────────────
        val tvDoors   = findViewById<TextView>(R.id.tvDoors)
        val tvWindows = findViewById<TextView>(R.id.tvWindows)
        val tvWalls   = findViewById<TextView>(R.id.tvWalls)
        val tvArea    = findViewById<TextView>(R.id.tvArea)
        val tvVolume  = findViewById<TextView>(R.id.tvVolume)
        val tvCement  = findViewById<TextView>(R.id.tvCement)
        val tvSand    = findViewById<TextView>(R.id.tvSand)
        // ── Spinners data ──────────────────────────────
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

        // ═══════════════════════════════════════════════
        // ➕ ADD DOOR  (multiple per wall supported)
        // ═══════════════════════════════════════════════
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

        // ═══════════════════════════════════════════════
        // ➕ ADD WINDOW  (multiple per wall supported)
        // ═══════════════════════════════════════════════
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

        // ═══════════════════════════════════════════════
        // ➕ ADD WALL  /  💾 SAVE MODIFICATION
        // ═══════════════════════════════════════════════
        btnAddWall.setOnClickListener {
            val length = etLength.text.toString().toDoubleOrNull()
            val height = etHeight.text.toString().toDoubleOrNull()

            if (length == null || length <= 0 || height == null || height <= 0) {
            Toast.makeText(this, "Enter valid wall Length and Height", Toast.LENGTH_SHORT).show()
            return@setOnClickListener
        }

            // ✅ Engineering check: openings must not exceed wall area
            val openingsArea = tempDoors.sumOf { it.area } + tempWindows.sumOf { it.area }
            val grossArea    = length * height
            if (openingsArea >= grossArea) {
                Toast.makeText(
                    this,
                    "⚠️ Openings area (%.2f m²) ≥ Wall area (%.2f m²)".format(openingsArea, grossArea),
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

// ═══════════════════════════════════════════════
// ✏️ MODIFY
// ═══════════════════════════════════════════════
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
                    modifyIndex    = which
                    btnAddWall.text = "💾 SAVE WALL ${which + 1}"
                    Toast.makeText(this, "Editing Wall ${which + 1}", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null).show()
        }

        // ═══════════════════════════════════════════════
        // 🗑 DELETE
        // ═══════════════════════════════════════════════
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

        // ═══════════════════════════════════════════════
        // 🔄 RESET
        // ═══════════════════════════════════════════════
        btnReset.setOnClickListener {
            walls.clear(); tempDoors.clear(); tempWindows.clear()
            modifyIndex = -1; btnAddWall.text = "ADD WALL"
            clearInputs(etLength, etHeight, etDoorL, etDoorW, etWinL, etWinW)
            updateWallsDisplay(tvWalls); updateOpeningsDisplay(tvDoors, tvWindows)
            tvArea.text = ""; tvVolume.text = ""; tvCement.text = ""; tvSand.text = ""
        }

        // ═══════════════════════════════════════════════════════════════════
        // 📊 CALCULATE  ─  FULL ENGINEERING CALCULATIONS
        // ═══════════════════════════════════════════════════════════════════
        btnCalc.setOnClickListener {
            if (walls.isEmpty()) {
                Toast.makeText(this, "Add at least one wall", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ─────────────────────────────────────────────────────────────
            // STEP 1 │ Total Net Plaster Area
            //        │ Net Area = Σ [ (L × H) − Σ doors − Σ windows ]
            // ─────────────────────────────────────────────────────────────
            val totalArea = walls.sumOf { it.netArea }

// ─────────────────────────────────────────────────────────────
// STEP 2 │ Plaster Thickness  (convert mm → m)
// ─────────────────────────────────────────────────────────────
            val thicknessStr = spinnerThickness.selectedItem.toString()
            val thickness: Double = when {
                thicknessStr.contains("6 mm")  -> 0.006
                thicknessStr.contains("12 mm") -> 0.012
                thicknessStr.contains("15 mm") -> 0.015
                thicknessStr.contains("20 mm") -> 0.020
                else                           -> 0.012
            }

            // ─────────────────────────────────────────────────────────────
            // STEP 3 │ Wet Volume
            //        │ Wet Volume = Net Area × Thickness   (m³)
            // ─────────────────────────────────────────────────────────────
            val wetVolume = totalArea * thickness

            // ─────────────────────────────────────────────────────────────
            // STEP 4 │ Dry Volume
            //        │ Mortar loses ~25% of its volume when mixed with water
            //        │ ∴ Dry Volume = Wet Volume × 1.33
            // ─────────────────────────────────────────────────────────────
            val dryVolume = wetVolume * DRY_VOLUME_FACTOR

            // ─────────────────────────────────────────────────────────────
            // STEP 5 │ Parse Mortar Ratio  e.g. "1:4" → c=1, s=4, total=5
            // ─────────────────────────────────────────────────────────────
            val mortarStr  = spinnerMortar.selectedItem.toString()
            val parts      = mortarStr.split(":")
            val cParts     = parts[0].trim().toDouble()
            val sParts     = parts[1].trim().toDouble()
            val totalParts = cParts + sParts

            // ─────────────────────────────────────────────────────────────
            // STEP 6 │ Cement & Sand Volumes
            //        │ Cement Vol = Dry Volume × ( cParts / totalParts )
            //        │ Sand Vol   = Dry Volume × ( sParts / totalParts )
            // ─────────────────────────────────────────────────────────────
            val cementVol = dryVolume * (cParts / totalParts)
            val sandVol   = dryVolume * (sParts / totalParts)

            // ─────────────────────────────────────────────────────────────
            // STEP 7 │ Cement Bags
            //        │ Cement Mass (kg) = Cement Volume (m³) × 1500 kg/m³
            //        │ Bags = Cement Mass ÷ 50 kg/bag
            // ─────────────────────────────────────────────────────────────
            val cementMass = cementVol * CEMENT_DENSITY_KG_M3
            val cementBags = cementMass / BAG_WEIGHT_KG

            // ─────────────────────────────────────────────────────────────
            // STEP 8 │ Add 5% Wastage
            //        │ Final Cement = Bags × 1.05
            //        │ Final Sand   = Sand Vol × 1.05
            // ─────────────────────────────────────────────────────────────
            val cementBagsFinal = cementBags * WASTE_FACTOR
            val sandVolFinal    = sandVol    * WASTE_FACTOR

            // ─────────────────────────────────────────────────────────────
            // STEP 9 │ Display
            // ─────────────────────────────────────────────────────────────
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

            // ─────────────────────────────────────────────────────────────
            // STEP 10 │ Save to history
            // ─────────────────────────────────────────────────────────────
            val sdf = SimpleDateFormat("dd/MM/yyyy  HH:mm", Locale.getDefault())
            history.add(0, HistoryEntry(
                date        = sdf.format(Date()),
                wallCount   = walls.size,
                totalArea   = totalArea,
                thickness   = thicknessStr,
                mortar      = mortarStr,
                wetVolume   = wetVolume,
                dryVolume   = dryVolume,
                cementBags  = cementBagsFinal,
                sandVol     = sandVolFinal
            ))
        }

        // ═══════════════════════════════════════════════
        // 📜 HISTORY
        // ═══════════════════════════════════════════════
        btnHistory.setOnClickListener {
            if (history.isEmpty()) {
                Toast.makeText(this, "No calculations yet", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val sb = StringBuilder()
            history.forEachIndexed { i, h ->
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━\n")
                sb.append("#${i + 1}  📅 ${h.date}\n")
                sb.append("Walls: ${h.wallCount}  |  Area: %.4f m²\n".format(h.totalArea))
                sb.append("Thickness: ${h.thickness}\n")
                sb.append("Mortar: ${h.mortar}\n")
                sb.append("Wet: %.5f m³  |  Dry: %.5f m³\n".format(h.wetVolume, h.dryVolume))
                sb.append("Cement: %.2f bags  |  Sand: %.5f m³\n".format(h.cementBags, h.sandVol))
            }
            AlertDialog.Builder(this)
                .setTitle("📜 Calculation History")
                .setMessage(sb.toString())
                .setPositiveButton("OK", null)
                .setNegativeButton("Clear History") { _, _ ->
                    history.clear()
                    Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show()
                }
                .show()
        }
    }

    // ═══════════════════════════════════════════════
    // 🔧 HELPERS
    // ═══════════════════════════════════════════════

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