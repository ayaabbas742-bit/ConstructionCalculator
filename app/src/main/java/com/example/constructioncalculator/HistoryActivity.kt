package com.example.constructioncalculator

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HistoryActivity : AppCompatActivity() {

    lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val tvHistory = findViewById<TextView>(R.id.tvHistory)

        db = DatabaseHelper(this)

        try {

            val data = db.getAll()

            tvHistory.text = if (data.isEmpty()) {
                "📭 No History Yet"
            } else {
                data.joinToString("\n\n")
            }

        } catch (e: Exception) {
            tvHistory.text = "⚠️ Error loading history: ${e.message}"
        }
    }
}