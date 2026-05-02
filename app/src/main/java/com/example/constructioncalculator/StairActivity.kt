package com.example.constructioncalculator

import android.app.AlertDialog
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

    companion object {
        const val BLONDEL_MIN    = 0.60
        const val BLONDEL_MAX    = 0.64
        const val BLONDEL_IDEAL  = 0.63
        const val RISER_MIN      = 0.15
        const val RISER_MAX      = 0.18
        const val RISER_IDEAL    = 0.17
        const val TREAD_MIN      = 0.27
        const val TREAD_MAX      = 0.33
        const val WIDTH_MIN_1_2  = 0.90
        const val WIDTH_MIN_3UP  = 1.20
        const val ANGLE_MIN      = 25.0
        const val ANGLE_MAX      = 38.0
        const val SPIRAL_WALK_OFFSET   = 0.50
        const val SPIRAL_TREAD_MIN     = 0.20
        const val SPIRAL_TREAD_COMFORT = 0.25
    }

    // ── فرمتة الأرقام بـ Locale.US دائماً ──
    private fun Double.f(d: Int = 2) = String.format(Locale.US, "%.${d}f", this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stair_case)

        db = DatabaseHelper(this)

        val spinner      = findViewById<Spinner>(R.id.spinnerType)
        val img          = findViewById<ImageView>(R.id.imgStair)
        val etH          = findViewById<EditText>(R.id.etH)
        val etW          = findViewById<EditText>(R.id.etW)
        val etL          = findViewById<EditText>(R.id.etL)
        val etD1         = findViewById<EditText>(R.id.etD1)
        val etD2         = findViewById<EditText>(R.id.etD2)
        val etA          = findViewById<EditText>(R.id.etAlpha)
        val layoutSpiral = findViewById<LinearLayout>(R.id.layoutSpiral)
        val btnCalc      = findViewById<Button>(R.id.btnCalc)
        val btnHistory   = findViewById<Button>(R.id.btnHistory)
        val tvResult     = findViewById<TextView>(R.id.tvResult)

        val types = arrayOf(
            "Straight / مستقيم",
            "L Shape / شكل L",
            "U Shape / شكل U",
            "Spiral / حلزوني"
        )

        spinner.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, types
        )

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                img.setImageResource(
                    when (pos) {
                        0    -> R.drawable.straight
                        1    -> R.drawable.l_shape
                        2    -> R.drawable.u_shape
                        else -> R.drawable.spiral
                    }
                )
                layoutSpiral.visibility = if (pos == 3) View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        // ════════════════════════════════════════════════════════════════
        //  زر الحساب
        // ════════════════════════════════════════════════════════════════
        btnCalc.setOnClickListener {

            val H          = etH.text.toString().toDoubleOrNull()
            val W          = etW.text.toString().toDoubleOrNull()
            val Lavailable = etL.text.toString().toDoubleOrNull()

            if (H == null || W == null || Lavailable == null ||
                H <= 0.0 || W <= 0.0 || Lavailable <= 0.0) {
            Toast.makeText(this,
                "⚠️ يرجى ملء جميع الحقول بقيم صحيحة وموجبة",
                Toast.LENGTH_SHORT).show()
            return@setOnClickListener
        }

            val pos  = spinner.selectedItemPosition
            val date = SimpleDateFormat("dd/MM/yyyy HH:mm",
                Locale.getDefault()).format(Date())
            // ── اختيار N الأمثل ──
            val N = chooseBestN(H)
            val R = H / N
            val T_ideal = BLONDEL_IDEAL - 2.0 * R
            val T       = T_ideal.coerceIn(TREAD_MIN, TREAD_MAX)
            val blondel = 2.0 * R + T

            val riserOk   = R in RISER_MIN..RISER_MAX
            val treadOk   = T in TREAD_MIN..TREAD_MAX
            val blondelOk = blondel in BLONDEL_MIN..BLONDEL_MAX
            val widthOk   = W >= WIDTH_MIN_1_2
            val angleDeg  = if (T > 0.0) Math.toDegrees(atan(R / T)) else 90.0
            val angleOk   = angleDeg in ANGLE_MIN..ANGLE_MAX
            val Lneeded   = (N - 1) * T

            val allOk = riserOk && treadOk && blondelOk &&
                    widthOk && angleOk && (Lneeded <= Lavailable)
            val conformityStatus =
                if (allOk) "✅ Compliant — DTR / 91-175"
                else       "⚠️ Review warnings below"

            var length = 0.0
            var area   = 0.0
            val sb     = StringBuilder()

            when (pos) {

                // ══ Straight ══════════════════════════════════════════
                0 -> {
                    length = Lneeded
                    area   = length * W

                    sb.append("🪜  Stair Type: Straight\n")
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    sb.append("📥  Inputs\n")
                    sb.append("    Total Height (H)   : ${H.f(2)} m\n")
                    sb.append("    Width (W)          : ${W.f(2)} m\n")
                    sb.append("    Available Length   : ${Lavailable.f(2)} m\n")
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    sb.append("🔢  Step 1 — Number of Steps\n")
                    sb.append("    N = H ÷ R_ideal = ${H.f(2)} ÷ ${RISER_IDEAL.f(2)}\n")
                    sb.append("    N = ${N}\n")
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    sb.append("📐  Step 2 — Riser Height\n")
                    sb.append("    R = H ÷ N = ${H.f(2)} ÷ $N\n")
                    sb.append("    R = ${R.f(3)} m  (${(R*100).f(1)} cm)\n")
                    sb.append("    ✔ Range: 15 – 18 cm  →  " +
                            if (riserOk) "✅ OK" else "❌ OUT" + "\n")
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    sb.append("📐  Step 3 — Tread Depth (Blondel)\n")
                    sb.append("    T = 0.63 - 2×R = 0.63 - 2×${R.f(3)}\n")
                    sb.append("    T = ${T.f(3)} m  (${(T*100).f(1)} cm)\n")
                    sb.append("    ✔ Range: 27 – 33 cm  →  " +
                            if (treadOk) "✅ OK" else "❌ OUT" + "\n")
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    sb.append("📐  Step 4 — Blondel Rule\n")
                    sb.append("    2R + T = 2×${R.f(3)} + ${T.f(3)}\n")
                    sb.append("    2R + T = ${blondel.f(3)} m\n")
                    sb.append("    ✔ Range: 0.60 – 0.64 m  →  " +
                            if (blondelOk) "✅ OK" else "❌ OUT" + "\n")
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    sb.append("📐  Step 5 — Slope Angle\n")
                    sb.append("    θ = arctan(R ÷ T) = arctan(${R.f(3)} ÷ ${T.f(3)})\n")
                    sb.append("    θ = ${angleDeg.f(1)}°\n")
                    sb.append("    ✔ Range: 25° – 38°  →  " +
                            if (angleOk) "✅ OK" else "⚠️ OUT" + "\n")
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    sb.append("📏  Step 6 — Space\n")
                    sb.append("    L_needed = (N-1) × T = ($N-1) × ${T.f(3)}\n")
                    sb.append("    L_needed = ${Lneeded.f(2)} m\n")
                    sb.append("    L_available = ${Lavailable.f(2)} m\n")
                    sb.append("    →  " +
                    if (Lneeded <= Lavailable) "✅ Space OK"
                    else "❌ Need +${(Lneeded - Lavailable).f(2)} m" + "\n")
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    sb.append("📦  Results\n")
                    sb.append("    Width (W)          : ${W.f(2)} m\n")
                    sb.append("    Floor Area         : ${Lneeded.f(2)} × ${W.f(2)}\n")
                    sb.append("                       = ${area.f(2)} m²\n")
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    sb.append("$conformityStatus\n")
                    sb.append("💾 Saved ($date)")
                }

                // ══ L-Shape ═══════════════════════════════════════════
                1 -> {
                    val n1 = (N * 0.60).roundToInt().coerceIn(1, N - 1)
                    val n2 = N - n1
                    val l1 = (n1 - 1) * T
                    val l2 = (n2 - 1) * T
                    length = l1 + l2
                    area   = (l1 * W) + (l2 * W) + (W * W)

                    sb.append("🪜  Stair Type: L-Shape\n")
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    sb.append("📥  Inputs\n")
                    sb.append("    Total Height (H)   : ${H.f(2)} m\n")
                    sb.append("    Width (W)          : ${W.f(2)} m\n")
                    sb.append("    Available Length   : ${Lavailable.f(2)} m\n")
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    sb.append("🔢  Step 1 — Number of Steps\n")
                    sb.append("    N = ${N}  →  n1 = ${n1} (60%)  n2 = ${n2} (40%)\n")
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    sb.append("📐  Step 2 — Riser & Tread\n")
                    sb.append("    R = ${H.f(2)} ÷ $N = ${R.f(3)} m  (${(R*100).f(1)} cm)\n")
                    sb.append("    T = 0.63 - 2×${R.f(3)} = ${T.f(3)} m  (${(T*100).f(1)} cm)\n")
                    sb.append("    2R+T = ${blondel.f(3)} m  →  " +
                            if (blondelOk) "✅ OK" else "❌ OUT" + "\n")
                    sb.append("    Slope = ${angleDeg.f(1)}°  →  " +
                            if (angleOk) "✅ OK" else "⚠️ OUT" + "\n")
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    sb.append("📏  Step 3 — Flight Lengths\n")
                    sb.append("    L1 = (n1-1) × T = ($n1-1) × ${T.f(3)} = ${l1.f(2)} m\n")
                    sb.append("    L2 = (n2-1) × T = ($n2-1) × ${T.f(3)} = ${l2.f(2)} m\n")
                    sb.append("    Landing = ${W.f(2)} × ${W.f(2)} m\n")
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    sb.append("📦  Results\n")
                    sb.append("    Area = (${l1.f(2)}+${l2.f(2)}) × ${W.f(2)} + ${W.f(2)}²\n")
                    sb.append("         = ${area.f(2)} m²\n")
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    sb.append("$conformityStatus\n")
                    sb.append("💾 Saved ($date)")
                }

                // ══ U-Shape ═══════════════════════════════════════════
                2 -> {
                    val n1 = (N * 0.40).roundToInt().coerceIn(1, N - 2)
                    val n3 = n1
                    val n2 = (N - n1 - n3).coerceAtLeast(1)
                    val l1 = (n1 - 1) * T
                    val l2 = (n2 - 1) * T
                    val l3 = (n3 - 1) * T
                    length = l1 + l2 + l3
                    area   = (l1 * W) + (l2 * W) + (l3 * W) + (2 * W * W)

                    sb.append("🪜  Stair Type: U-Shape\n")
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    sb.append("📥  Inputs\n")
                    sb.append("    Total Height (H)   : ${H.f(2)} m\n")
                    sb.append("    Width (W)          : ${W.f(2)} m\n")
                    sb.append("    Available Length   : ${Lavailable.f(2)} m\n")
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    sb.append("🔢  Step 1 — Number of Steps\n")
                    sb.append("    N = $N  →  n1=$n1(40%)  n2=$n2(20%)  n3=$n3(40%)\n")
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    sb.append("📐  Step 2 — Riser & Tread\n")
                    sb.append("    R = ${H.f(2)} ÷ $N = ${R.f(3)} m  (${(R*100).f(1)} cm)\n")
                    sb.append("    T = 0.63 - 2×${R.f(3)} = ${T.f(3)} m  (${(T*100).f(1)} cm)\n")
                    sb.append("    2R+T = ${blondel.f(3)} m  →  " +
                            if (blondelOk) "✅ OK" else "❌ OUT" + "\n")
                    sb.append("    Slope = ${angleDeg.f(1)}°  →  " +
                            if (angleOk) "✅ OK" else "⚠️ OUT" + "\n")
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    sb.append("📏  Step 3 — Flight Lengths\n")
                    sb.append("    L1 = ($n1-1) × ${T.f(3)} = ${l1.f(2)} m\n")
                    sb.append("    L2 = ($n2-1) × ${T.f(3)} = ${l2.f(2)} m\n")
                    sb.append("    L3 = ($n3-1) × ${T.f(3)} = ${l3.f(2)} m\n")
                    sb.append("    2 Landings = 2 × ${W.f(2)} × ${W.f(2)} m\n")
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    sb.append("📦  Results\n")
                    sb.append("    Area = (${l1.f(2)}+${l2.f(2)}+${l3.f(2)}) × ${W.f(2)}\n")
                    sb.append("         + 2 × ${W.f(2)}²\n")
                    sb.append("         = ${area.f(2)} m²\n")
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    sb.append("$conformityStatus\n")
                    sb.append("💾 Saved ($date)")
                }

                // ══ Spiral ════════════════════════════════════════════
                3 -> {
                    val D1    = etD1.text.toString().toDoubleOrNull()
                    val D2    = etD2.text.toString().toDoubleOrNull()
                    val alpha = etA.text.toString().toDoubleOrNull()

                    if (D1 == null || D2 == null || alpha == null ||
                            D1 <= 0.0 || D2 <= 0.0 || alpha <= 0.0) {
                        Toast.makeText(this,
                            "⚠️ يرجى ملء بيانات السلم الحلزوني",
                            Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    if (D1 <= D2) {
                        Toast.makeText(this,
                            "⚠️ D1 يجب أن يكون أكبر من D2",
                            Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val Rext      = D1 / 2.0
                    val Rint      = D2 / 2.0
                    val Rwalk     = (Rint + SPIRAL_WALK_OFFSET).coerceAtMost(Rext - 0.05)
                    val stepAngle = alpha / N
                    val treadWalk  = 2.0 * PI * Rwalk * (stepAngle / 360.0)
                    val treadInner = 2.0 * PI * Rint  * (stepAngle / 360.0)
                    val treadOuter = 2.0 * PI * Rext  * (stepAngle / 360.0)
                    length = 2.0 * PI * Rwalk * (alpha / 360.0)
                    area   = PI * Rext * Rext

                    val treadStatus = when {
                        treadWalk < SPIRAL_TREAD_MIN     ->
                            "❌ Tread at walk line too narrow (${(treadWalk*100).f(1)} cm < 20 cm)"
                        treadWalk < SPIRAL_TREAD_COMFORT ->
                            "⚠️ Tread acceptable but narrow (${(treadWalk*100).f(1)} cm)"
                        else ->
                            "✅ Tread comfortable (${(treadWalk*100).f(1)} cm ≥ 25 cm)"
                    }
                    sb.append("🌀  Stair Type: Spiral\n")
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    sb.append("📥  Inputs\n")
                    sb.append("    Height (H)         : ${H.f(2)} m\n")
                    sb.append("    Outer Diameter D1  : ${D1.f(2)} m\n")
                    sb.append("    Inner Diameter D2  : ${D2.f(2)} m\n")
                    sb.append("    Rotation Angle     : ${alpha.f(1)}°\n")
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    sb.append("🔢  Step 1 — Number of Steps\n")
                    sb.append("    N = H ÷ R_ideal = ${H.f(2)} ÷ ${RISER_IDEAL.f(2)} = $N\n")
                    sb.append("    R = ${H.f(2)} ÷ $N = ${R.f(3)} m  (${(R*100).f(1)} cm)\n")
                    sb.append("    →  " + if (riserOk) "✅ OK" else "❌ OUT" + "\n")
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    sb.append("📐  Step 2 — Step Angle\n")
                    sb.append("    α_step = α ÷ N = ${alpha.f(1)} ÷ $N = ${stepAngle.f(2)}°\n")
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    sb.append("📐  Step 3 — Walking Radius\n")
                    sb.append("    R_int = D2 ÷ 2 = ${D2.f(2)} ÷ 2 = ${Rint.f(3)} m\n")
                    sb.append("    R_walk = R_int + 0.50 = ${Rwalk.f(3)} m\n")
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    sb.append("📐  Step 4 — Tread Depths\n")
                    sb.append("    Walk  = 2π×${Rwalk.f(3)}×(${stepAngle.f(2)}÷360)\n")
                    sb.append("          = ${treadWalk.f(3)} m  (${(treadWalk*100).f(1)} cm)\n")
                    sb.append("    Inner = 2π×${Rint.f(3)}×(${stepAngle.f(2)}÷360)\n")
                    sb.append("          = ${treadInner.f(3)} m  (${(treadInner*100).f(1)} cm)\n")
                    sb.append("    Outer = 2π×${Rext.f(3)}×(${stepAngle.f(2)}÷360)\n")
                    sb.append("          = ${treadOuter.f(3)} m  (${(treadOuter*100).f(1)} cm)\n")
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    sb.append("📏  Step 5 — Measurements\n")
                    sb.append("    Walk Length = 2π×${Rwalk.f(3)}×(${alpha.f(1)}÷360)\n")
                    sb.append("               = ${length.f(2)} m\n")
                    sb.append("    Footprint   = π×${Rext.f(3)}² = ${area.f(2)} m²\n")
                    sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                    sb.append("$treadStatus\n")
                    sb.append(if (riserOk) "✅ Riser OK" else "❌ Riser OUT")
                    sb.append("\n")
                    sb.append(
                        if ((Rext - Rint) >= WIDTH_MIN_1_2)
                            "✅ Width ${(Rext-Rint).f(2)} m OK"
                        else
                            "❌ Width ${(Rext-Rint).f(2)} m < 0.90 m"
                    )
                    sb.append("\n💾 Saved ($date)")

                    tvResult.text = sb.toString()

                    db.insertStair(
                        type    = "Spiral",
                        height  = H,
                        steps   = N,
                        riser   = R,
                        tread   = treadWalk,
                        blondel = 0.0,
                        length  = length,
                        area    = area,
                        status  = treadStatus,
                        date    = date
                    )
                    return@setOnClickListener
                }
            }

            db.insertStair(
                type    = types[pos],
                height  = H,
                steps   = N,
                riser   = R,
                tread   = T,
                blondel = blondel,
                length  = length,
                area    = area,
                status  = conformityStatus,
                date    = date
            )
            tvResult.text = sb.toString()
        }
         // ════════════════════════════════════════════════════════════════
        //  زر History — مثل Paint (AlertDialog)
        // ════════════════════════════════════════════════════════════════
        btnHistory.setOnClickListener {
            val records = db.getAllStairHistory()
            if (records.isEmpty()) {
                Toast.makeText(this, "No calculations yet", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sb = StringBuilder()
            records.forEachIndexed { i, h ->
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━\n")
                sb.append("#${i+1}  📅 ${h["date"]}\n")
                sb.append("Type    : ${h["type"]}\n")
                sb.append("Height  : ${h["height"]} m\n")
                sb.append("Steps   : ${h["steps"]}\n")
                sb.append("Riser   : ${h["riser"]} m\n")
                sb.append("Tread   : ${h["tread"]} m\n")
                sb.append("Length  : ${h["length"]} m\n")
                sb.append("Area    : ${h["area"]} m²\n")
                sb.append("Status  : ${h["status"]}\n")
            }

            AlertDialog.Builder(this)
                .setTitle("📜 Stair History")
                .setMessage(sb.toString())
                .setPositiveButton("OK", null)
                .setNegativeButton("🗑 Clear History") { _, _ ->
                    db.clearStairHistory()
                    Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show()
                }
                .show()
        }
    }

    // ════════════════════════════════════════════════════════════════════
    private fun chooseBestN(H: Double): Int {
        val N_raw   = H / RISER_IDEAL
        val N_floor = N_raw.toInt().coerceAtLeast(1)
        val N_ceil  = N_floor + 1
        val R_floor = H / N_floor
        val R_ceil  = H / N_ceil
        val floorInRange = R_floor in RISER_MIN..RISER_MAX
        val ceilInRange  = R_ceil  in RISER_MIN..RISER_MAX
        return when {
            floorInRange && ceilInRange ->
                if (abs(R_floor - RISER_IDEAL) <= abs(R_ceil - RISER_IDEAL)) N_floor else N_ceil
            floorInRange -> N_floor
            ceilInRange  -> N_ceil
            else ->
                if (abs(R_floor - RISER_IDEAL) <= abs(R_ceil - RISER_IDEAL)) N_floor else N_ceil
        }
    }
}