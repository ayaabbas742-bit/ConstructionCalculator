package com.example.constructioncalculator

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ClientActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var selectMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client)

        db         = DatabaseHelper(this)
        selectMode = intent.getBooleanExtra("select_mode", false)

        val etName   = findViewById<EditText>(R.id.etName)
        val etEmail  = findViewById<EditText>(R.id.etEmail)
        val etPhone  = findViewById<EditText>(R.id.etPhone)
        val etAddr1  = findViewById<EditText>(R.id.etAddress1)
        val etAddr2  = findViewById<EditText>(R.id.etAddress2)
        val etShip1  = findViewById<EditText>(R.id.etShip1)
        val etShip2  = findViewById<EditText>(R.id.etShip2)
        val etNotes  = findViewById<EditText>(R.id.etNotes)

        // إذا select_mode أظهر قائمة العملاء الموجودين
        if (selectMode) {
            showExistingClients(etName)
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<TextView>(R.id.btnSave).setOnClickListener {
            if (etName.text.toString().isEmpty()) {
                Toast.makeText(this, "Enter client name!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // حفظ العميل في SQLite
            val clientId = db.saveClient(
                name         = etName.text.toString(),
                email        = etEmail.text.toString(),
                phone        = etPhone.text.toString(),
                address1     = etAddr1.text.toString(),
                address2     = etAddr2.text.toString(),
                shipAddress1 = etShip1.text.toString(),
                shipAddress2 = etShip2.text.toString(),
                notes        = etNotes.text.toString()
            )

            if (selectMode) {
                // إرجاع بيانات العميل للفاتورة
                val result = Intent()
                result.putExtra("client_id",   clientId.toInt())
                result.putExtra("client_name", etName.text.toString())
                result.putExtra("client_email", etEmail.text.toString())
                result.putExtra("client_phone", etPhone.text.toString())
                result.putExtra("client_address",
                    "${etAddr1.text}\n${etAddr2.text}".trim())
                setResult(Activity.RESULT_OK, result)
            } else {
                Toast.makeText(this, "✅ Client saved!", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }

    private fun showExistingClients(etName: EditText) {
        val clients = db.getAllClients()
        if (clients.isEmpty()) return

        val names = clients.map { it["name"] ?: "" }.toTypedArray()

        android.app.AlertDialog.Builder(this)
            .setTitle("Select existing client or create new")
            .setItems(names) { _, which ->
                val client = clients[which]
                // إرجاع العميل الموجود مباشرة
                val result = Intent()
                result.putExtra("client_id",      client["id"]!!.toInt())
                result.putExtra("client_name",    client["name"] ?: "")
                result.putExtra("client_email",   client["email"] ?: "")
                result.putExtra("client_phone",   client["phone"] ?: "")
                result.putExtra("client_address",
                    "${client["address1"]}\n${client["address2"]}".trim())
                setResult(Activity.RESULT_OK, result)
                finish()
            }
            .setNegativeButton("Create New", null)
            .show()
    }
}