package com.example.constructioncalculator

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class TriangleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paintBg = Paint().apply {
        color = Color.parseColor("#0A4A42")
        isAntiAlias = true
    }

    private val paintTriangle = Paint().apply {
        color = Color.parseColor("#0D5C52")
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()

        // خلفية
        canvas.drawRect(0f, 0f, w, h, paintBg)

        // مثلث
        val path = Path()
        path.moveTo(0f, h)
        path.lineTo(w / 2, 0f)
        path.lineTo(w, h)
        path.close()
        canvas.drawPath(path, paintTriangle)
    }
}