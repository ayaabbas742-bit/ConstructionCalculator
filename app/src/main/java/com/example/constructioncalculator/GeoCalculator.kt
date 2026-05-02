package com.example.constructioncalculator

import kotlin.math.*

object GeoCalculator {

    // ══ SOIL CLASSIFICATION ══════════════════════════════
    data class SoilResult(
        val symbol: String,
        val description: String,
        val group: String,
        val pi: Double,
        val li: Double,
        val ci: Double
    )

    fun classifySoil(ll: Double, pl: Double, moisture: Double, fine: Double): SoilResult {
        val pi = ll - pl
        val li = if (pi != 0.0) (moisture - pl) / pi else 0.0
        val ci = if (pi != 0.0) (ll - moisture) / pi else 0.0

        val symbol: String
        val description: String
        val group: String

        if (fine < 50) {
            if (fine < 5) {
                if (pi < 4 || pi < 0.73 * (ll - 20)) {
                    symbol = "SW"; description = "Well-graded sand"; group = "Coarse"
                } else {
                    symbol = "SP"; description = "Poorly-graded sand"; group = "Coarse"
                }
            } else if (fine > 12) {
                if (pi > 7 && pi > 0.73 * (ll - 20)) {
                    symbol = "SC"; description = "Clayey sand"; group = "Coarse"
                } else {
                    symbol = "SM"; description = "Silty sand"; group = "Coarse"
                }
            } else {
                symbol = "SW-SM"; description = "Well-graded silty sand"; group = "Coarse"
            }
        } else {
            val aLine = 0.73 * (ll - 20)
            when {
                ll < 50 && pi > aLine && pi > 7 ->
                { symbol = "CL"; description = "Low plasticity clay"; group = "Fine" }
                ll < 50 && pi < aLine ->
                { symbol = "ML"; description = "Low plasticity silt"; group = "Fine" }
                ll >= 50 && pi > aLine ->
                { symbol = "CH"; description = "High plasticity clay"; group = "Fine" }
                ll >= 50 && pi < aLine ->
                { symbol = "MH"; description = "High plasticity silt"; group = "Fine" }
                else ->
                { symbol = "CL-ML"; description = "Clay-silt"; group = "Fine" }
            }
        }
        return SoilResult(symbol, description, group, pi, li, ci)
    }

    // ══ BEARING CAPACITY ═════════════════════════════════
    data class BearingResult(
        val nc: Double, val nq: Double, val ny: Double,
        val qu: Double, val qnet: Double, val qa: Double
    )

    fun calcBearing(
        c: Double, phi: Double, gamma: Double,
        df: Double, b: Double, l: Double, fs: Double
    ): BearingResult {
        val phiRad   = Math.toRadians(phi)
        val isSquare = abs(b - l) < 0.01

        val nq = exp(PI * tan(phiRad)) * tan(Math.toRadians(45.0) + phiRad / 2).pow(2)
        val nc = if (phi > 0.0) (nq - 1) / tan(phiRad) else 5.14
        val ny = 2.0 * (nq + 1) * tan(phiRad)

        val sc = if (isSquare) 1.3 else 1.0
        val sy = if (isSquare) 0.8 else 1.0
        val q  = gamma * df

        val qu   = sc * c * nc + q * nq + sy * 0.5 * gamma * b * ny
        val qnet = qu - q
        val qa   = qnet / fs + q

        return BearingResult(nc, nq, ny, qu, qnet, qa)
    }

    // ══ SETTLEMENT ═══════════════════════════════════════
    data class SettlementResult(
        val si: Double, val sc: Double, val st: Double,
        val isOC: Boolean
    )

    fun calcSettlement(
        q: Double, b: Double, es: Double, v: Double,
        h: Double, cc: Double, cs: Double, e0: Double,
        sigma0: Double, sigmaC: Double,
        l: Double = b  // أضف L كمعامل
    ): SettlementResult {

        // معامل الشكل حسب نسبة L/B (Steinbrenner)
        val lb = l / b
        val iw = when {
            lb <= 1.0 -> 0.82
            lb <= 2.0 -> 0.85
            lb <= 5.0 -> 0.89
            else      -> 0.93
        }

        val si = (q * b * (1 - v.pow(2)) * iw / es) * 1000

        val isOC       = sigmaC > sigma0
        val deltaSigma = q

        val sc: Double = when {
            isOC && sigma0 + deltaSigma <= sigmaC ->
                (cs / (1 + e0)) * h * log10((sigma0 + deltaSigma) / sigma0) * 1000
            isOC ->
                ((cs / (1 + e0)) * log10(sigmaC / sigma0) +
                        (cc / (1 + e0)) * log10((sigma0 + deltaSigma) / sigmaC)) * h * 1000
            else ->
                (cc / (1 + e0)) * h * log10((sigma0 + deltaSigma) / sigma0) * 1000
        }

        return SettlementResult(si, sc, si + sc, isOC)
    }

    // ══ SLOPE STABILITY ══════════════════════════════════
    data class SlopeResult(
        val fs: Double, val ns: Double, val hc: Double,
        val status: String, val statusColor: Int
    )

    fun calcSlope(c: Double, phi: Double, gamma: Double, h: Double, beta: Double): SlopeResult {
        val phiRad  = Math.toRadians(phi)
        val betaRad = Math.toRadians(beta)

        // Stability Number (Taylor)
        val ns = c / (gamma * h)

        // Critical Height (DTR GT 1992)
        val hc = (4.0 * c / gamma) * (
                (sin(betaRad) * cos(phiRad)) /
                        (1.0 - cos(betaRad - phiRad))
                )

        // Factor of Safety — Infinite Slope (DTR GT)
        val fs = if (beta <= phi) {
            // منحدر لا نهائي مع تماسك
            (c / (gamma * h * sin(betaRad) * cos(betaRad))) +
                    (tan(phiRad) / tan(betaRad))
        } else {
            // منحدر محدود
            (c / (gamma * h * sin(betaRad))) +
                    (tan(phiRad) / tan(betaRad))
        }

        val status = when {
            fs >= 1.5 -> "stable ✅"
            fs >= 1.0 -> "marginal ⚠️"
            else      -> "unstable ❌"
        }

        return SlopeResult(fs, ns, hc, status, 0)
    }

    // ══ LATERAL EARTH PRESSURE ════════════════════════════
    data class LateralResult(
        val ka: Double, val kp: Double, val k0: Double,
        val pa: Double, val pp: Double, val p0: Double
    )

    fun calcLateral(phi: Double, delta: Double, gamma: Double, h: Double): LateralResult {
        val phiRad   = Math.toRadians(phi)
        val deltaRad = Math.toRadians(delta)
        val alpha    = Math.toRadians(90.0)
        val beta     = Math.toRadians(0.0)

        val ka = sin(alpha + phiRad).pow(2) /
                (sin(alpha).pow(2) * sin(alpha - deltaRad) *
                        (1 + sqrt(
                            sin(phiRad + deltaRad) * sin(phiRad - beta) /
                                    (sin(alpha - deltaRad) * sin(alpha + beta))
                        )).pow(2))
        val kp = tan(Math.toRadians(45.0) + phiRad / 2).pow(2)
        val k0 = 1 - sin(phiRad)

        val pa = 0.5 * ka * gamma * h * h
        val pp = 0.5 * kp * gamma * h * h
        val p0 = 0.5 * k0 * gamma * h * h

        return LateralResult(ka, kp, k0, pa, pp, p0)
    }

    fun fmt(v: Double, d: Int = 2) = "%.${d}f".format(v)
}