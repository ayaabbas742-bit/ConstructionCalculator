package com.example.constructioncalculator

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil

class TilesActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    // ─── مقاسات البلاط الجزائرية الشائعة ───
    data class TilePreset(
        val name: String,
        val lengthCm: Double,
        val widthCm: Double,
        val wastePct: Double,   // نسبة الهدر القياسية
        val drawableId: Int
    )

    private val tilePresets = listOf(
        TilePreset("سيراميك 30×30 سم",    30.0, 30.0, 0.05, R.drawable.tile_ceramic),
        TilePreset("بورسلان 40×40 سم",    40.0, 40.0, 0.07, R.drawable.tile_porcelain),
        TilePreset("بورسلان 60×60 سم",    60.0, 60.0, 0.08, R.drawable.tile_porcelain),
        TilePreset("بورسلان 80×80 سم",    80.0, 80.0, 0.10, R.drawable.tile_porcelain),
        TilePreset("رخام 30×60 سم",       30.0, 60.0, 0.10, R.drawable.tile_marble),
        TilePreset("غرانيت 60×60 سم",     60.0, 60.0, 0.10, R.drawable.tile_granite),
        TilePreset("موزاييك 20×20 سم",    20.0, 20.0, 0.15, R.drawable.tile_mosaic),
        TilePreset("خاص (إدخال يدوي)",     0.0,  0.0, 0.08, R.drawable.tile_ceramic)
    )

    // نوع التركيب
    private val installTypes = arrayOf(
        "مستقيم (Droit) - هدر +5%",
        "مائل 45° (Diagonal) - هدر +12%",
        "متداخل (Décalé) - هدر +8%"
    )
    private val installWaste = doubleArrayOf(0.05, 0.12, 0.08)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tiles)

        db = DatabaseHelper(this)

        val img      = findViewById<ImageView>(R.id.imgTiles)
        val spType   = findViewById<Spinner>(R.id.spTileType)
        val etFL     = findViewById<EditText>(R.id.etFloorL)
        val etFW     = findViewById<EditText>(R.id.etFloorW)
        val etTL     = findViewById<EditText>(R.id.etTileL)
        val etTW     = findViewById<EditText>(R.id.etTileW)
        val tv       = findViewById<TextView>(R.id.tvTilesResult)

        // ─── Spinner نوع البلاط ───
        spType.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item,
            tilePresets.map { it.name })

        spType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) {
                val preset = tilePresets[pos]
                img.setImageResource(preset.drawableId)

                // ملء المقاسات تلقائياً (إلا "خاص")
                if (preset.lengthCm > 0) {
                    etTL.setText(preset.lengthCm.toInt().toString())
                    etTW.setText(preset.widthCm.toInt().toString())
                    etTL.isEnabled = false
                    etTW.isEnabled = false
                } else {
                    etTL.setText("")
                    etTW.setText("")
                    etTL.isEnabled = true
                    etTW.isEnabled = true
                }
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
        // ─── Spinner نوع التركيب ───
        // نضيف spinner ثانٍ لطريقة التركيب
        // (تأكد من إضافة R.id.spInstallType في layout)
        val spInstall = try {
            findViewById<Spinner>(R.id.spInstallType)
        } catch (e: Exception) { null }

        spInstall?.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item, installTypes)

        // ─── حساب ───
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCalc)
            .setOnClickListener {

                val floorL = etFL.text.toString().toDoubleOrNull()
                val floorW = etFW.text.toString().toDoubleOrNull()
                val tileL  = etTL.text.toString().toDoubleOrNull()
                val tileW  = etTW.text.toString().toDoubleOrNull()

                if (floorL == null || floorW == null||  tileL == null || tileW == null
                    ||floorL <= 0 || floorW <= 0 || tileL <= 0 || tileW <= 0) {
                Toast.makeText(this, "⚠️ يرجى ملء جميع الحقول بقيم صحيحة", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

                val pos     = spType.selectedItemPosition
                val preset  = tilePresets[pos]

                // هدر = هدر البلاط + هدر التركيب
                val installPos   = spInstall?.selectedItemPosition ?: 0
                val installExtra = installWaste.getOrElse(installPos) { 0.05 }

                // الهدر الإجمالي = هدر البلاط + هدر التركيب (لا يتضاعف، بل يُجمع)
                val totalWastePct = preset.wastePct + installExtra

                val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

                // ─── الحسابات ───

                // مساحة الأرضية
                val floorArea  = floorL * floorW

                // مساحة البلاطة الواحدة (الإدخال بالسنتيمتر → تحويل للمتر)
                val tileAreaM2 = (tileL / 100.0) * (tileW / 100.0)

                // عدد البلاطات الصافي (بدون هدر)
                val baseTiles  = floorArea / tileAreaM2

                // عدد البلاطات مع الهدر (تقريب للأعلى دائماً)
                val totalTiles = ceil(baseTiles * (1.0 + totalWastePct)).toInt()

                // عدد البلاطات في كل اتجاه
                val tilesAlongL = ceil(floorL / (tileL / 100.0)).toInt()
                val tilesAlongW = ceil(floorW / (tileW / 100.0)).toInt()

                // عدد الصناديق (افتراض: الصندوق الجزائري = 1 م²)
                val boxArea = tileAreaM2   // مساحة البلاطة
                // الصندوق عادة 6-8 بلاطات في الجزائر للمقاس 60×60
                val tilesPerBox = when {
                    tileL >= 80 -> 4
                    tileL >= 60 -> 4
                    tileL >= 40 -> 6
                    else        -> 9
                }
                val boxesNeeded = ceil(totalTiles.toDouble() / tilesPerBox).toInt()

                // ─── التحقق من المعيار الجزائري للمساحة ───
                val roomCheck = when {
                    floorArea >= 22 -> "✅ صالح لغرفة جلوس (≥ 22 م²)"
                    floorArea >= 12 -> "✅ صالح لغرفة نوم (≥ 12 م²)"
                    floorArea >= 8  -> "ℹ️ صالح لحمام/مطبخ صغير"
                    else            -> "ℹ️ مساحة صغيرة (< 8 م²)"
                }

                // ─── الحفظ ───
                db.insertTileHistory(
                    tileType   = preset.name,
                    floorArea  = floorArea,
                    tileLcm    = tileL,
                    tileWcm    = tileW,
                    baseTiles  = ceil(baseTiles).toInt(),
                    totalTiles = totalTiles,
                    wastePct   = totalWastePct * 100,
                    installType = installTypes.getOrElse(installPos) { "مستقيم" },
                    date       = date
                )

                // ─── عرض النتيجة ───
                tv.text = """
                ══════════════════════════════
                    🧱 حساب البلاط - معيار جزائري
                    ══════════════════════════════
                    النوع          : ${preset.name}
                    طريقة التركيب : ${installTypes.getOrElse(installPos) { "مستقيم" }}
                    
                    📐 الأرضية
                    الطول          : $floorL م
                    العرض          : $floorW م
                    المساحة        : ${"%.3f".format(floorArea)} م²
                    $roomCheck
                    
                    🧩 البلاطة
                    المقاس         : ${tileL.toInt()} × ${tileW.toInt()} سم
                    مساحة البلاطة  : ${"%.4f".format(tileAreaM2)} م²
                    
                    🔢 الكميات
                    عدد صافٍ       : ${ceil(baseTiles).toInt()} بلاطة
                    هدر البلاط     : ${(preset.wastePct * 100).toInt()}%
                    هدر التركيب    : ${(installExtra * 100).toInt()}%
                    هدر إجمالي     : ${(totalWastePct * 100).toInt()}%
                    الإجمالي النهائي: $totalTiles بلاطة
                    
                    📦 التعبئة
                    بلاطات/صندوق   : $tilesPerBox بلاطة
                    عدد الصناديق   : $boxesNeeded صندوق
                    
                    📊 التوزيع
                    على الطول      : $tilesAlongL بلاطة
                    على العرض      : $tilesAlongW بلاطة
                    ══════════════════════════════
                    💾 محفوظ ($date)
                """.trimIndent()
            }
    }
}