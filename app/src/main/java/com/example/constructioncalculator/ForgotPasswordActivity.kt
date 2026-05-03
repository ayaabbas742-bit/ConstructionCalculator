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

        val emailEt = findViewById<EditText>(R.id.email)
        val sendBtn = findViewById<Button>(R.id.sendBtn)

        auth = FirebaseAuth.getInstance()

        sendBtn.setOnClickListener {
            val email = emailEt.text.toString().trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEt.error = "Invalid email"
                return@setOnClickListener
            }

            // ← Firebase يرسل إيميل حقيقي لإعادة تعيين كلمة السر
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Email sent to $email ! Check your inbox and Spam folder 📧",
                            Toast.LENGTH_LONG
                        ).show()
                        startActivity(Intent(this, LoginActivity::class.java))
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