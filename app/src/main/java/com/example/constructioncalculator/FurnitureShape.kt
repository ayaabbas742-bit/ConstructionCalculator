package com.example.constructioncalculator

import android.graphics.*

class FurnitureShape(
    var x: Float,
    var y: Float,
    val type: String,
    private val bitmap: Bitmap?
) : FloorPlanShape() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    var scale: Float = 1f

    private val baseSize = 120f

    override fun draw(canvas: Canvas) {

        val size = baseSize * scale

        if (bitmap != null) {
            canvas.drawBitmap(
                bitmap,
                null,
                RectF(x, y, x + size, y + size),
                paint
            )
        } else {
            // fallback إذا الصورة ناقصة
            val p = Paint().apply { color = Color.GRAY }
            canvas.drawRect(x, y, x + size, y + size, p)
            canvas.drawText(type, x + 10f, y + 60f, Paint().apply {
                color = Color.WHITE
                textSize = 25f
            })
        }
    }

    override fun contains(px: Float, py: Float): Boolean {
        val size = baseSize * scale
        return px in x..(x + size) && py in y..(y + size)
    }

    override fun moveTo(nx: Float, ny: Float) {
        x = nx
        y = ny
    }

    override fun getBounds(): RectF {
        val size = baseSize * scale
        return RectF(x, y, x + size, y + size)
    }
}