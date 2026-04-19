package com.example.constructioncalculator

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val emailEt = findViewById<EditText>(R.id.email)
        val sendBtn = findViewById<Button>(R.id.sendBtn)

        val db = DatabaseHelper(this)

        sendBtn.setOnClickListener {

            val email = emailEt.text.toString().trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEt.error = "Invalid email"
                return@setOnClickListener
            }

            // 🔥 generate OTP random
            val otp = db.generateOtp(email)

            Toast.makeText(this, "Code sent: $otp", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, VerificationActivity::class.java)
            intent.putExtra("email", email)
            startActivity(intent)

            finish()
        }
    }
}