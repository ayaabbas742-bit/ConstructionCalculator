package com.example.constructioncalculator

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

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
            client = clients.find { it["id"] == clientId.toString() } ?: mapOf()
        }

        // بعد - ننتظر حتى يكتمل رسم الـ View
        window.decorView.post {
            loadData()
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        // ✅ الأزرار تشارك كصورة
        findViewById<ImageView>(R.id.btnShare).setOnClickListener { shareAsImage() }
        findViewById<Button>(R.id.btnShareBottom).setOnClickListener { shareAsImage() }
        findViewById<Button>(R.id.btnWhatsApp).setOnClickListener { shareViaWhatsAppImage() }
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

            if (items.indexOf(item) % 2 == 0)
                row.setBackgroundColor(android.graphics.Color.parseColor("#F3E5F5")) // بنفسجي فاتح
            else
                row.setBackgroundColor(android.graphics.Color.parseColor("#000000")) // أبيض

// وأضف padding أكبر
            row.setPadding(16, 14, 16, 14)

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

        // Signature
        // Signature
        val sigPath = invoice["signature"] ?: ""
        if (sigPath.isNotEmpty()) {
            val sigView = findViewById<ImageView>(R.id.imgSignature)
            val bitmap = BitmapFactory.decodeFile(sigPath)
            if (bitmap != null) {
                sigView.setImageBitmap(bitmap)
                sigView.visibility = View.VISIBLE
                sigView.bringToFront()
            }
        }
        // الشروط
        val terms = invoice["terms"] ?: ""
        if (terms.isNotEmpty()) {
            findViewById<TextView>(R.id.tvTerms).text = terms
        }

        // طريقة الدفع
        val payment = invoice["payment_method"] ?: ""
        if (payment.isNotEmpty()) {
            findViewById<TextView>(R.id.tvPayment).text = payment
        }
    }

// ==================== IMAGE SHARE ====================

// تحويل الـ View إلى Bitmap
private fun invoiceViewToBitmap(): Bitmap {
    val invoiceCard = findViewById<View>(R.id.invoiceCard)
    invoiceCard.measure(
        View.MeasureSpec.makeMeasureSpec(invoiceCard.width, View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    )
    invoiceCard.layout(0, 0, invoiceCard.measuredWidth, invoiceCard.measuredHeight)

    val bitmap = Bitmap.createBitmap(
        invoiceCard.measuredWidth,
        invoiceCard.measuredHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE)
    invoiceCard.draw(canvas)
    return bitmap
}

    // مشاركة كصورة
    private fun shareAsImage() {
        try {
            val bitmap = invoiceViewToBitmap()
            val file = File(cacheDir, "invoice_${invoice["invoice_number"]}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val uri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "image/png"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(intent, "Share Invoice"))
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // مشاركة عبر واتساب كصورة
    private fun shareViaWhatsAppImage() {
        try {
            val bitmap = invoiceViewToBitmap()
            val file = File(cacheDir, "invoice_${invoice["invoice_number"]}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val uri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "image/png"
            intent.setPackage("com.whatsapp")
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "WhatsApp not installed!", Toast.LENGTH_SHORT).show()
        }
    }
}
