package com.example.constructioncalculator

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class InvoiceListActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var listView: ListView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var btnAddEmpty: Button
    private lateinit var btnAddFab: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invoice_list)

        db          = DatabaseHelper(this)
        listView    = findViewById(R.id.listView)
        layoutEmpty = findViewById(R.id.layoutEmpty)
        btnAddEmpty = findViewById(R.id.btnAddInvoice)
        btnAddFab   = findViewById(R.id.btnFab)

        // زر إضافة فاتورة من الشاشة الفارغة
        btnAddEmpty.setOnClickListener {
            startActivity(Intent(this, CreateInvoiceActivity::class.java))
        }

        // زر + في الأسفل
        btnAddFab.setOnClickListener {
            startActivity(Intent(this, CreateInvoiceActivity::class.java))
        }

        // فتح الفاتورة عند الضغط
        listView.setOnItemClickListener { _, _, position, _ ->
            val invoices = db.getAllInvoices()
            val id = invoices[position]["id"]!!.toInt()
            val intent = Intent(this, PreviewInvoiceActivity::class.java)
            intent.putExtra("invoice_id", id)
            startActivity(intent)
        }

        // حذف عند الضغط الطويل
        listView.setOnItemLongClickListener { _, _, position, _ ->
            val invoices = db.getAllInvoices()
            val id = invoices[position]["id"]!!.toInt()
            val number = invoices[position]["invoice_number"] ?: ""

            AlertDialog.Builder(this)
                .setTitle("Delete Invoice")
                .setMessage("Delete invoice $number?")
                .setPositiveButton("Delete") { _, _ ->
                    db.deleteInvoice(id)
                    Toast.makeText(this, "🗑️ Deleted!", Toast.LENGTH_SHORT).show()
                    loadInvoices()
                }
                .setNegativeButton("Cancel", null)
                .show()
            true
        }
    }

    override fun onResume() {
        super.onResume()
        loadInvoices()
    }

    private fun loadInvoices() {
        val invoices = db.getAllInvoices()

        if (invoices.isEmpty()) {
            layoutEmpty.visibility = View.VISIBLE
            listView.visibility    = View.GONE
            btnAddFab.visibility   = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            listView.visibility    = View.VISIBLE
            btnAddFab.visibility   = View.VISIBLE

            val items = invoices.map { inv ->
                val number  = inv["invoice_number"] ?: ""
                val client  = inv["client_name"] ?: "No client"
                val date    = inv["created_date"] ?: ""
                val due     = inv["due_date"] ?: ""
                "$number\nClient: $client\nDate: $date  |  Due: $due"
            }

            listView.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                items
            )
        }
    }
}