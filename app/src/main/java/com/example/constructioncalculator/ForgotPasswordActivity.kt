package com.example.constructioncalculator

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val emailEt   = findViewById<EditText>(R.id.email)
        val sendBtn   = findViewById<Button>(R.id.sendBtn)
        val backBtn   = findViewById<ImageView>(R.id.backBtn)
        val backLogin = findViewById<TextView>(R.id.backToLogin)

        auth = FirebaseAuth.getInstance()

        // ← زر الرجوع في الأعلى
        backBtn.setOnClickListener {
            finish()
        }

        // ← رجوع لتسجيل الدخول
        backLogin.setOnClickListener {
            finish()
        }

        sendBtn.setOnClickListener {
            val email = emailEt.text.toString().trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEt.error = "Invalid email"
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Reset link sent to $email 📧 Check your inbox!",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            task.exception?.message ?: "Error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }
}