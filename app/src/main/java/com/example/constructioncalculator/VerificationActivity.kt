package com.example.constructioncalculator

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class VerificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

        val codeEt = findViewById<EditText>(R.id.code)
        val btn = findViewById<Button>(R.id.verifyBtn)

        val db = DatabaseHelper(this)

        // 📩 email جاي من Forgot Password
        val email = intent.getStringExtra("email") ?: ""

        btn.setOnClickListener {

            val code = codeEt.text.toString().trim()

            if (code.length != 4) {
                codeEt.error = "Enter 4 digits"
                return@setOnClickListener
            }

            // 🔑 check OTP from SQLite
            if (db.checkOtp(email, code)) {

                Toast.makeText(this, "Verified", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, NewPasswordActivity::class.java)
                intent.putExtra("email", email)
                startActivity(intent)

                finish()

            } else {
                Toast.makeText(this, "Wrong code", Toast.LENGTH_SHORT).show()
            }
        }
    }
}