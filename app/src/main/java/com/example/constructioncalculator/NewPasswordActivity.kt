package com.example.constructioncalculator

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class NewPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_password)

        val pass = findViewById<EditText>(R.id.newPassword)
        val confirm = findViewById<EditText>(R.id.confirmPassword)
        val btn = findViewById<Button>(R.id.saveBtn)

        val db = DatabaseHelper(this)

        // 📩 لازم يكون جاي من Verification
        val email = intent.getStringExtra("email")

        Toast.makeText(this, "EMAIL = $email", Toast.LENGTH_LONG).show()

        if (email.isNullOrEmpty()) {
            Toast.makeText(this, "Email missing!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        btn.setOnClickListener {

            val p = pass.text.toString().trim()
            val c = confirm.text.toString().trim()

            if (p.length < 6) {
                pass.error = "Min 6 characters"
                return@setOnClickListener
            }

            if (p != c) {
                confirm.error = "Passwords not match"
                return@setOnClickListener
            }

            // 🔥 UPDATE SQLite password
            val updated = db.updatePassword(email, p)

            if (updated) {

                Toast.makeText(this, "Password Updated", Toast.LENGTH_SHORT).show()

                // 🔁 back to login
                startActivity(Intent(this, LoginActivity::class.java))
                finish()

            } else {
                Toast.makeText(this, "Error updating password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}