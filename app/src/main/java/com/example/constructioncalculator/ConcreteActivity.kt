package com.example.constructioncalculator

import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class ConcreteActivity : AppCompatActivity() {


    private lateinit var db: DatabaseHelper

    data class MixData(
        val label: String,
        val cementKgPerM3: Double,
        val ratio: String,
        val cPart: Double,
        val sPart: Double,
        val gPart: Double
    )

    data class SteelRatio(
        val minPct: Double,
        val typPct: Double,
        val maxPct: Double,
        val note: String
    )

    private val mixTable = mapOf(
        "C16/20" to MixData("C16/20 (fc28=20 MPa)", 300.0, "1:2.5:4.5", 1.0, 2.5, 4.5),
        "C20/25" to MixData("C20/25 (fc28=25 MPa)", 350.0, "1:2.0:4.0", 1.0, 2.0, 4.0),
        "C25/30" to MixData("C25/30 (fc28=30 MPa)", 385.0, "1:1.5:3.0", 1.0, 1.5, 3.0),
        "C30/37" to MixData("C30/37 (fc28=37 MPa)", 420.0, "1:1.2:2.5", 1.0, 1.2, 2.5),
        "C35/45" to MixData("C35/45 (fc28=45 MPa)", 450.0, "1:1.0:2.0", 1.0, 1.0, 2.0)
    )

    private val steelTable = mapOf(
        "Slab"    to SteelRatio(0.40, 0.60, 1.20, "Art. A.8.1.21 CBA93"),
        "Footing" to SteelRatio(0.40, 0.55, 1.00, "Art. A.9 CBA93")
    )

    private fun Double.f(d: Int = 2) = String.format(Locale.US, "%.${d}f", this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_concrete)

        // ✅ غيّرنا ConcreteDB إلى DatabaseHelper
        db = DatabaseHelper(this)

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

        val elements = arrayOf("Slab", "Footing")
        val grades   = mixTable.keys.toTypedArray()

        spinnerEl.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, elements)
        spinnerGrade.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, grades)

        spinnerEl.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: android.view.View?,
                                        pos: Int, id: Long) {
                imgElement.setImageResource(
                    if (elements[pos] == "Slab") R.drawable.slab else R.drawable.footing)
                updateHints(elements[pos], etLength, etWidth, etHeight)
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        wasteBar.max      = 15
        wasteBar.progress = 5
        tvWaste.text      = "5%"
        wasteBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, f: Boolean) {
                tvWaste.text = "$p%"
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })

        // ════════════════════════════════════════════════════════════════
        //  CALCULATE
        // ════════════════════════════════════════════════════════════════
        btnCalc.setOnClickListener {
            val L = etLength.text.toString().toDoubleOrNull()
            val W = etWidth.text.toString().toDoubleOrNull()
            val H = etHeight.text.toString().toDoubleOrNull()

            if (L == null ||W== null||H == null ||
         L <=0|| W <= 0||H <= 0) {
            Toast.makeText(this,
                "⚠️ Please enter valid dimensions (> 0)",
                Toast.LENGTH_SHORT).show()
            return@setOnClickListener
        }

            val openings = etOpenings.text.toString().toDoubleOrNull() ?: 0.0
            val wastePct = wasteBar.progress
            val wasteFac = 1.0 + (wastePct / 100.0)
            val element  = elements[spinnerEl.selectedItemPosition]
            val gradeKey = grades[spinnerGrade.selectedItemPosition]
            val mix      = mixTable[gradeKey]!!
            val steel    = steelTable[element]!!
            val date     = SimpleDateFormat("dd/MM/yyyy HH:mm",
                Locale.getDefault()).format(Date())

            val volumeGross = L * W * H
            val volumeNet   = volumeGross - openings
            if (volumeNet <= 0) {
                Toast.makeText(this,
                    "⚠️ Negative volume! Check openings.",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val volumeCmd  = volumeNet * wasteFac
            val cementKg   = mix.cementKgPerM3 * volumeCmd
            val bags       = Math.ceil(cementKg / 50.0).toInt()
            val dryVol     = volumeCmd * 1.65
            val totalParts = mix.cPart + mix.sPart + mix.gPart
            val sandM3     = dryVol * (mix.sPart / totalParts)
            val gravelM3   = dryVol * (mix.gPart / totalParts)
            val wc = when (gradeKey) {
                "C16/20" -> 0.55
                "C20/25" -> 0.50
                else     -> 0.45
            }
            val waterLiters = cementKg * wc
            val steelMinKg  = (steel.minPct / 100.0) * volumeNet * 7850
            val steelTypKg  = (steel.typPct / 100.0) * volumeNet * 7850
            val steelMaxKg  = (steel.maxPct / 100.0) * volumeNet * 7850

            val sb = StringBuilder()
            sb.append("🏗️  Concrete Calculator\n")
            sb.append("    Standard: DTR BC 2.41 / CBA93\n")
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            sb.append("📥  Inputs\n")
            sb.append("    Element  : $element\n")
            sb.append("    Grade    : ${mix.label}\n")
            sb.append("    L = ${L.f(2)} m  |  W = ${W.f(2)} m  |  H = ${H.f(2)} m\n")
            sb.append("    Openings : ${openings.f(3)} m³\n")
            sb.append("    Waste    : $wastePct%\n")
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            sb.append("📦  Step 1 — Net Volume\n")
            sb.append("    V_gross = ${L.f(2)} × ${W.f(2)} × ${H.f(2)}\n")
            sb.append("            = ${volumeGross.f(3)} m³\n")
            sb.append("    V_net   = ${volumeGross.f(3)} − ${openings.f(3)}\n")
            sb.append("            = ${volumeNet.f(3)} m³\n")
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            sb.append("📦  Step 2 — Order Volume\n")
            sb.append("    V_order = ${volumeNet.f(3)} × ${wasteFac.f(2)}\n")
            sb.append("            = ${volumeCmd.f(3)} m³\n")
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            sb.append("🏗️  Step 3 — Cement\n")
            sb.append("    Dosage  : ${mix.cementKgPerM3.toInt()} kg/m³\n")
            sb.append("    C_total = ${mix.cementKgPerM3.toInt()} × ${volumeCmd.f(3)}\n")
            sb.append("            = ${cementKg.f(1)} kg\n")
            sb.append("    Bags    = ceil(${cementKg.f(1)} ÷ 50)\n")
            sb.append("            = $bags bags × 50 kg\n")
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            sb.append("🪨  Step 4 — Aggregates\n")
            sb.append("    Mix ratio C:S:G = ${mix.ratio}\n")
            sb.append("    Dry vol = ${volumeCmd.f(3)} × 1.65 = ${dryVol.f(3)} m³\n")
            sb.append("    Parts   = ${totalParts.f(1)}\n")
            sb.append("    Sand    = ${dryVol.f(3)} × (${mix.sPart} ÷ ${totalParts.f(1)})\n")
            sb.append("            = ${sandM3.f(3)} m³\n")
            sb.append("    Gravel  = ${dryVol.f(3)} × (${mix.gPart} ÷ ${totalParts.f(1)})\n")
            sb.append("            = ${gravelM3.f(3)} m³\n")
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            sb.append("💧  Step 5 — Water  (W/C = $wc)\n")
            sb.append("    Water = ${cementKg.f(1)} × $wc\n")
            sb.append("          = ${waterLiters.f(1)} litres\n")
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            sb.append("⚙️  Step 6 — Steel  (${steel.note})\n")
            sb.append("    ρ min = ${steel.minPct}%  →  ${steelMinKg.f(1)} kg\n")
            sb.append("    ρ typ = ${steel.typPct}%  →  ${steelTypKg.f(1)} kg  ✅\n")
            sb.append("    ρ max = ${steel.maxPct}%  →  ${steelMaxKg.f(1)} kg\n")
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            sb.append("✅  Cement : $bags bags × 50 kg\n")
            sb.append("✅  Sand   : ${sandM3.f(3)} m³\n")
            sb.append("✅  Gravel : ${gravelM3.f(3)} m³\n")
            sb.append("✅  Water  : ${waterLiters.f(1)} L\n")
            sb.append("✅  Steel  : ${steelTypKg.f(1)} kg (typical)\n")
            sb.append("⚠️  Steel values are indicative.\n")
            sb.append("💾  Saved ($date)")

            tvResult.text = sb.toString()

            // ✅ غيّرنا db.insert إلى db.insertConcreteHistory
            db.insertConcreteHistory(
                element    = element,
                grade      = gradeKey,
                volume     = volumeNet,
                cementBags = bags,
                sandM3     = sandM3,
                gravelM3   = gravelM3,
                steelKg    = steelTypKg,
                mixRatio   = mix.ratio,
                date       = date
            )
            Toast.makeText(this, "✅ Saved to history", Toast.LENGTH_SHORT).show()
        }

        // ════════════════════════════════════════════════════════════════
        //  HISTORY
        // ════════════════════════════════════════════════════════════════
        btnHistory.setOnClickListener {
            // ✅ غيّرنا db.getAll() إلى db.getAllConcreteHistory()
            val records = db.getAllConcreteHistory()
            if (records.isEmpty()) {
                Toast.makeText(this, "No calculations yet",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sb = StringBuilder()
            // ✅ أضفنا النوع صراحةً لحل مشكلة inference
            records.forEachIndexed { i: Int, h: Map<String, String> ->
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━\n")
                sb.append("#${i + 1}  📅 ${h["date"]}\n")
                sb.append("Element  : ${h["element"]}\n")
                sb.append("Grade    : ${h["grade"]}\n")
                sb.append("Volume   : ${h["volume"]} m³\n")
                sb.append("Cement   : ${h["cement_bags"]} bags × 50 kg\n")
                sb.append("Sand     : ${h["sand_m3"]} m³\n")
                sb.append("Gravel   : ${h["gravel_m3"]} m³\n")
                sb.append("Steel    : ${h["steel_kg"]} kg\n")
                sb.append("Mix      : ${h["mix_ratio"]}\n")
            }

            AlertDialog.Builder(this)
                .setTitle("📜 Concrete History")
                .setMessage(sb.toString())
                .setPositiveButton("OK", null)
            // ✅ غيّرنا db.deleteAll() إلى db.clearConcreteHistory()
           .setNegativeButton("🗑 Clear History") { _, _ ->
            db.clearConcreteHistory()
            Toast.makeText(this, "History cleared",
                Toast.LENGTH_SHORT).show()
        }
            .show()
        }

        restoreLastResult(tvResult)
    }

    // ════════════════════════════════════════════════════════════════════
    private fun restoreLastResult(tvResult: TextView) {
        // ✅ غيّرنا db.getLastResult() إلى db.getAllConcreteHistory()
        val records = db.getAllConcreteHistory()
        if (records.isEmpty()) return
        val last = records.first()

        val sb = StringBuilder()
        sb.append("🏗️  Last Saved Calculation\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("📅  ${last["date"]}\n")
        sb.append("    Element  : ${last["element"]}\n")
        sb.append("    Grade    : ${last["grade"]}\n")
        sb.append("    Volume   : ${last["volume"]} m³\n")
        sb.append("    Cement   : ${last["cement_bags"]} bags × 50 kg\n")
        sb.append("    Sand     : ${last["sand_m3"]} m³\n")
        sb.append("    Gravel   : ${last["gravel_m3"]} m³\n")
        sb.append("    Steel    : ${last["steel_kg"]} kg\n")
        sb.append("    Mix      : ${last["mix_ratio"]}\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("Press CALCULATE for a new result.")
        tvResult.text = sb.toString()
    }

    private fun updateHints(
        element: String,
        etL: EditText, etW: EditText, etH: EditText
    ) {
        when (element) {
            "Slab" -> {
                etL.hint = "Slab Length (m)"
                etW.hint = "Slab Width (m)"
                etH.hint = "Thickness (m)  e.g. 0.15"
            }
            "Footing" -> {
                etL.hint = "Footing Length (m)"
                etW.hint = "Footing Width (m)"
                etH.hint = "Height (m)  e.g. 0.40"
            }
        }
    }
}