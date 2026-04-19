package com.example.constructioncalculator

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class DesignDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_design_detail)

        val img = findViewById<ImageView>(R.id.imgDesignDetail)

        // 🔹 استلام الصورة من Intent
        val imageRes = intent.getIntExtra("imageRes", -1) // -1 بدل 0
        if (imageRes != -1) {
            img.setImageResource(imageRes)
        } else {
            // إذا لم توجد صورة، نعرض صورة افتراضية أو نغلق Activity
            finish()
        }
    }
}