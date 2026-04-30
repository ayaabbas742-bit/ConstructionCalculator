package com.example.constructioncalculator

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class PaintActivity : AppCompatActivity() {

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
        const val WASTE_FACTOR = 1.15

        val PAINT_COVERAGE = mapOf(
            "Acrylic"       to 8.5,
            "Oil"           to 6.5,
            "Decorative"    to 4.5,
            "Anti-Moisture" to 6.5,
            "Exterior"      to 7.5,
            "Epoxy"         to 4.5,
            "Primer"        to 10.5
        )

        val PAINT_COATS = mapOf(
            "Acrylic"       to 3,
            "Oil"           to 2,
            "Decorative"    to 3,
            "Anti-Moisture" to 2,
            "Exterior"      to 3,
            "Epoxy"         to 2,
            "Primer"        to 1
        )

        val PAINT_IMAGES = mapOf(
            "Acrylic"       to R.drawable.paint_acrylic,
            "Oil"           to R.drawable.paint_oil,
            "Decorative"    to R.drawable.paint_decorative,
            "Anti-Moisture" to R.drawable.paint_moisture,
            "Exterior"      to R.drawable.paint_exterior,
            "Epoxy"         to R.drawable.paint_epoxy,
            "Primer"        to R.drawable.paint_primer
        )

        val CAN_SIZES = listOf(20.0, 10.0, 4.0, 2.5, 1.0)
    }

    private val walls       = mutableListOf<Wall>()
    private var modifyIndex = -1
    private val tempDoors   = mutableListOf<Opening>()
    private val tempWindows = mutableListOf<Opening>()
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paint)

        db = DatabaseHelper(this)

        // ── Views ───────────────────────────────────────────────────────
        val imgPaint     = findViewById<ImageView>(R.id.imgPaint)
        val spType1      = findViewById<Spinner>(R.id.spPaintType)
        val spType2      = findViewById<Spinner>(R.id.spPaintType2)
        val rg           = findViewById<RadioGroup>(R.id.rgPaintMode)
        val tvMsg        = findViewById<TextView>(R.id.tvModeMsg)
        val etL          = findViewById<EditText>(R.id.etLength)
        val etH          = findViewById<EditText>(R.id.etHeight)
        val etDL         = findViewById<EditText>(R.id.etDoorL)
        val etDW         = findViewById<EditText>(R.id.etDoorW)
        val etWL         = findViewById<EditText>(R.id.etWindowL)
        val etWW         = findViewById<EditText>(R.id.etWindowW)
        val etP1         = findViewById<EditText>(R.id.etPercent1)
        val etP2         = findViewById<EditText>(R.id.etPercent2)
        val tvWalls      = findViewById<TextView>(R.id.tvWalls)
        val tvDoors      = findViewById<TextView>(R.id.tvDoors)
        val tvWindows    = findViewById<TextView>(R.id.tvWindows)
        val tvResult     = findViewById<TextView>(R.id.tvResult)
        val btnAddDoor   = findViewById<Button>(R.id.btnAddDoor)
        val btnAddWindow = findViewById<Button>(R.id.btnAddWindow)
        val btnAddWall   = findViewById<Button>(R.id.btnAddWall)
        val btnModify    = findViewById<Button>(R.id.btnModifyWall)
        val btnDelete    = findViewById<Button>(R.id.btnDeleteWall)
        val btnReset     = findViewById<Button>(R.id.btnReset)
        val btnCalc      = findViewById<Button>(R.id.btnCalc)
        val btnHistory   = findViewById<Button>(R.id.btnHistory)

        val paintTypes = arrayOf(
            "Acrylic", "Oil", "Decorative",
            "Anti-Moisture", "Exterior", "Epoxy", "Primer"
        )

        // ── Spinners ────────────────────────────────────────────────────
        spType1.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, paintTypes)
        spType2.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, paintTypes)

        spType2.visibility = View.GONE
        etP1.visibility    = View.GONE
        etP2.visibility    = View.GONE

        spType1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>, v: View?, pos: Int, id: Long) {
                PAINT_IMAGES[paintTypes[pos]]?.let { imgPaint.setImageResource(it) }
            }
            override fun onNothingSelected(p: AdapterView<*>) {}
        }

        // ── Paint Mode ──────────────────────────────────────────────────
        rg.setOnCheckedChangeListener { _, id ->
            if (id == R.id.rbDual) {
                tvMsg.text         = "Dual Mode: p1% + p2% must = 100"
                spType2.visibility = View.VISIBLE
                etP1.visibility    = View.VISIBLE
                etP2.visibility    = View.VISIBLE
            } else {
                tvMsg.text         = "Single Mode: One paint"
                spType2.visibility = View.GONE
                etP1.visibility    = View.GONE
                etP2.visibility    = View.GONE
            }
        }

        // ── Add Door ────────────────────────────────────────────────────
        btnAddDoor.setOnClickListener {
            val l = etDL.text.toString().toDoubleOrNull()
            val w = etDW.text.toString().toDoubleOrNull()
            if (l == null || w == null || l <= 0 || w <= 0) {
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
            if (length == null || length <= 0 || height == null || height <= 0) {
                Toast.makeText(this, "Enter valid wall Length and Height", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val openingsArea = tempDoors.sumOf { it.area } + tempWindows.sumOf { it.area }
            if (openingsArea >= length * height) {
                Toast.makeText(
                    this,
                    "⚠️ Openings (%.2f m²) ≥ Wall area (%.2f m²)".format(openingsArea, length * height),
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
        // CALCULATE
        // ═══════════════════════════════════════════════════════════════
        btnCalc.setOnClickListener {
            if (walls.isEmpty()) {
                Toast.makeText(this, "Add at least one wall", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val isDual = rg.checkedRadioButtonId == R.id.rbDual

            // ── Validate Dual percentages ───────────────────────────────
            var p1 = 100.0
            var p2 = 0.0
            if (isDual) {
                val raw1 = etP1.text.toString().toDoubleOrNull()
                val raw2 = etP2.text.toString().toDoubleOrNull()
                if (raw1 == null || raw2 == null) {
                    Toast.makeText(this, "Enter both percentages", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (raw1 <= 0 || raw2 <= 0) {
                    Toast.makeText(this, "Each % must be > 0", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (Math.abs(raw1 + raw2 - 100.0) > 0.01) {
                    Toast.makeText(
                        this,
                        "⚠️ %.1f%% + %.1f%% = %.1f%% ≠ 100%%".format(raw1, raw2, raw1 + raw2),
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }
                p1 = raw1; p2 = raw2
            }

            // ── STEP 1: Total Net Area ──────────────────────────────────
            val totalNetArea = walls.sumOf { it.netArea }

            // ── STEP 2: Paint 1 ─────────────────────────────────────────
            val type1     = spType1.selectedItem.toString()
            val coverage1 = PAINT_COVERAGE[type1] ?: 8.5
            val coats1    = PAINT_COATS[type1]    ?: 2

            val area1     = totalNetArea * (p1 / 100.0)
            val baseVol1  = (area1 / coverage1) * coats1
            val finalVol1 = baseVol1 * WASTE_FACTOR

            // ── STEP 3: Paint 2 (Dual only) ─────────────────────────────
            var type2     = ""
            var coverage2 = 0.0
            var coats2    = 0
            var baseVol2  = 0.0
            var finalVol2 = 0.0

            if (isDual) {
                type2     = spType2.selectedItem.toString()
                coverage2 = PAINT_COVERAGE[type2] ?: 8.5
                coats2    = PAINT_COATS[type2]    ?: 2
                val area2 = totalNetArea * (p2 / 100.0)
                baseVol2  = (area2 / coverage2) * coats2
                finalVol2 = baseVol2 * WASTE_FACTOR
            }

            val totalLiters = finalVol1 + finalVol2

            // ── STEP 4: Optimal can distribution ───────────────────────
            val cansMap1  = optimalCans(finalVol1)
            val cansMap2  = if (isDual) optimalCans(finalVol2) else emptyMap()
            val cansTotal = optimalCans(totalLiters)

            // ── STEP 5: Build result ────────────────────────────────────
            val sb = StringBuilder()
            sb.append("🧱  Walls: ${walls.size}\n")
            walls.forEachIndexed { i, w ->
                sb.append("  W${i+1}: ${w.length}×${w.height}m  →  Net: %.3f m²\n".format(w.netArea))
            }
            sb.append("📐  Total Net Area = %.4f m²\n".format(totalNetArea))
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            sb.append("🎨  Paint 1: $type1\n")
            sb.append("    Coverage : %.1f m²/L\n".format(coverage1))
            sb.append("    Coats    : $coats1\n")
            if (isDual) sb.append("    Area     : %.4f × %.0f%% = %.4f m²\n".format(totalNetArea, p1, area1))
            sb.append("    Base Vol : (%.4f ÷ %.1f) × %d = %.4f L\n".format(area1, coverage1, coats1, baseVol1))
            sb.append("    +15%% waste: %.4f × 1.15 = %.4f L\n".format(baseVol1, finalVol1))
            sb.append("    Cans     : ${formatCans(cansMap1)}\n")

            if (isDual) {
                val area2 = totalNetArea * (p2 / 100.0)
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                sb.append("🎨  Paint 2: $type2\n")
                sb.append("    Coverage : %.1f m²/L\n".format(coverage2))
                sb.append("    Coats    : $coats2\n")
                sb.append("    Area     : %.4f × %.0f%% = %.4f m²\n".format(totalNetArea, p2, area2))
                sb.append("    Base Vol : (%.4f ÷ %.1f) × %d = %.4f L\n".format(area2, coverage2, coats2, baseVol2))
                sb.append("    +15%% waste: %.4f × 1.15 = %.4f L\n".format(baseVol2, finalVol2))
                sb.append("    Cans     : ${formatCans(cansMap2)}\n")
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                sb.append("✅  Total = %.4f + %.4f\n".format(finalVol1, finalVol2))
            }

            sb.append("✅  TOTAL PAINT = %.3f L\n".format(totalLiters))
            sb.append("🛒  Total Cans  : ${formatCans(cansTotal)}")
            tvResult.text = sb.toString()

            // ── STEP 6: Save to DB ──────────────────────────────────────
            val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            db.insertPaintHistory(
                 paintType  = if (isDual) "$type1 + $type2" else type1,
            surface    = "Walls: ${walls.size}",
            area       = totalNetArea,
            coats      = coats1,
            paintLiters = totalLiters,
            cansNeeded = cansTotal.values.sum(),
            primerL    = 0.0,
            date       = dateStr
            )
            Toast.makeText(this, "✅ Saved to history", Toast.LENGTH_SHORT).show()
        }

        // ── History ─────────────────────────────────────────────────────
        btnHistory.setOnClickListener {
            val records = db.getAllPaintHistory()
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
                sb.append("Paint    : ${h["paint_type"]}\n")
                sb.append("Total    : ${"%.3f".format(h["paint_liters"]?.toDoubleOrNull() ?: 0.0)} L\n")
                sb.append("Cans     : ${h["cans_needed"]}\n")
            }

            AlertDialog.Builder(this)
                .setTitle("📜 Calculation History")
                .setMessage(sb.toString())
                .setPositiveButton("OK", null)
                .setNegativeButton("🗑 Clear History") { _, _ ->
                    db.clearPaintHistory()
                    Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show()
                }
                .show()
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private fun optimalCans(liters: Double): Map<Double, Int> {
        val result    = mutableMapOf<Double, Int>()
        var remaining = liters
        for (size in CAN_SIZES) {
            if (remaining <= 0) break
            val count = (remaining / size).toInt()
            if (count > 0) {
                result[size] = count
                remaining -= count * size
            }
        }
        if (remaining > 0.001) result[1.0] = (result[1.0] ?: 0) + 1
        return result
    }

    private fun formatCans(cans: Map<Double, Int>): String {
        if (cans.isEmpty()) return "—"
        return cans.entries.joinToString("  ") { (size, count) ->
            "${count}×${if (size == size.toInt().toDouble()) size.toInt().toString() else size}L"
        }
    }

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