package com.example.constructioncalculator

import android.os.Bundle
import android.widget.*
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

        findViewById<android.widget.Button>(R.id.btnAddUser)
            .setOnClickListener { showAddUserDialog() }
        loadUsers()
    }

    private fun loadUsers() {
        val list = db.getAllUsers()
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val currentEmail = prefs.getString("logged_email", "") ?: ""

        val adapter = UsersAdapter(list, currentEmail,
            onDelete = { email ->
                val user = list.find { it["email"] == email }
                val name = "${user?.get("firstName")} ${user?.get("lastName")}"
                AlertDialog.Builder(this)
                    .setTitle("Delete User")
                    .setMessage("Are you sure you want to delete $name?")
                    .setPositiveButton("Delete") { _, _ ->
                        val success = db.deleteUser(email, currentEmail)
                        if (success) {
                            Toast.makeText(this, "✅ User deleted", Toast.LENGTH_SHORT).show()
                            loadUsers()
                        } else {
                            Toast.makeText(this, "❌ Cannot delete Admin", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            },
            onEdit = { email ->
                showEditUserDialog(email, list)
            }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
    }

    private fun showEditUserDialog(
        email: String,
        list: List<Map<String, String>>
    ) {
        val user = list.find { it["email"] == email } ?: return

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 30, 50, 10)
        }

        val etFirst = EditText(this).apply {
            hint = "First Name"
            setText(user["firstName"])
        }
        val etLast = EditText(this).apply {
            hint = "Last Name"
            setText(user["lastName"])
        }
        val etRole = EditText(this).apply {
            hint = "Role (user / admin)"
            setText(user["role"])
        }

        layout.addView(etFirst)
        layout.addView(etLast)
        layout.addView(etRole)

        AlertDialog.Builder(this)
            .setTitle("✏️ Edit User")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val success = db.updateUser(
                    email = email,
                    newFirstName = etFirst.text.toString().trim(),
                    newLastName = etLast.text.toString().trim(),
                    newRole = etRole.text.toString().trim()
                )
                if (success) {
                    Toast.makeText(this, "✅ User updated", Toast.LENGTH_SHORT).show()
                    loadUsers()
                } else {
                    Toast.makeText(this, "❌ Update failed", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun showAddUserDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 30, 50, 10)
        }
        val etFirst = EditText(this).apply { hint = "First Name" }
        val etLast = EditText(this).apply { hint = "Last Name" }
        val etEmail = EditText(this).apply { hint = "Email" }
        val etPass = EditText(this).apply { hint = "Password" }
        val etRole = EditText(this).apply { hint = "Role (user / admin)" }

        layout.addView(etFirst)
        layout.addView(etLast)
        layout.addView(etEmail)
        layout.addView(etPass)
        layout.addView(etRole)

        AlertDialog.Builder(this)
            .setTitle("➕ Add User")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val first = etFirst.text.toString().trim()
                val last = etLast.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val pass = etPass.text.toString().trim()
                val role = etRole.text.toString().trim()

                if (first.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(this, "⚠️ Fill required fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val success = db.addUser(
                    firstName = first,
                    lastName = last,
                    email = email,
                    password = pass,
                    role = role.ifEmpty { "user" }
                )
                if (success) {
                    Toast.makeText(this, "✅ User added successfully", Toast.LENGTH_SHORT).show()
                    loadUsers()
                } else {
                    Toast.makeText(this, "❌ Email already exists", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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