package com.example.constructioncalculator

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class AdminFeedbackActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkAdminAccess()) return

        setContentView(R.layout.activity_admin_feedback)

        db = DatabaseHelper(this)

        val listView = findViewById<ListView>(R.id.listFeedback)

        val data = db.getAllFeedback().map { item ->
            val rating = item["rating"]?.toDoubleOrNull() ?: 0.0
            val stars  = "⭐".repeat(rating.toInt())
            "$stars ${item["rating"]}/5\n📧 ${item["email"]}\n💬 ${item["note"]}"
        }

        if (data.isEmpty()) {
            listView.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                listOf("لا توجد تقييمات بعد")
            )
        } else {
            listView.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                data
            )
        }
    }

    private fun checkAdminAccess(): Boolean {
        val email = getSharedPreferences("app_prefs", MODE_PRIVATE)
            .getString("logged_email", "") ?: ""
        if (!DatabaseHelper(this).isAdmin(email)) {
            finish()
            return false
        }
        return true
    }
}