package com.example.constructioncalculator

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val firstName       = findViewById<EditText>(R.id.firstName)
        val lastName        = findViewById<EditText>(R.id.lastName)
        val email           = findViewById<EditText>(R.id.email)
        val password        = findViewById<EditText>(R.id.password)
        val confirmPassword = findViewById<EditText>(R.id.confirmPassword)
        val signupBtn       = findViewById<Button>(R.id.signupBtn)
        val togglePassword  = findViewById<ImageView>(R.id.togglePassword)
        val toggleConfirm   = findViewById<ImageView>(R.id.toggleConfirmPassword)
        val loginLink       = findViewById<TextView>(R.id.loginLink) // ✅ جديد
        var isPassVisible    = false
        var isConfirmVisible = false

        // ✅ رجوع لتسجيل الدخول
        loginLink.setOnClickListener {
            finish()
        }

        togglePassword.setOnClickListener {
            isPassVisible = !isPassVisible
            password.inputType = if (isPassVisible)
                android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            togglePassword.setImageResource(if (isPassVisible) R.drawable.ic_eye_on else R.drawable.ic_eye_off)
            password.setSelection(password.text.length)
        }

        toggleConfirm.setOnClickListener {
            isConfirmVisible = !isConfirmVisible
            confirmPassword.inputType = if (isConfirmVisible)
                android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            toggleConfirm.setImageResource(if (isConfirmVisible) R.drawable.ic_eye_on else R.drawable.ic_eye_off)
            confirmPassword.setSelection(confirmPassword.text.length)
        }

        val db = DatabaseHelper(this)
        auth   = FirebaseAuth.getInstance()

        signupBtn.setOnClickListener {
            val f = firstName.text.toString().trim()
            val l = lastName.text.toString().trim()
            val e = email.text.toString().trim()
            val p = password.text.toString().trim()
            val c = confirmPassword.text.toString().trim()

            if (f.isEmpty() || l.isEmpty() || e.isEmpty() || p.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (p != c) {
                confirmPassword.error = "Passwords not match"
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(e, p)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        db.insertUser(f, l, e, p)
                        Toast.makeText(this, "Account Created ✅", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, task.exception?.message ?: "Error", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}