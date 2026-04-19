package com.example.constructioncalculator

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val dot1 = findViewById<View>(R.id.dot1)
        val dot2 = findViewById<View>(R.id.dot2)
        val dot3 = findViewById<View>(R.id.dot3)

        val anim = AnimationUtils.loadAnimation(this, R.anim.loader_blink)

        dot1.startAnimation(anim)
        dot2.startAnimation(anim.apply { startOffset = 150 })
        dot3.startAnimation(anim.apply { startOffset = 300 })

        // ✅ SESSION
        val pref = getSharedPreferences("SESSION", MODE_PRIVATE)
        val isLogged = pref.getBoolean("isLogged", false)
        val email = pref.getString("email", "")

        Handler(Looper.getMainLooper()).postDelayed({

            if (isLogged) {
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("email", email)
                startActivity(intent)
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }

            finish()

        }, 2500)
    }
}