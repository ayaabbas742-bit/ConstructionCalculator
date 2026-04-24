package com.example.constructioncalculator

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class IrregularShapeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // رسم الخطوط
    private val linePaint = Paint().apply {
        color = Color.parseColor("#6D4C41")
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }

    // تلوين داخل الشكل
    private val fillPaint = Paint().apply {
        color = Color.parseColor("#6D4C41")
        alpha = 40
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // رسم النقاط
    private val dotPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // أرقام النقاط
    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 32f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
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

        val minX = points.minOf { it.first }
        val maxX = points.maxOf { it.first }
        val minY = points.minOf { it.second }
        val maxY = points.maxOf { it.second }

        val widthRange = maxX - minX
        val heightRange = maxY - minY

        if (widthRange == 0f || heightRange == 0f) return

        val padding = 50f

        val scaleX = (width - padding * 2) / widthRange
        val scaleY = (height - padding * 2) / heightRange
        val scale = min(scaleX, scaleY)

        val offsetX = (width - widthRange * scale) / 2
        val offsetY = (height - heightRange * scale) / 2

        fun transform(p: Pair<Float, Float>): PointF {
            return PointF(
                offsetX + (p.first - minX) * scale,
                offsetY + (p.second - minY) * scale
            )
        }

        // رسم الشكل
        val first = transform(points[0])
        path.moveTo(first.x, first.y)

        for (i in 1 until points.size) {
            val pt = transform(points[i])
            path.lineTo(pt.x, pt.y)
        }
        path.close()

        // تلوين الداخل
        canvas.drawPath(path, fillPaint)

        // رسم الحدود
        canvas.drawPath(path, linePaint)

        // رسم النقاط والأرقام
        for (i in points.indices) {
            val pt = transform(points[i])

            // دائرة حمراء
            canvas.drawCircle(pt.x, pt.y, 10f, dotPaint)

            // رقم النقطة
            canvas.drawText(
                "P${i + 1}",
                pt.x,
                pt.y - 18f,
                textPaint
            )
        }
    }
}