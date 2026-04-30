package com.example.constructioncalculator

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail    = findViewById<EditText>(R.id.email)
        val etPassword = findViewById<EditText>(R.id.password)
        val loginBtn   = findViewById<Button>(R.id.loginBtn)
        val signupBtn  = findViewById<Button>(R.id.signupBtn)
        val forgot     = findViewById<TextView>(R.id.forgot)
        val rememberMe = findViewById<CheckBox>(R.id.remember)

        val db = DatabaseHelper(this)

        // ✅ مُصحَّح: نفس اسم الـ SharedPreferences في كل مكان "app_prefs"
        val pref = getSharedPreferences("app_prefs", MODE_PRIVATE)

        // ── تحقق من جلسة سابقة ──────────────────────────
        if (pref.getBoolean("isLogged", false)) {
            val savedEmail = pref.getString("logged_email", "") ?: ""
            if (savedEmail.isNotEmpty()) {
                goToCorrectScreen(savedEmail, db)
                return
            }
        }

        // ── تسجيل الدخول ────────────────────────────────
        loginBtn.setOnClickListener {
            val e = etEmail.text.toString().trim()
            val p = etPassword.text.toString().trim()

            if (e.isEmpty() || p.isEmpty()) {
                Toast.makeText(this, "please fill out all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (db.checkUser(e, p)) {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()

                // ✅ حفظ الجلسة بـ "app_prefs" و "logged_email"
                val editor = pref.edit()
                editor.putString("logged_email", e)
                if (rememberMe.isChecked) {
                    editor.putBoolean("isLogged", true)
                }
                editor.apply()

                goToCorrectScreen(e, db)
                finish()

            } else {
                Toast.makeText(this, "Incorrect email or password", Toast.LENGTH_SHORT).show()
            }
        }

        // ── إنشاء حساب ──────────────────────────────────
        signupBtn.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        // ── نسيت كلمة المرور ────────────────────────────
        forgot.setOnClickListener {
            val e = etEmail.text.toString().trim()
            if (e.isEmpty()) {
                Toast.makeText(this, "Enter your email address first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            intent.putExtra("email", e)
            startActivity(intent)
        }
    }

    // ── توجيه المستخدم حسب دوره ─────────────────────────
    private fun goToCorrectScreen(email: String, db: DatabaseHelper) {
        if (db.isAdmin(email)) {
            startActivity(Intent(this, AdminDashboardActivity::class.java))
        } else {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
        }
        finish()
    }
}