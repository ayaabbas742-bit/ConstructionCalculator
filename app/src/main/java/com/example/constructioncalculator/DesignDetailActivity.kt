package com.example.constructioncalculator

import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class DesignDetailActivity : AppCompatActivity() {

    private lateinit var img: ImageView
    private lateinit var scaleDetector: ScaleGestureDetector

    private var scaleFactor = 1f

    // 🔥 position for drag
    private var dX = 0f
    private var dY = 0f
    private var lastX = 0f
    private var lastY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_design_detail)

        img = findViewById(R.id.imgDesignDetail)

        val imageRes = intent.getIntExtra("imageRes", -1)
        if (imageRes != -1) {
            img.setImageResource(imageRes)
        } else {
            finish()
        }

        // 🔥 ZOOM
        scaleDetector = ScaleGestureDetector(this,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {

                    scaleFactor *= detector.scaleFactor
                    scaleFactor = scaleFactor.coerceIn(0.5f, 5f)

                    img.scaleX = scaleFactor
                    img.scaleY = scaleFactor

                    return true
                }
            })

        // 🔥 TOUCH (Zoom + Drag)
        img.setOnTouchListener { _, event ->

            scaleDetector.onTouchEvent(event)

            when (event.actionMasked) {

                MotionEvent.ACTION_DOWN -> {
                    lastX = event.rawX - dX
                    lastY = event.rawY - dY
                }

                MotionEvent.ACTION_MOVE -> {
                    dX = event.rawX - lastX
                    dY = event.rawY - lastY

                    img.translationX = dX
                    img.translationY = dY
                }
            }

            true
        }
    }
}