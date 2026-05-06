package com.example.constructioncalculator

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    // ✅ ضع إيميل الـ Admin هنا
    private val ADMIN_EMAIL = "admin@gmail.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail    = findViewById<EditText>(R.id.email)
        val etPassword = findViewById<EditText>(R.id.password)
        val loginBtn   = findViewById<Button>(R.id.loginBtn)
        val signupBtn = findViewById<com.google.android.material.button.MaterialButton>(R.id.signupBtn)
        val forgot     = findViewById<TextView>(R.id.forgot)
        val rememberMe = findViewById<CheckBox>(R.id.remember)
        val togglePassword = findViewById<ImageView>(R.id.togglePassword)
        var isPasswordVisible = false

        togglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePassword.setImageResource(R.drawable.ic_eye_on)
            } else {
                etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePassword.setImageResource(R.drawable.ic_eye_off)
            }
            etPassword.setSelection(etPassword.text.length)
        }

        val pref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        auth     = FirebaseAuth.getInstance()

        // ── تحقق من جلسة سابقة ──────────────────────────
        if (pref.getBoolean("isLogged", false)) {
            val savedEmail = pref.getString("logged_email", "") ?: ""
            if (savedEmail.isNotEmpty()) {
                goToCorrectScreen(savedEmail)
                return
            }
        }

        // ── تسجيل الدخول عبر Firebase ───────────────────
        loginBtn.setOnClickListener {
            val e = etEmail.text.toString().trim()
            val p = etPassword.text.toString().trim()

            if (e.isEmpty() || p.isEmpty()) {
                Toast.makeText(this, "please fill out all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(e, p)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                        val editor = pref.edit()
                        editor.putString("logged_email", e)
                        editor.putBoolean("isLogged", rememberMe.isChecked)
                        editor.apply()
                        goToCorrectScreen(e)
                        finish()
                    } else {
                        Toast.makeText(this, "Incorrect email or password", Toast.LENGTH_SHORT).show()
                    }
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
    // ✅ التحقق من Admin عن طريق الإيميل مباشرة
    private fun goToCorrectScreen(email: String) {
        if (email == ADMIN_EMAIL) {
            startActivity(Intent(this, AdminDashboardActivity::class.java))
        } else {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
        }
        finish()
    }
}