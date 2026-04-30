package com.example.constructioncalculator

import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class SignatureActivity : AppCompatActivity() {

    private lateinit var signatureView: SignatureView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signature)

        signatureView = findViewById(R.id.signatureView)

        val btnColor = findViewById<TextView>(R.id.btnColor)
        val btnStyle = findViewById<TextView>(R.id.btnStyle)
        val btnEraser = findViewById<TextView>(R.id.btnEraser)
        val btnZoom = findViewById<TextView>(R.id.btnZoom)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.btnClear).setOnClickListener {
            signatureView.clear()
        }

        findViewById<TextView>(R.id.btnSave).setOnClickListener {
            val bitmap = signatureView.getBitmap()
            val dir = File(filesDir, "signatures")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "sig_${System.currentTimeMillis()}.png")

            FileOutputStream(file).use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }

            val result = Intent()
            result.putExtra("signature_path", file.absolutePath)
            setResult(Activity.RESULT_OK, result)
            finish()
        }

        // 🎨 لون
        btnColor.setOnClickListener {
            signatureView.setColor(Color.RED)
        }

        // ✏️ حجم القلم
        btnStyle.setOnClickListener {
            signatureView.setStrokeWidth(15f)
        }

        // 🧽 ممحاة
        btnEraser.setOnClickListener {
            signatureView.enableEraser()
        }

        // 🔍 تكبير
        btnZoom.setOnClickListener {
            signatureView.zoomIn()
        }
    }
}

// ==============================
// ✍️ Custom View داخل نفس الملف
// ==============================
class SignatureView(context: android.content.Context, attrs: android.util.AttributeSet?) :
    View(context, attrs) {

    private var drawColor = Color.BLACK
    private var stroke = 5f

    private val paint = Paint().apply {
        color = drawColor
        strokeWidth = stroke
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private val path = Path()
    private var lastX = 0f
    private var lastY = 0f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.WHITE)
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(event.x, event.y)
                lastX = event.x
                lastY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                path.quadTo(
                    lastX, lastY,
                    (event.x + lastX) / 2,
                    (event.y + lastY) / 2
                )
                lastX = event.x
                lastY = event.y
                invalidate()
            }
        }
        return true
    }

    fun clear() {
        path.reset()
        invalidate()
    }

    fun setColor(color: Int) {
        drawColor = color
        paint.color = drawColor
    }

    fun setStrokeWidth(width: Float) {
        stroke = width
        paint.strokeWidth = stroke
    }

    fun enableEraser() {
        paint.color = Color.WHITE
    }

    fun zoomIn() {
        stroke += 5f
        paint.strokeWidth = stroke
    }

    fun getBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)
        return bitmap
    }
}