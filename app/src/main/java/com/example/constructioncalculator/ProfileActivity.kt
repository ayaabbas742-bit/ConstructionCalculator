package com.example.constructioncalculator

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.InputStream

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // ================= EMAIL =================
        val email = intent.getStringExtra("email") ?: ""

        // ================= SQLITE =================
        val db = DatabaseHelper(this)
        val user = db.getUser(email)

        // ================= UI =================
        val nameTv = findViewById<TextView>(R.id.nameText)
        val lastTv = findViewById<TextView>(R.id.lastNameText)
        val emailTv = findViewById<TextView>(R.id.emailText)

        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val ratingText = findViewById<TextView>(R.id.ratingText)

        val logoutBtn = findViewById<Button>(R.id.logoutBtn)

        profileImage = findViewById(R.id.profileImage)

        // ================= SET DATA FROM SQLITE =================
        if (user != null) {
            nameTv.text = user.firstName
            lastTv.text = user.lastName
            emailTv.text = user.email
        }

        ratingBar.rating = 4.5f
        ratingText.text = "Engineer Rating"

        profileImage.setImageResource(R.drawable.ic_user)

        // ================= IMAGE PICK =================
        profileImage.setOnClickListener {
            openGallery()
        }

        // ================= LOGOUT (SQLite Session Clear) =================
        logoutBtn.setOnClickListener {

            // ❌ هنا نمسح أي session محفوظة في SQLite أو app
            // (اختياري: إذا عندك table session لاحقاً)

            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            finish()
        }
    }

    // ================= IMAGE PICK =================
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {

            val uri: Uri? = data?.data

            if (uri != null) {
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)

                profileImage.setImageBitmap(bitmap)
            }
        }
    }
}