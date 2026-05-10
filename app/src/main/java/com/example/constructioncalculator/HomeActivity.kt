package com.example.constructioncalculator

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var email: String
    private lateinit var badgeView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        db = DatabaseHelper(this)

        // ================= FEATURES =================
        val recycler = findViewById<RecyclerView>(R.id.recycler)
        recycler.layoutManager = GridLayoutManager(this, 3)
        recycler.adapter = FeatureAdapter(getList())

        // ================= EMAIL =================
        email = intent.getStringExtra("email") ?: ""

        // ================= DATABASE =================
        val name = db.getUserName(email)
        val title = findViewById<TextView>(R.id.title)
        title.text = if (name.isNotEmpty()) "Welcome Engineer $name " else "Welcome Engineer "

        // ================= NOTIFICATION BADGE =================
        setupNotificationBadge()

        // ================= SETTINGS BUTTON =================
        val settings = findViewById<ImageView>(R.id.ic_settings)
        settings.setOnClickListener {
            showNotificationsAndSettings()
        }

        // ================= BOTTOM NAV =================
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_calculator -> {
                    startActivity(Intent(this, MenuCalculatorActivity::class.java))
                    true
                }
                R.id.nav_notes -> {
                    startActivity(Intent(this, NoteEditActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    val i = Intent(this, ProfileActivity::class.java)
                    i.putExtra("email", email)
                    startActivity(i)
                    true
                }
                else -> false
            }
        }
    }

    // ================= NOTIFICATION BADGE SETUP =================
    private fun setupNotificationBadge() {
        val settingsContainer = findViewById<FrameLayout>(R.id.settingsContainer)

        // إنشاء badge ديناميكياً
        badgeView = TextView(this).apply {
            setBackgroundColor(Color.RED)
            setTextColor(Color.WHITE)
            textSize = 9f
            gravity = Gravity.CENTER
            visibility = View.GONE
            setPadding(6, 2, 6, 2)
        }

        // إضافة Badge فوق Settings
        val badgeParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
        }
        settingsContainer?.addView(badgeView, badgeParams)

        // تحديث الإشعارات
        updateBadge()
    }

    // ================= UPDATE BADGE =================
    private fun updateBadge() {
        val notifications = getNotifications()
        if (notifications.isEmpty()) {
            badgeView.visibility = View.GONE
        } else {
            badgeView.visibility = View.VISIBLE
            badgeView.text = if (notifications.size > 9) "9+" else notifications.size.toString()
        }
    }
    // ================= GET NOTIFICATIONS =================
    private fun getNotifications(): List<String> {
        val notifications = mutableListOf<String>()
        val today = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // ── 1. مشاريع قريبة الانتهاء (Timeline) ──
        try {
            val projects = db.getAllProjects()
            for (project in projects) {
                val endDateStr = project["end_date"] ?: continue
                val endDate = sdf.parse(endDateStr) ?: continue
                val diff = ((endDate.time - today.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
                if (diff in 0..7) {
                    notifications.add("⏰ Project \"${project["name"]}\" ends in $diff days!")
                }
            }
        } catch (_: Exception) {}

        // ── 2. فواتير منتهية الصلاحية ──
        try {
            val invoices = db.getOverdueInvoices()
            for (invoice in invoices) {
                notifications.add("📄 Invoice #${invoice["invoice_number"]} is overdue!")
            }
        } catch (_: Exception) {}

        // ── 3. تقييمات جديدة ──
        try {
            val newFeedbackCount = db.getNewFeedbackCount(email)
            if (newFeedbackCount > 0) {
                notifications.add("⭐ You have $newFeedbackCount new review(s)!")
            }
        } catch (_: Exception) {}

        // ── 4. ملاحظات البناء ──
        try {
            val notesCount = db.getRecentNotesCount()
            if (notesCount > 0) {
                notifications.add("📝 You have $notesCount new construction note(s)!")
            }
        } catch (_: Exception) {}

        return notifications
    }

    // ================= SHOW NOTIFICATIONS + SETTINGS =================
    private fun showNotificationsAndSettings() {
        val notifications = getNotifications()

        if (notifications.isNotEmpty()) {
            // عرض الإشعارات أولاً
            val message = notifications.joinToString("\n\n")
            android.app.AlertDialog.Builder(this)
                .setTitle("🔔 Notifications")
                .setMessage(message)
                .setPositiveButton("Settings") { _, _ ->
                    showSettingsMenu()
                }
                .setNegativeButton("Dismiss") { _, _ ->
                    badgeView.visibility = View.GONE
                }
                .show()
        } else {
            showSettingsMenu()
        }
    }

    // ================= SETTINGS MENU =================
    private fun showSettingsMenu() {
        val options = arrayOf("Profile", "Feedback")
        android.app.AlertDialog.Builder(this)
            .setTitle("Settings")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(this, ProfileActivity::class.java)
                        intent.putExtra("email", email)
                        startActivity(intent)
                    }
                    1 -> showFeedbackDialog(email)
                }
            }
            .show()
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
            Toast.makeText(this, "Feedback saved successfully!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            updateBadge()
        }
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        updateBadge()
    }
    // ================= FEATURES LIST =================
    private fun getList(): List<Feature> {
        return listOf(
            Feature("Construction Calculator", R.drawable.ic_calc),
            Feature("Area Calculator", R.drawable.ic_area),
            Feature("Tank Calculator", R.drawable.ic_tank),
            Feature("Plan Drawing", R.drawable.ic_draw),
            Feature("Floor Plan", R.drawable.ic_floor),
            Feature("Create Invoice", R.drawable.ic_invoice),
            Feature("Construction Notes", R.drawable.ic_notes),
            Feature("Geotechnical Engineering", R.drawable.ic_geo),
            Feature("Timeline", R.drawable.ic_time)
        )
    }
}