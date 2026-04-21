package com.example.constructioncalculator

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

class IrregularShapeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint().apply {
        color = Color.parseColor("#6D4C41")
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }

    private val path = Path()

    var points: List<Pair<Float, Float>> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (points.size < 2) return

        path.reset()

        val scale = calculateScale()

        path.moveTo(points[0].first * scale, points[0].second * scale)

        for (i in 1 until points.size) {
            path.lineTo(points[i].first * scale, points[i].second * scale)
        }

        path.close()

        canvas.drawPath(path, paint)
    }

    private fun calculateScale(): Float {
        val maxX = points.maxOfOrNull { it.first } ?: 1f
        val maxY = points.maxOfOrNull { it.second } ?: 1f

        val maxValue = max(maxX, maxY)

        return if (maxValue == 0f) 1f else (width / maxValue) * 0.8f
    }
}