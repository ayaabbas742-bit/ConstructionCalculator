package com.example.constructioncalculator

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val firstName = findViewById<EditText>(R.id.firstName)
        val lastName = findViewById<EditText>(R.id.lastName)
        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.password)
        val confirmPassword = findViewById<EditText>(R.id.confirmPassword)
        val signupBtn = findViewById<Button>(R.id.signupBtn)

        val db = DatabaseHelper(this)

        signupBtn.setOnClickListener {

            val f = firstName.text.toString().trim()
            val l = lastName.text.toString().trim()
            val e = email.text.toString().trim()
            val p = password.text.toString().trim()
            val c = confirmPassword.text.toString().trim()

            // ❗ check empty
            if (f.isEmpty() || l.isEmpty() || e.isEmpty() || p.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ❗ check password match
            if (p != c) {
                confirmPassword.error = "Passwords not match"
                return@setOnClickListener
            }

            // ❗ insert into SQLite
            val success = db.insertUser(f, l, e, p)

            if (success) {

                Toast.makeText(this, "Account Created", Toast.LENGTH_SHORT).show()

                // ➜ go to Login
                startActivity(Intent(this, LoginActivity::class.java))
                finish()

            } else {
                Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show()
            }
        }
    }
}