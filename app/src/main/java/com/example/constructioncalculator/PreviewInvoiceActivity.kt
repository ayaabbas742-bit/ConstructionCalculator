package com.example.constructioncalculator

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class PreviewInvoiceActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var invoiceId = -1
    private var invoice   = mapOf<String, String>()
    private var items     = listOf<Map<String, String>>()
    private var business  = mapOf<String, String>()
    private var client    = mapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview_invoice)

        db        = DatabaseHelper(this)
        invoiceId = intent.getIntExtra("invoice_id", -1)
        if (invoiceId == -1) { finish(); return }

        // جلب كل البيانات من SQLite
        invoice  = db.getInvoiceById(invoiceId)
        items    = db.getItemsByInvoice(invoiceId)
        business = db.getBusiness()

        // جلب بيانات العميل
        val clientId = invoice["client_id"]?.toIntOrNull() ?: -1
        if (clientId != -1) {
            val clients = db.getAllClients()
            client = clients.find { it["id"] == clientId.toString() }
                ?: mapOf()
        }

        loadData()

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<ImageView>(R.id.btnShare).setOnClickListener { shareInvoice() }
        findViewById<Button>(R.id.btnShareBottom).setOnClickListener { shareInvoice() }
        findViewById<Button>(R.id.btnWhatsApp).setOnClickListener { shareViaWhatsApp() }
    }

    private fun loadData() {
        // اسم الشركة
        findViewById<TextView>(R.id.tvBusinessName).text =
            business["name"] ?: ""

        // رقم الفاتورة
        findViewById<TextView>(R.id.tvInvoiceNumber).text =
            "Invoice No : ${invoice["invoice_number"]}"
        findViewById<TextView>(R.id.tvCreatedDate).text =
            "Created Date : ${invoice["created_date"]}"
        findViewById<TextView>(R.id.tvDueDate).text =
            "Due Date : ${invoice["due_date"]}"

        // بيانات العميل
        findViewById<TextView>(R.id.tvClientName).text =
            client["name"] ?: invoice["client_name"] ?: ""
        findViewById<TextView>(R.id.tvClientPhone).text =
            client["phone"] ?: ""
        findViewById<TextView>(R.id.tvClientEmail).text =
            client["email"] ?: ""
        findViewById<TextView>(R.id.tvClientAddress).text =
            "${client["address1"] ?: ""}\n${client["address2"] ?: ""}".trim()

        // بيانات الشركة
        findViewById<TextView>(R.id.tvBusinessPhone).text =
            business["phone"] ?: ""
        findViewById<TextView>(R.id.tvBusinessEmail).text =
            business["email"] ?: ""
        findViewById<TextView>(R.id.tvBusinessWebsite).text =
            business["website"] ?: ""
        findViewById<TextView>(R.id.tvBusinessAddress).text =
            business["address"] ?: ""

        // عناصر الفاتورة
        val itemsContainer = findViewById<LinearLayout>(R.id.itemsContainer)
        itemsContainer.removeAllViews()

        var subtotal = 0.0
        for (item in items) {
            val qty      = item["quantity"]!!.toDouble()
            val price    = item["price"]!!.toDouble()
            val discount = item["discount"]!!.toDouble()
            val tax      = item["tax"]!!.toDouble()
            val total    = qty * price * (1 - discount/100) * (1 + tax/100)
            subtotal    += total

            // صف العنصر
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.setPadding(16, 12, 16, 12)

            val tvName = TextView(this)
            tvName.text = item["name"]
            tvName.layoutParams = LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
            val tvQty = TextView(this)
            tvQty.text = qty.toInt().toString()
            tvQty.gravity = android.view.Gravity.CENTER
            tvQty.layoutParams = LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

            val tvPrice = TextView(this)
            tvPrice.text = "PKR ${"%.0f".format(price)}"
            tvPrice.gravity = android.view.Gravity.CENTER
            tvPrice.layoutParams = LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

            val tvDisc = TextView(this)
            tvDisc.text = "${discount.toInt()}%"
            tvDisc.gravity = android.view.Gravity.CENTER
            tvDisc.layoutParams = LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

            val tvTax = TextView(this)
            tvTax.text = "${tax.toInt()}%"
            tvTax.gravity = android.view.Gravity.CENTER
            tvTax.layoutParams = LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

            val tvTotal = TextView(this)
            tvTotal.text = "PKR ${"%.0f".format(total)}"
            tvTotal.gravity = android.view.Gravity.END
            tvTotal.layoutParams = LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

            row.addView(tvName)
            row.addView(tvQty)
            row.addView(tvPrice)
            row.addView(tvDisc)
            row.addView(tvTax)
            row.addView(tvTotal)

            // تلوين الصفوف
            if (items.indexOf(item) % 2 == 0)
                row.setBackgroundColor(android.graphics.Color.parseColor("#F5F5F5"))

            itemsContainer.addView(row)
        }

        // الحسابات
        val discount    = invoice["discount"]!!.toDouble()
        val taxPct      = invoice["tax_percent"]!!.toDouble()
        val taxName     = invoice["tax_name"] ?: "Tax"
        val shipping    = invoice["shipping"]!!.toDouble()
        val discountAmt = subtotal * discount / 100
        val taxAmt      = (subtotal - discountAmt) * taxPct / 100
        val total       = subtotal - discountAmt + taxAmt + shipping

        findViewById<TextView>(R.id.tvSubtotal).text =
            "PKR ${"%.0f".format(subtotal)}"
        findViewById<TextView>(R.id.tvDiscountAmt).text =
            "PKR ${"%.0f".format(discountAmt)}"
        findViewById<TextView>(R.id.tvTaxLabel).text =
            "$taxName (${taxPct}%)"
        findViewById<TextView>(R.id.tvTaxAmt).text =
            "PKR ${"%.1f".format(taxAmt)}"
        findViewById<TextView>(R.id.tvShippingAmt).text =
            "PKR ${"%.0f".format(shipping)}"
        findViewById<TextView>(R.id.tvGrandTotal).text =
            "PKR ${"%.1f".format(total)}"

        // التوقيع
        val sigPath = invoice["signature"] ?: ""
        if (sigPath.isNotEmpty()) {
            val sigView = findViewById<ImageView>(R.id.imgSignature)
            sigView.setImageBitmap(BitmapFactory.decodeFile(sigPath))
            sigView.visibility = android.view.View.VISIBLE
        }

        // الشروط
        val terms = invoice["terms"] ?: ""
        if (terms.isNotEmpty()) {
            findViewById<TextView>(R.id.tvTerms).text = terms
        }

        // طريقة الدفع - IBAN
        val payment = invoice["payment_method"] ?: ""
        if (payment.isNotEmpty()) {
            findViewById<TextView>(R.id.tvPayment).text = payment
        }
    }

    // ==================== SHARE ====================

    private fun buildInvoiceText(): String {
        val sb = StringBuilder()
        sb.append("*${business["name"]}*\n")
        sb.append("================================\n")
        sb.append("Invoice No : ${invoice["invoice_number"]}\n")
        sb.append("Date : ${invoice["created_date"]}\n")
        sb.append("Due  : ${invoice["due_date"]}\n")
        sb.append("Client : ${client["name"] ?: invoice["client_name"]}\n")
        if (!client["phone"].isNullOrEmpty())
            sb.append("Phone : ${client["phone"]}\n")
        sb.append("================================\n")

        var subtotal = 0.0
        for (item in items) {
            val qty   = item["quantity"]!!.toDouble()
            val price = item["price"]!!.toDouble()
            val disc  = item["discount"]!!.toDouble()
            val tax   = item["tax"]!!.toDouble()
            val total = qty * price * (1 - disc/100) * (1 + tax/100)
            subtotal += total
            sb.append("• ${item["name"]}  " +
                    "${qty.toInt()} × PKR ${"%.0f".format(price)}" +
                    " = PKR ${"%.0f".format(total)}\n")
        }

        val discount    = invoice["discount"]!!.toDouble()
        val taxPct      = invoice["tax_percent"]!!.toDouble()
        val taxName     = invoice["tax_name"] ?: "Tax"
        val shipping    = invoice["shipping"]!!.toDouble()
        val discountAmt = subtotal * discount / 100
        val taxAmt      = (subtotal - discountAmt) * taxPct / 100
        val total       = subtotal - discountAmt + taxAmt + shipping

        sb.append("================================\n")
        sb.append("Subtotal : PKR ${"%.0f".format(subtotal)}\n")
        if (discount > 0)
            sb.append("Discount (${discount}%) : PKR ${"%.0f".format(discountAmt)}\n")
        if (taxPct > 0)
            sb.append("$taxName (${taxPct}%) : PKR ${"%.1f".format(taxAmt)}\n")
        if (shipping > 0)
            sb.append("Shipping : PKR ${"%.0f".format(shipping)}\n")
        sb.append("================================\n")
        sb.append("*Total : PKR ${"%.1f".format(total)}*\n")
        sb.append("================================\n")

        if (!invoice["payment_method"].isNullOrEmpty())
            sb.append("Payment : ${invoice["payment_method"]}\n")
        if (!invoice["terms"].isNullOrEmpty())
            sb.append("Terms : ${invoice["terms"]}\n")
        if (!business["email"].isNullOrEmpty())
            sb.append("Email : ${business["email"]}\n")
        if (!business["phone"].isNullOrEmpty())
            sb.append("Phone : ${business["phone"]}\n")
        if (!business["website"].isNullOrEmpty())
            sb.append("Web : ${business["website"]}\n")

        return sb.toString()
    }

    private fun shareInvoice() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT,
            "Invoice ${invoice["invoice_number"]}")
        shareIntent.putExtra(Intent.EXTRA_TEXT, buildInvoiceText())
        startActivity(Intent.createChooser(shareIntent, "Share Invoice via"))
    }

    private fun shareViaWhatsApp() {
        try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.setPackage("com.whatsapp")
            intent.putExtra(Intent.EXTRA_TEXT, buildInvoiceText())
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "WhatsApp not installed!", Toast.LENGTH_SHORT).show()
        }
    }
}