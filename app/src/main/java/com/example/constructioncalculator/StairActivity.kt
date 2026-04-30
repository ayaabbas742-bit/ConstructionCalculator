package com.example.constructioncalculator

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*



class StairActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    // ─── المعايير الجزائرية ───
    companion object {
        // قاعدة بلوندل الجزائرية (كما هو في DTR): 2R + T بين 60 و64 سم
        const val BLONDEL_MIN = 0.60
        const val BLONDEL_MAX = 0.64
        const val BLONDEL_IDEAL = 0.63   // القيمة المثلى الجزائرية

        // ارتفاع الدرجة (R) حسب المرسوم 91-175
        const val RISER_MIN = 0.15       // 15 سم
        const val RISER_MAX = 0.18       // 18 سم
        const val RISER_IDEAL = 0.17     // 17 سم (مثالي)

        // عمق الدرجة (T)
        const val TREAD_MIN = 0.27       // 27 سم (من بلوندل مع R=18)
        const val TREAD_MAX = 0.33       // 33 سم (من بلوندل مع R=15)

        // عرض الدرج
        const val WIDTH_MIN_1_2 = 0.90   // مسكن واحد أو اثنان
        const val WIDTH_MIN_3UP = 1.20   // 3 مساكن أو أكثر في الطابق

        // ارتفاع الطابق
        const val FLOOR_HEIGHT_MIN = 2.50  // علو الغرفة الرئيسية الجزائري
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stair_case)

        db = DatabaseHelper(this)

        val spinner = findViewById<Spinner>(R.id.spinnerType)
        val img = findViewById<ImageView>(R.id.imgStair)
        val etH = findViewById<EditText>(R.id.etH)
        val etW = findViewById<EditText>(R.id.etW)
        val etL = findViewById<EditText>(R.id.etL)
        val etD1 = findViewById<EditText>(R.id.etD1)
        val etD2 = findViewById<EditText>(R.id.etD2)
        val etA = findViewById<EditText>(R.id.etAlpha)
        val layoutSpiral = findViewById<LinearLayout>(R.id.layoutSpiral)
        val btnCalc = findViewById<Button>(R.id.btnCalc)
        val btnHistory = findViewById<Button>(R.id.btnHistory)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        val types =
            arrayOf("Straight / مستقيم", "L Shape / شكل L", "U Shape / شكل U", "Spiral / حلزوني")

        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item, types
        )

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                img.setImageResource(
                    when (pos) {
                        0 -> R.drawable.straight
                        1 -> R.drawable.l_shape
                        2 -> R.drawable.u_shape
                        else -> R.drawable.spiral
                    }
                )
                layoutSpiral.visibility = if (pos == 3) View.VISIBLE else View.GONE
            }

            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        btnCalc.setOnClickListener {

            val H = etH.text.toString().toDoubleOrNull()
            val W = etW.text.toString().toDoubleOrNull()
            val Lavailable = etL.text.toString().toDoubleOrNull()
            if (H == null || W == null || Lavailable == null || H <= 0 || W <= 0 || Lavailable <= 0) {
                Toast.makeText(this, "⚠️ يرجى ملء جميع الحقول بقيم صحيحة", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val pos = spinner.selectedItemPosition
            val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

            // ════════════════════════════════════════
            //  محرك الحسابات الهندسية الجزائرية
            // ════════════════════════════════════════

            // 1) عدد الدرجات المثالي بناءً على R المثالي (17 سم)
            val N_ideal = (H / RISER_IDEAL)
            val N = max(1, N_ideal.roundToInt())

            // 2) ارتفاع الدرجة الفعلي
            val R = H / N

            // 3) عمق الدرجة من قاعدة بلوندل الجزائرية
            val T = BLONDEL_IDEAL - (2.0 * R)

            // 4) التحقق من قيمة بلوندل
            val blondel = 2.0 * R + T  // يجب = 0.63 دائماً
            val blondelOk = blondel in BLONDEL_MIN..BLONDEL_MAX

            // 5) التحقق من معايير R و T الجزائرية
            val riserOk = R in RISER_MIN..RISER_MAX
            val treadOk = T in TREAD_MIN..TREAD_MAX

            // 6) التحقق من عرض الدرج (الجزائر: 0.90 أو 1.20 م)
            val widthMin = if (W >= WIDTH_MIN_3UP) WIDTH_MIN_3UP else WIDTH_MIN_1_2
            val widthOk = W >= WIDTH_MIN_1_2

            // 7) الطول المطلوب
            val Lneeded = (N - 1) * T

            // 8) زاوية الميل (يجب بين 25° و 38° للسكن)
            val angleRad = atan(R / T)
            val angleDeg = Math.toDegrees(angleRad)
            val angleOk = angleDeg in 25.0..38.0

            // ─── تقرير المطابقة الجزائرية ───
            // ─── Algerian Conformity Report ───
            val conformityLines = buildList {
                add(
                    if (riserOk)
                        "✅ Riser Height: ${"%.1f".format(R * 100)} cm [Standard: 15–18 cm]"
                    else
                        "❌ Riser Height: ${"%.1f".format(R * 100)} cm [Out of range 15–18 cm]"
                )

                add(
                    if (treadOk)
                        "✅ Tread Depth: ${"%.1f".format(T * 100)} cm [Standard: 27–33 cm]"
                    else
                        "❌ Tread Depth: ${"%.1f".format(T * 100)} cm [Out of range 27–33 cm]"
                )

                add(
                    if (blondelOk)
                        "✅ Blondel Formula: ${"%.3f".format(blondel)} m [Standard: 0.60–0.64 m]"
                    else
                        "❌ Blondel Formula: ${"%.3f".format(blondel)} m [Out of range 0.60–0.64 m]"
                )

                add(
                    if (widthOk)
                        "✅ Stair Width: ${"%.2f".format(W)} m [Minimum: ${"%.2f".format(widthMin)} m]"
                    else
                        "❌ Stair Width: ${"%.2f".format(W)} m [Must be ≥ ${"%.2f".format(widthMin)} m]"
                )

                add(
                    if (angleOk)
                        "✅ Slope Angle: ${"%.1f".format(angleDeg)}° [Standard: 25°–38°]"
                    else
                        "⚠️ Slope Angle: ${"%.1f".format(angleDeg)}° [Out of range 25°–38°]"
                )

                add(
                    when {
                        Lneeded <= Lavailable ->
                            "✅ Available Space: Sufficient"

                        Lneeded <= Lavailable * 1.15 ->
                            "⚠️ Available Space: Tight (Need +${"%.2f".format(Lneeded - Lavailable)} m)"

                        else ->
                            "❌ Available Space: Insufficient (Need ${"%.2f".format(Lneeded)} m, Available ${
                                "%.2f".format(
                                    Lavailable
                                )
                            } m)"
                    }
                )
            }

            val allOk =
                riserOk && treadOk && blondelOk && widthOk && angleOk && Lneeded <= Lavailable

            val conformityStatus =
                if (allOk) "✅ Compliant with Algerian Standard (91-175)"
                else "⚠️ Check the warnings below"
            var length = 0.0
            var area = 0.0
            var result = ""

            when (pos) {
                0 -> { // مستقيم
                    length = Lneeded
                    area = length * W
                    result = buildStandardResult(
                        "مستقيم - Straight",
                        N,
                        H,
                        R,
                        T,
                        blondel,
                        Lneeded,
                        Lavailable,
                        W,
                        area,
                        angleDeg,
                        conformityLines,
                        conformityStatus,
                        date
                    )
                }

                1 -> { // L Shape
                    val n1 = (N * 0.60).roundToInt()
                    val n2 = N - n1
                    val l1 = n1 * T
                    val l2 = n2 * T
                    length = l1 + l2
                    area = (l1 + l2) * W
                    result = """
                        ══════════════════════════════
                        🪜 سلم شكل L
                        ══════════════════════════════
                        الارتفاع الكلي  : ${"%.2f".format(H)} م
                        عدد الدرجات    : $N
                        
                        الذراع الأول   : $n1 درجة  → ${"%.2f".format(l1)} م
                        الذراع الثاني  : $n2 درجة  → ${"%.2f".format(l2)} م
                        
                        ارتفاع درجة R : ${"%.3f".format(R)} م (${"%.1f".format(R * 100)} سم)
                        عمق الدرجة T  : ${"%.3f".format(T)} م (${"%.1f".format(T * 100)} سم)
                        بلوندل 2R+T   : ${"%.3f".format(blondel)} م
                        زاوية الميل   : ${"%.1f".format(angleDeg)}°
                        العرض         : ${"%.2f".format(W)} م
                        المساحة الكلية: ${"%.2f".format(area)} م²
                        
                        ── مطابقة المعيار الجزائري ──
                        ${conformityLines.joinToString("\n")}
                        
                        $conformityStatus
                        ══════════════════════════════
                        💾 محفوظ ($date)
                    """.trimIndent()
                }

                2 -> { // U Shape
                    val n1 = (N * 0.40).roundToInt()
                    val n2 = N - (n1 * 2).coerceAtMost(N - 1)
                    val l1 = n1 * T
                    val l2 = n2 * T
                    length = (l1 * 2) + l2
                    area = ((l1 * 2) + l2) * W
                    result = """
                        ══════════════════════════════
                        🪜 سلم شكل U
                        ══════════════════════════════
                        الارتفاع الكلي     : ${"%.2f".format(H)} م
                        عدد الدرجات الكلي  : $N
                        
                        كل جانب            : $n1 درجة  → ${"%.2f".format(l1)} م
                        الوسط              : $n2 درجة  → ${"%.2f".format(l2)} م
                        
                        ارتفاع درجة R     : ${"%.3f".format(R)} م (${"%.1f".format(R * 100)} سم)
                        عمق الدرجة T      : ${"%.3f".format(T)} م (${"%.1f".format(T * 100)} سم)
                        بلوندل 2R+T       : ${"%.3f".format(blondel)} م
                        زاوية الميل       : ${"%.1f".format(angleDeg)}°
                        العرض             : ${"%.2f".format(W)} م
                        المساحة الكلية    : ${"%.2f".format(area)} م²
                        
                        ── مطابقة المعيار الجزائري ──
                        ${conformityLines.joinToString("\n")}
                        
                        $conformityStatus
                        ══════════════════════════════
                        💾 محفوظ ($date)
                    """.trimIndent()
                }

                3 -> { // حلزوني
                    val D1 = etD1.text.toString().toDoubleOrNull()
                    val D2 = etD2.text.toString().toDoubleOrNull()
                    val alpha = etA.text.toString().toDoubleOrNull()

                    if (D1 == null || D2 == null || alpha == null) {
                        Toast.makeText(
                            this,
                            "⚠️ يرجى ملء بيانات السلم الحلزوني",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                    if (D1 <= D2) {
                        Toast.makeText(
                            this,
                            "⚠️ D1 (خارجي) يجب أن يكون أكبر من D2 (داخلي)",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                    // نصف قطر المشي الفعلي عند 2/3 من المركز
                    val Rext = D1 / 2.0
                    val Rint = D2 / 2.0
                    val Rwalk = Rint + (Rext - Rint) * (2.0 / 3.0)

                    val stepAngle = alpha / N
                    val treadSpiral = 2.0 * PI * Rwalk * (stepAngle / 360.0)
                    length = 2.0 * PI * Rwalk * (alpha / 360.0)
                    area = PI * Rext * Rext

                    // الحلزوني: عمق الدرجة 20-25 سم عند المركز الداخلي
                    val treadInner = 2.0 * PI * Rint * (stepAngle / 360.0)
                    val spiralOk = treadSpiral >= 0.20

                    val spiralStatus = when {
                        treadSpiral < 0.20 -> "❌ عمق الدرجة ضيق جداً (${"%.1f".format(treadSpiral * 100)} سم < 20 سم)"
                        treadSpiral < 0.25 -> "⚠️ عمق الدرجة مقبول (${"%.1f".format(treadSpiral * 100)} سم)"
                        else -> "✅ عمق الدرجة مريح (${"%.1f".format(treadSpiral * 100)} سم)"
                    }

                    result = """
                        ══════════════════════════════
                        🌀 سلم حلزوني
                        ══════════════════════════════
                        الارتفاع الكلي   : ${"%.2f".format(H)} م
                        عدد الدرجات     : $N
                        ارتفاع درجة R  : ${"%.3f".format(R)} م (${"%.1f".format(R * 100)} سم)
                        
                        🔵 الأبعاد
                        قطر خارجي D1   : ${"%.2f".format(D1)} م
                        قطر داخلي D2   : ${"%.2f".format(D2)} م
                        نصف قطر المشي  : ${"%.3f".format(Rwalk)} م
                        
                        📐 الدرجة
                        زاوية/درجة      : ${"%.2f".format(stepAngle)}°
                        عمق عند المشي   : ${"%.3f".format(treadSpiral)} م (${
                        "%.1f".format(
                            treadSpiral * 100
                        )
                    } سم)
                        عمق عند الداخل  : ${"%.3f".format(treadInner)} م (${"%.1f".format(treadInner * 100)} سم)
                        مسار المشي      : ${"%.2f".format(length)} م
                        بصمة الأرض      : ${"%.2f".format(area)} م²
                        
                        $spiralStatus
                        ${if (riserOk) "✅ ارتفاع الدرجة مطابق (${"%,.1f".format(R * 100)} سم)" else "❌ ارتفاع الدرجة خارج المعيار"}
                        ══════════════════════════════
                        💾 محفوظ ($date)
                    """.trimIndent()

                    db.insertStair(
                        type = "Spiral",
                        height = H,
                        steps = N,
                        riser = R,
                        tread = treadSpiral,
                        blondel = blondel,
                        length = length,
                        area = area,
                        status = spiralStatus,
                        date = date
                    )
                    tvResult.text = result
                    return@setOnClickListener
                }
            }

            db.insertStair(
                type = types[pos],
                height = H,
                steps = N,
                riser = R,
                tread = T,
                blondel = blondel,
                length = length,
                area = area,
                status = conformityStatus,
                date = date
            )
            tvResult.text = result
        }

        btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    private fun buildStandardResult(
        type: String, N: Int, H: Double, R: Double, T: Double, blondel: Double,
        Lneeded: Double, Lavailable: Double, W: Double, area: Double,
        angleDeg: Double, conformityLines: List<String>, conformityStatus: String, date: String
    ) = """
══════════════════════════════
🪜 Stair - $type
══════════════════════════════
Total Height     : ${"%.2f".format(H)} m
Number of Steps  : $N

📐 Step Dimensions
Riser (R)        : ${"%.3f".format(R)} m (${"%.1f".format(R * 100)} cm)
Tread (T)        : ${"%.3f".format(T)} m (${"%.1f".format(T * 100)} cm)
Blondel (2R+T)   : ${"%.3f".format(blondel)} m
Slope Angle      : ${"%.1f".format(angleDeg)}°

📏 Space
Required Length  : ${"%.2f".format(Lneeded)} m
Available Length : ${"%.2f".format(Lavailable)} m
Width            : ${"%.2f".format(W)} m
Area             : ${"%.2f".format(area)} m²

── Algerian Standard Compliance (91-175) ──
${conformityLines.joinToString("\n")}

$conformityStatus
══════════════════════════════
💾 Saved ($date)
""".trimIndent()
}