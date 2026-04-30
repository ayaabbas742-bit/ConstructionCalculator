package com.example.constructioncalculator

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val email = prefs.getString("logged_email", "") ?: ""

        // check login + admin
        if (email.isEmpty() || !DatabaseHelper(this).isAdmin(email)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_admin_dashboard)

        db = DatabaseHelper(this)

        loadStats()

        // ── Buttons (LinearLayout / CardView -> View) ──
        findViewById<View>(R.id.btnManageUsers).setOnClickListener {
            startActivity(Intent(this, AdminUsersActivity::class.java))
        }

        findViewById<View>(R.id.btnViewFeedback).setOnClickListener {
            startActivity(Intent(this, AdminFeedbackActivity::class.java))
        }

        findViewById<View>(R.id.btnLogout).setOnClickListener {
            prefs.edit().clear().apply()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    // ── Load dashboard stats ──
    private fun loadStats() {
        findViewById<TextView>(R.id.tvTotalUsers).text =
            db.getTotalUsers().toString()

        findViewById<TextView>(R.id.tvTotalFeedback).text =
            db.getTotalFeedback().toString()

        findViewById<TextView>(R.id.tvAvgRating).text =
            "${"%.1f".format(db.getAverageRating())} ⭐"
    }

    // refresh when returning
    override fun onResume() {
        super.onResume()
        if (::db.isInitialized) {
            loadStats()
        }
    }
}