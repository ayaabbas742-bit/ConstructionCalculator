package com.example.constructioncalculator

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AdminUsersActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var recycler: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkAdminAccess()) return

        setContentView(R.layout.activity_admin_users)

        db = DatabaseHelper(this)
        recycler = findViewById(R.id.recyclerUsers)

        loadUsers()
    }

    private fun loadUsers() {
        val list = db.getAllUsers()

        // ✅ مُصحَّح: نمرر email للـ adapter بدل id
        val adapter = UsersAdapter(list) { email ->
            val user = list.find { it["email"] == email }
            val name = "${user?.get("firstName")} ${user?.get("lastName")}"

            AlertDialog.Builder(this)
                .setTitle("حذف المستخدم")
                .setMessage("هل أنت متأكد من حذف $name ؟")
                .setPositiveButton("حذف") { _, _ ->
                    val success = db.deleteUser(email)
                    if (success) {
                        Toast.makeText(this, "✅ تم الحذف", Toast.LENGTH_SHORT).show()
                        loadUsers()
                    } else {
                        Toast.makeText(this, "❌ لا يمكن حذف Admin", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("إلغاء", null)
                .show()
        }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
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