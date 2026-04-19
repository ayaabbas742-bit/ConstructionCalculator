package com.example.constructioncalculator

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.password)
        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val signupBtn = findViewById<Button>(R.id.signupBtn)
        val forgot = findViewById<TextView>(R.id.forgot)
        val rememberMe = findViewById<CheckBox>(R.id.remember)

        val db = DatabaseHelper(this)

        // ✅ SESSION
        val pref = getSharedPreferences("SESSION", MODE_PRIVATE)

        // ================= LOGIN =================
        loginBtn.setOnClickListener {

            val e = email.text.toString().trim()
            val p = password.text.toString().trim()

            if (e.isEmpty() || p.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (db.checkUser(e, p)) {

                Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show()

                // 🔥 حفظ الدخول
                if (rememberMe.isChecked) {
                    pref.edit()
                        .putBoolean("isLogged", true)
                        .putString("email", e)
                        .apply()
                }

                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("email", e)
                startActivity(intent)
                finish()

            } else {
                Toast.makeText(this, "Wrong email or password", Toast.LENGTH_SHORT).show()
            }
        }

        // ================= SIGN UP =================
        signupBtn.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        // ================= FORGOT PASSWORD =================
        forgot.setOnClickListener {

            val e = email.text.toString().trim()

            if (e.isEmpty()) {
                Toast.makeText(this, "Enter your email first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, ForgotPasswordActivity::class.java)
            intent.putExtra("email", e)
            startActivity(intent)
        }
    }
}