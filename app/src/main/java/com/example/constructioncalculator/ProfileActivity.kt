package com.example.constructioncalculator

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileImage: ImageView
    private lateinit var db: DatabaseHelper
    private lateinit var email: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        email = intent.getStringExtra("email") ?: ""
        db = DatabaseHelper(this)

        val nameTv = findViewById<TextView>(R.id.nameText)
        val lastTv = findViewById<TextView>(R.id.lastNameText)
        val emailTv = findViewById<TextView>(R.id.emailText)
        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val ratingText = findViewById<TextView>(R.id.ratingText)
        val logoutBtn = findViewById<Button>(R.id.logoutBtn)
        profileImage = findViewById(R.id.profileImage)

        // ================= SET DATA =================
        val user = db.getUser(email)
        if (user != null) {
            nameTv.text = user.firstName
            lastTv.text = user.lastName
            emailTv.text = user.email

            // 🔥 LOAD IMAGE
            try {
                if (!user.profileImage.isNullOrEmpty()) {
                    val uri = Uri.parse(user.profileImage)
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    profileImage.setImageURI(uri)
                } else {
                    profileImage.setImageResource(R.drawable.ic_user)
                }
            } catch (e: Exception) {
                profileImage.setImageResource(R.drawable.ic_user)
            }
        }

        ratingBar.rating = 4.5f
        ratingText.text = "Engineer Rating"

        // ================= PICK IMAGE =================
        profileImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "image/*"
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivityForResult(intent, 100)
        }

        // ================= LOGOUT =================
        logoutBtn.setOnClickListener {
            getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .clear()
                .apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val uri: Uri? = data?.data
            if (uri != null) {
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    profileImage.setImageURI(uri)
                    db.updateProfileImage(email, uri.toString())
                } catch (e: Exception) {
                    Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}