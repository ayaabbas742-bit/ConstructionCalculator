package com.example.constructioncalculator

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class BusinessActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var logoPath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business)

        db = DatabaseHelper(this)

        val etName    = findViewById<EditText>(R.id.etName)
        val etEmail   = findViewById<EditText>(R.id.etEmail)
        val etPhone   = findViewById<EditText>(R.id.etPhone)
        val etAddress = findViewById<EditText>(R.id.etAddress)
        val etWebsite = findViewById<EditText>(R.id.etWebsite)
        val imgLogo   = findViewById<ImageView>(R.id.imgLogo)

        // تحميل البيانات الموجودة
        val business = db.getBusiness()
        if (business.isNotEmpty()) {
            etName.setText(business["name"])
            etEmail.setText(business["email"])
            etPhone.setText(business["phone"])
            etAddress.setText(business["address"])
            etWebsite.setText(business["website"])
            logoPath = business["logo"] ?: ""
        }

        // اختيار شعار
        imgLogo.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        // رجوع
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        // حفظ
        findViewById<TextView>(R.id.btnSave).setOnClickListener {
            if (etName.text.toString().isEmpty()) {
                Toast.makeText(this, "أدخل اسم العمل!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            db.saveBusiness(
                name    = etName.text.toString(),
                email   = etEmail.text.toString(),
                phone   = etPhone.text.toString(),
                address = etAddress.text.toString(),
                website = etWebsite.text.toString(),
                logo    = logoPath
            )
            Toast.makeText(this, "✅ تم الحفظ!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            logoPath = uri.toString()
            findViewById<ImageView>(R.id.imgLogo).setImageURI(uri)
        }
    }
}