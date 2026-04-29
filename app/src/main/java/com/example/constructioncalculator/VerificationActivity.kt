package com.example.constructioncalculator

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class VerificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

        val d1 = findViewById<EditText>(R.id.d1)
        val d2 = findViewById<EditText>(R.id.d2)
        val d3 = findViewById<EditText>(R.id.d3)
        val d4 = findViewById<EditText>(R.id.d4)

        val btn = findViewById<Button>(R.id.verifyBtn)
        val db = DatabaseHelper(this)

        val email = intent.getStringExtra("email") ?: ""

        btn.setOnClickListener {

            // 🔥 نجمع الكود هنا (داخل الزر)
            val code = d1.text.toString() +
                    d2.text.toString() +
                    d3.text.toString() +
                    d4.text.toString()

            if (code.length != 4) {
                Toast.makeText(this, "Enter 4 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔑 التحقق من SQLite
            if (db.checkOtp(email, code)) {

                Toast.makeText(this, "Verified ✔", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, NewPasswordActivity::class.java)
                intent.putExtra("email", email)
                startActivity(intent)

                finish()

            } else {
                Toast.makeText(this, "Wrong code ❌", Toast.LENGTH_SHORT).show()
            }
        }
    }
}