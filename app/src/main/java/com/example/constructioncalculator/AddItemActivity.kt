package com.example.constructioncalculator

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class AddItemActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)

        val etName     = findViewById<EditText>(R.id.etName)
        val etQty      = findViewById<EditText>(R.id.etQty)
        val etPrice    = findViewById<EditText>(R.id.etPrice)
        val etDiscount = findViewById<EditText>(R.id.etDiscount)
        val etTax      = findViewById<EditText>(R.id.etTax)
        val etDesc     = findViewById<EditText>(R.id.etDesc)
        val tvAmount   = findViewById<TextView>(R.id.tvAmount)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        // حساب المبلغ تلقائياً
        val watcher = object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                val qty      = etQty.text.toString().toDoubleOrNull() ?: 1.0
                val price    = etPrice.text.toString().toDoubleOrNull() ?: 0.0
                val discount = etDiscount.text.toString().toDoubleOrNull() ?: 0.0
                val tax      = etTax.text.toString().toDoubleOrNull() ?: 0.0
                val amount   = qty * price * (1 - discount/100) * (1 + tax/100)
                tvAmount.text = "مبلغ ${"%.2f".format(amount)}"
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
        }

        etQty.addTextChangedListener(watcher)
        etPrice.addTextChangedListener(watcher)
        etDiscount.addTextChangedListener(watcher)
        etTax.addTextChangedListener(watcher)

        // حفظ
        findViewById<TextView>(R.id.btnSave).setOnClickListener {
            if (etName.text.toString().isEmpty()) {
                Toast.makeText(this, "أدخل اسم العنصر!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent()
            intent.putExtra("name",        etName.text.toString())
            intent.putExtra("quantity",    etQty.text.toString().toDoubleOrNull() ?: 1.0)
            intent.putExtra("price",       etPrice.text.toString().toDoubleOrNull() ?: 0.0)
            intent.putExtra("discount",    etDiscount.text.toString().toDoubleOrNull() ?: 0.0)
            intent.putExtra("tax",         etTax.text.toString().toDoubleOrNull() ?: 0.0)
            intent.putExtra("description", etDesc.text.toString())
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}