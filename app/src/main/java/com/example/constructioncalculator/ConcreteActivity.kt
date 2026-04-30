package com.example.constructioncalculator

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

// ─────────────────────────────────────────────
// 1.  DATABASE HELPER
// ─────────────────────────────────────────────
class ConcreteDB(context: Context) :
    SQLiteOpenHelper(context, "concrete.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE history (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                date        TEXT,
                element     TEXT,
                grade       TEXT,
                volume      REAL,
                cement_bags INTEGER,
                sand_m3     REAL,
                gravel_m3   REAL,
                steel_kg    REAL,
                mix_ratio   TEXT
            )"""
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, old: Int, new: Int) {
        db.execSQL("DROP TABLE IF EXISTS history")
        onCreate(db)
    }

    fun insert(
        element: String, grade: String,
        volume: Double, cementBags: Int,
        sandM3: Double, gravelM3: Double,
        steelKg: Double, mixRatio: String
    ) {
        val cv = ContentValues().apply {
            put("date", SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()))
            put("element", element)
            put("grade", grade)
            put("volume", volume)
            put("cement_bags", cementBags)
            put("sand_m3", sandM3)
            put("gravel_m3", gravelM3)
            put("steel_kg", steelKg)
            put("mix_ratio", mixRatio)
        }
        writableDatabase.insert("history", null, cv)
    }

    fun getAll(): List<HistoryItem> {
        val list   = mutableListOf<HistoryItem>()
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM history ORDER BY id DESC", null
        )
        while (cursor.moveToNext()) {
            list.add(readRow(cursor))
        }
        cursor.close()
        return list
    }

    fun getLastResult(): HistoryItem? {
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM history ORDER BY id DESC LIMIT 1", null
        )
        val item = if (cursor.moveToFirst()) readRow(cursor) else null
        cursor.close()
        return item
    }

    fun deleteAll() = writableDatabase.delete("history", null, null)

    private fun readRow(cursor: android.database.Cursor) = HistoryItem(
        id         = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
        date       = cursor.getString(cursor.getColumnIndexOrThrow("date")),
        element    = cursor.getString(cursor.getColumnIndexOrThrow("element")),
        grade      = cursor.getString(cursor.getColumnIndexOrThrow("grade")),
        volume     = cursor.getDouble(cursor.getColumnIndexOrThrow("volume")),
        cementBags = cursor.getInt(cursor.getColumnIndexOrThrow("cement_bags")),
        sandM3     = cursor.getDouble(cursor.getColumnIndexOrThrow("sand_m3")),
        gravelM3   = cursor.getDouble(cursor.getColumnIndexOrThrow("gravel_m3")),
        steelKg    = cursor.getDouble(cursor.getColumnIndexOrThrow("steel_kg")),
        mixRatio   = cursor.getString(cursor.getColumnIndexOrThrow("mix_ratio"))
    )
}

// ─────────────────────────────────────────────
// 2.  MAIN ACTIVITY
// ─────────────────────────────────────────────
class ConcreteActivity : AppCompatActivity() {

    private lateinit var db: ConcreteDB

    // ── MIX TABLE  (DTR BC 2.41) ──────────────────────────────────────────────
    data class MixData(
        val label: String,
        val cementKgPerM3: Double,
        val ratio: String,
        val cPart: Double,
        val sPart: Double,
        val gPart: Double
    )
    private val mixTable = mapOf(
        "C16/20" to MixData("C16/20 (fc28=20 MPa)", 300.0, "1:2.5:4.5", 1.0, 2.5, 4.5),
        "C20/25" to MixData("C20/25 (fc28=25 MPa)", 350.0, "1:2.0:4.0", 1.0, 2.0, 4.0),
        "C25/30" to MixData("C25/30 (fc28=30 MPa)", 385.0, "1:1.5:3.0", 1.0, 1.5, 3.0),
        "C30/37" to MixData("C30/37 (fc28=37 MPa)", 420.0, "1:1.2:2.5", 1.0, 1.2, 2.5),
        "C35/45" to MixData("C35/45 (fc28=45 MPa)", 450.0, "1:1.0:2.0", 1.0, 1.0, 2.0)
    )

    // ── STEEL RATIOS  (CBA93) ─────────────────────────────────────────────────
    data class SteelRatio(
        val minPct: Double,
        val typPct: Double,
        val maxPct: Double,
        val note: String
    )

    private val steelTable = mapOf(
        "Slab"    to SteelRatio(0.40, 0.60, 1.20, "Art. A.8.1.21 CBA93"),
        "Footing" to SteelRatio(0.40, 0.55, 1.00, "Art. A.9 CBA93 – shallow foundation")
    )

    // ─────────────────────────────────────────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_concrete)

        db = ConcreteDB(this)

        val imgElement   = findViewById<ImageView>(R.id.imgElement)
        val spinnerEl    = findViewById<Spinner>(R.id.spinnerElement)
        val spinnerGrade = findViewById<Spinner>(R.id.spinnerGrade)
        val etLength     = findViewById<EditText>(R.id.etLength)
        val etWidth      = findViewById<EditText>(R.id.etWidth)
        val etHeight     = findViewById<EditText>(R.id.etHeight)
        val etOpenings   = findViewById<EditText>(R.id.etOpenings)
        val wasteBar     = findViewById<SeekBar>(R.id.wasteBar)
        val tvWaste      = findViewById<TextView>(R.id.tvWasteValue)
        val btnCalc      = findViewById<Button>(R.id.btnCalc)
        val btnHistory   = findViewById<Button>(R.id.btnHistory)
        val tvResult     = findViewById<TextView>(R.id.tvResult)

        // ── SPINNERS ──────────────────────────────────────────────────────────
        val elements = arrayOf("Slab", "Footing")
        spinnerEl.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, elements
        )
        val grades = mixTable.keys.toTypedArray()
        spinnerGrade.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, grades
        )

        // ── IMAGE SWITCH ──────────────────────────────────────────────────────
        spinnerEl.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) {
                when (elements[pos]) {
                    "Slab"    -> imgElement.setImageResource(R.drawable.slab)
                    "Footing" -> imgElement.setImageResource(R.drawable.footing)
                }
                updateFieldHints(elements[pos], etLength, etWidth, etHeight)
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        // ── WASTE BAR ─────────────────────────────────────────────────────────
        wasteBar.max      = 15
        wasteBar.progress = 5
        tvWaste.text      = "5%"
        wasteBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, f: Boolean) { tvWaste.text = "$p%" }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?)  {}
        })

        // ── BUTTONS ───────────────────────────────────────────────────────────
        btnCalc.setOnClickListener {
            calculate(
                element  = elements[spinnerEl.selectedItemPosition],
                gradeKey = grades[spinnerGrade.selectedItemPosition],
                etL      = etLength,
                etW      = etWidth,
                etH      = etHeight,
                etO      = etOpenings,
                wastePct = wasteBar.progress,
                tvResult = tvResult
            )
        }

        btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        // ── RESTORE LAST RESULT ON LAUNCH ─────────────────────────────────────
        restoreLastResult(tvResult)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CALCULATION ENGINE  (DTR BC 2.41 / CBA93)
    // ─────────────────────────────────────────────────────────────────────────
    private fun calculate(
        element: String, gradeKey: String,
        etL: EditText, etW: EditText, etH: EditText, etO: EditText,
        wastePct: Int, tvResult: TextView
    ) {
        val L = etL.text.toString().toDoubleOrNull()
        val W = etW.text.toString().toDoubleOrNull()
        val H = etH.text.toString().toDoubleOrNull()

        if (L == null || W == null || H == null || L <= 0 || W <= 0 || H <= 0) {
            Toast.makeText(this, "Please enter valid dimensions (> 0)", Toast.LENGTH_SHORT).show()
            return
        }

        val openings = etO.text.toString().toDoubleOrNull() ?: 0.0
        val wasteFac = 1.0 + (wastePct / 100.0)
        val mix      = mixTable[gradeKey]!!
        val steel    = steelTable[element]!!

        // 1. VOLUMES
        val volumeNet = L * W * H - openings
        if (volumeNet <= 0) {
            Toast.makeText(this, "Negative volume! Check openings value.", Toast.LENGTH_SHORT).show()
            return
        }
        val volumeCmd = volumeNet * wasteFac

        // 2. CEMENT  (DTR BC 2.41 — gravimetric)
        val cementKg = mix.cementKgPerM3 * volumeCmd
        val bags     = Math.ceil(cementKg / 50.0).toInt()

        // 3. AGGREGATES  (dry factor 1.65 — DTR)
        val dryVol     = volumeCmd * 1.65
        val totalParts = mix.cPart + mix.sPart + mix.gPart
        val sandM3     = dryVol * (mix.sPart / totalParts)
        val gravelM3   = dryVol * (mix.gPart / totalParts)

        // 4. WATER  (W/C ratio — DTR BC 2.41)
        val wc = when (gradeKey) {
            "C16/20" -> 0.55
            "C20/25" -> 0.50
            else     -> 0.45
        }
        val waterLiters = cementKg * wc

        // 5. STEEL  (CBA93 Art. A.8 / A.9)
        val steelMinKg = (steel.minPct / 100.0) * volumeNet * 7850
        val steelTypKg = (steel.typPct / 100.0) * volumeNet * 7850
        val steelMaxKg = (steel.maxPct / 100.0) * volumeNet * 7850

        // BUILD RESULT
        val sb = StringBuilder()
        sb.append("══════════════════════════════\n")
        sb.append("  RESULT  –  $element  [$gradeKey]\n")
        sb.append("  Standard: DTR BC 2.41 / CBA93\n")
        sb.append("══════════════════════════════\n\n")
        sb.append("📐 DIMENSIONS\n")
        sb.append("  L = ${"%.2f".format(L)} m  |  W = ${"%.2f".format(W)} m  |  H = ${"%.2f".format(H)} m\n")
        sb.append("  Openings = ${"%.3f".format(openings)} m³\n\n")
        sb.append("📦 VOLUMES\n")
        sb.append("  Net volume      : ${"%.3f".format(volumeNet)} m³\n")
        sb.append("  Waste factor    : +$wastePct%\n")
        sb.append("  Volume to order : ${"%.3f".format(volumeCmd)} m³\n\n")
        sb.append("🏗️ CONCRETE  (DTR Dosage)\n")
        sb.append("  Cement dosage   : ${mix.cementKgPerM3.toInt()} kg/m³\n")
        sb.append("  Cement total    : ${"%.1f".format(cementKg)} kg  →  $bags bags × 50 kg\n")
        sb.append("  Sand            : ${"%.3f".format(sandM3)} m³\n")
        sb.append("  Gravel          : ${"%.3f".format(gravelM3)} m³\n")
        sb.append("  Water (W/C=$wc) : ${"%.1f".format(waterLiters)} litres\n")
        sb.append("  Mix ratio C:S:G : ${mix.ratio}\n\n")
        sb.append("⚙️ STEEL  (${steel.note})\n")
        sb.append("  ρ min = ${steel.minPct}%  →  ${"%.1f".format(steelMinKg)} kg\n")
        sb.append("  ρ typ = ${steel.typPct}%  →  ${"%.1f".format(steelTypKg)} kg  ✅ recommended\n")
        sb.append("  ρ max = ${steel.maxPct}%  →  ${"%.1f".format(steelMaxKg)} kg\n\n")
        sb.append("⚠️  Steel values are indicative.\n")
        sb.append("     Full reinforcement design required per CBA93.\n")
        sb.append("══════════════════════════════")

        tvResult.text = sb.toString()

        // SAVE TO SQLITE
        db.insert(
            element    = element,
            grade      = gradeKey,
            volume     = volumeNet,
            cementBags = bags,
            sandM3     = sandM3,
            gravelM3   = gravelM3,
            steelKg    = steelTypKg,
            mixRatio   = mix.ratio
        )
        Toast.makeText(this, "✅ Saved to history", Toast.LENGTH_SHORT).show()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RESTORE LAST RESULT ON APP LAUNCH
    // ─────────────────────────────────────────────────────────────────────────
    private fun restoreLastResult(tvResult: TextView) {
        val last = db.getLastResult() ?: return

        val sb = StringBuilder()
        sb.append("══════════════════════════════\n")
        sb.append("  Last Saved Calculation  ✅\n")
        sb.append("  ${last.date}\n")
        sb.append("══════════════════════════════\n\n")
        sb.append("  Element   : ${last.element}\n")
        sb.append("  Grade     : ${last.grade}\n")
        sb.append("  Volume    : ${"%.3f".format(last.volume)} m³\n\n")
        sb.append("  Cement    : ${last.cementBags} bags × 50 kg\n")
        sb.append("  Sand      : ${"%.3f".format(last.sandM3)} m³\n")
        sb.append("  Gravel    : ${"%.3f".format(last.gravelM3)} m³\n")
        sb.append("  Steel     : ${last.steelKg.toInt()} kg\n")
        sb.append("  Mix ratio : ${last.mixRatio}\n\n")
        sb.append("══════════════════════════════\n")
        sb.append("  Press CALCULATE for a new result.")

        tvResult.text = sb.toString()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FIELD HINTS
    // ─────────────────────────────────────────────────────────────────────────
    private fun updateFieldHints(
        element: String,
        etL: EditText, etW: EditText, etH: EditText
    ) {
        when (element) {
            "Slab" -> {
                etL.hint = "Slab Length (m)"
                etW.hint = "Slab Width (m)"
                etH.hint = "Slab Thickness (m)  e.g. 0.15"
            }
            "Footing" -> {
                etL.hint = "Footing Length (m)"
                etW.hint = "Footing Width (m)"
                etH.hint = "Footing Height (m)  e.g. 0.40"
            }
        }
    }
}