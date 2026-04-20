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

        // 🔥 Zoom فقط (بدون تحريك)
        scaleDetector = ScaleGestureDetector(this,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

                override fun onScale(detector: ScaleGestureDetector): Boolean {

                    scaleFactor *= detector.scaleFactor

                    // حدود التكبير والتصغير
                    scaleFactor = scaleFactor.coerceIn(0.5f, 4f)

                    // تطبيق الزوم فقط
                    img.scaleX = scaleFactor
                    img.scaleY = scaleFactor

                    return true
                }
            })

        img.setOnTouchListener { _, event ->
            scaleDetector.onTouchEvent(event)
            true
        }
    }
}