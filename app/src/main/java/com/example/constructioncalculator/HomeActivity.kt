package com.example.constructioncalculator

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // ================= FEATURES =================
        val recycler = findViewById<RecyclerView>(R.id.recycler)
        recycler.layoutManager = GridLayoutManager(this, 3)
        recycler.adapter = FeatureAdapter(getList())

        // ================= EMAIL =================
        val email = intent.getStringExtra("email") ?: ""

        // ================= DATABASE =================
        val db = DatabaseHelper(this)
        val name = db.getUserName(email)

        val title = findViewById<TextView>(R.id.title)

        title.text = if (name.isNotEmpty())
            "Welcome Engineer $name 👷"
        else
            "Welcome Engineer 👷"

        // ================= SETTINGS BUTTON =================
        val settings = findViewById<ImageView>(R.id.ic_settings)

        settings.setOnClickListener {

            val options = arrayOf("Profile", "Feedback")

            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("Settings")

            builder.setItems(options) { _, which ->

                when (which) {

                    // 👤 PROFILE
                    0 -> {
                        val intent = Intent(this, ProfileActivity::class.java)
                        intent.putExtra("email", email)
                        startActivity(intent)
                    }

                    // ⭐ FEEDBACK
                    1 -> {
                        showFeedbackDialog(email)
                    }
                }
            }

            builder.show()
        }
    }

    // ================= FEEDBACK DIALOG =================
    private fun showFeedbackDialog(email: String) {

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_settings)

        val ratingBar = dialog.findViewById<RatingBar>(R.id.ratingBar)
        val notes = dialog.findViewById<EditText>(R.id.notes)
        val saveBtn = dialog.findViewById<Button>(R.id.saveBtn)

        val db = DatabaseHelper(this)

        saveBtn.setOnClickListener {

            val rating = ratingBar.rating
            val note = notes.text.toString()

            db.insertFeedback(email, rating, note)

            Toast.makeText(
                this,
                "Feedback saved successfully!",
                Toast.LENGTH_SHORT
            ).show()

            dialog.dismiss()
        }

        dialog.show()
    }

    // ================= FEATURES LIST =================
    private fun getList(): List<Feature> {
        return listOf(
            Feature("Construction Calculator", R.drawable.ic_calc),
            Feature("Area Calculator", R.drawable.ic_area),
            Feature("Bubble Level", R.drawable.ic_level),
            Feature("Tank Calculator", R.drawable.ic_tank),
            Feature("Plan Drawing", R.drawable.ic_draw),
            Feature("Floor Plan", R.drawable.ic_floor),
            Feature("Invoices", R.drawable.ic_invoice),
            Feature("Construction Notes", R.drawable.ic_notes),
            Feature("Geotechnical Engineering", R.drawable.ic_geo)
        )
    }
}