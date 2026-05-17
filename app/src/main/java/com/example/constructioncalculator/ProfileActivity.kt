package com.example.constructioncalculator

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileImage: ShapeableImageView
    private lateinit var db: DatabaseHelper
    private lateinit var email: String

    // ✅ بديل حديث عن startActivityForResult
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            if (uri != null) {
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    profileImage.setImageURI(uri)
                    db.updateProfileImage(email, uri.toString())
                } catch (_: Exception) {
                    Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        email = intent.getStringExtra("email") ?: ""
        db = DatabaseHelper(this)

        // ================= FIND VIEWS =================
        profileImage       = findViewById(R.id.profileImage)
        val cameraIcon     = findViewById<ShapeableImageView>(R.id.cameraIcon)
        val tvFirstName    = findViewById<TextView>(R.id.tvFirstName)
        val tvLastName     = findViewById<TextView>(R.id.tvLastName)
        val tvEmail        = findViewById<TextView>(R.id.tvEmail)
        val tvFullName     = findViewById<TextView>(R.id.tvFullName)
        val tvRatingBadge  = findViewById<TextView>(R.id.tvRatingBadge)
        val tvScoreNumber  = findViewById<TextView>(R.id.tvScoreNumber)
        val tvRatingLabel  = findViewById<TextView>(R.id.tvRatingLabel)
        val tvReviewCount  = findViewById<TextView>(R.id.tvReviewCount)
        val ratingBar      = findViewById<RatingBar>(R.id.ratingBar)
        val btnBack        = findViewById<ImageButton>(R.id.btnBack)
        val logoutBtn      = findViewById<MaterialButton>(R.id.logoutBtn)

        // ================= LOAD USER DATA =================
        val user = db.getUser(email)
        if (user != null) {
            tvFirstName.text = user.firstName
            tvLastName.text  = user.lastName
            tvEmail.text     = user.email
            tvFullName.text  = "${user.firstName} ${user.lastName}"

            // Load profile image
            try {
                if (!user.profileImage.isNullOrEmpty()) {
                    val uri = Uri.parse(user.profileImage)
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    profileImage.setImageURI(uri)
                } else {
                    profileImage.setImageResource(R.drawable.ic_engineer_avatar)
                }
            } catch (_: Exception) {
                profileImage.setImageResource(R.drawable.ic_engineer_avatar)
            }
        }

        // ================= RATING =================
        val rating      = db.getEngineerRating(email) ?: 4.5f
        val reviewCount = db.getReviewCount(email) ?: 0

        ratingBar.rating    = rating
        tvScoreNumber.text  = String.format("%.1f", rating)
        tvRatingLabel.text  = "Engineer Rating"
        tvReviewCount.text  = "$reviewCount reviews"
        tvRatingBadge.text  = " ${String.format("%.1f", rating)}  ·  Certified Engineer"
        // ================= PICK IMAGE =================
        val openGallery = {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "image/*"
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            pickImageLauncher.launch(intent)
        }

        profileImage.setOnClickListener { openGallery() }
        cameraIcon.setOnClickListener  { openGallery() }

        // ================= BACK =================
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // ================= LOGOUT =================
        logoutBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .remove("isLogged")
                .remove("logged_email")
                .apply()
            Intent(this, LoginActivity::class.java).also {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(it)
            }
            finish()
        }
    }
}