package com.example.constructioncalculator

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.math.min

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

        // 🔥 حساب الحدود
        val minX = points.minOf { it.first }
        val maxX = points.maxOf { it.first }
        val minY = points.minOf { it.second }
        val maxY = points.maxOf { it.second }

        val widthRange = maxX - minX
        val heightRange = maxY - minY

        if (widthRange == 0f || heightRange == 0f) return

        // 🔥 Padding داخل الشاشة
        val padding = 40f

        val scaleX = (width - padding * 2) / widthRange
        val scaleY = (height - padding * 2) / heightRange
        val scale = min(scaleX, scaleY)

        // 🔥 تمركز الشكل في الوسط
        val offsetX = (width - widthRange * scale) / 2
        val offsetY = (height - heightRange * scale) / 2

        fun transform(p: Pair<Float, Float>): PointF {
            return PointF(
                offsetX + (p.first - minX) * scale,
                offsetY + (p.second - minY) * scale
            )
        }

        val first = transform(points[0])
        path.moveTo(first.x, first.y)

        for (i in 1 until points.size) {
            val pt = transform(points[i])
            path.lineTo(pt.x, pt.y)
        }

        path.close()

        canvas.drawPath(path, paint)
    }
}