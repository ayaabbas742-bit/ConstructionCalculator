package com.example.constructioncalculator

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class CreateInvoiceActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    private var selectedClientId   = -1
    private var selectedBusinessId = 1
    private var editInvoiceId = -1
    private val items = mutableListOf<InvoiceItem>()

    private lateinit var tvInvoiceNumber: TextView
    private lateinit var tvCreatedDate:   TextView
    private lateinit var tvDueDate:       TextView
    private lateinit var tvClientName:    TextView
    private lateinit var tvItemsList:     TextView
    private lateinit var tvSubtotal:      TextView
    private lateinit var tvDiscountVal:   TextView
    private lateinit var tvTaxVal:        TextView
    private lateinit var tvShippingVal:   TextView
    private lateinit var tvTotalVal:      TextView
    private lateinit var etNotes:         EditText
    private lateinit var tvPaymentVal:    TextView
    private lateinit var tvTermsVal:      TextView
    private lateinit var tvSignatureVal:  TextView

    private var discountPct   = 0.0
    private var taxName       = ""
    private var taxPct        = 0.0
    private var shippingVal   = 0.0
    private var paymentMethod = ""
    private var termsText     = ""
    private var signaturePath = ""

    data class InvoiceItem(
        val name: String,
        val quantity: Double,
        val price: Double,
        val discount: Double,
        val tax: Double,
        val description: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_invoice)

        db = DatabaseHelper(this)

        tvInvoiceNumber = findViewById(R.id.tvInvoiceNumber)
        tvCreatedDate   = findViewById(R.id.tvCreatedDate)
        tvDueDate       = findViewById(R.id.tvDueDate)
        tvClientName    = findViewById(R.id.tvClientName)
        tvItemsList     = findViewById(R.id.tvItemsList)
        tvSubtotal      = findViewById(R.id.tvSubtotal)
        tvDiscountVal   = findViewById(R.id.tvDiscountVal)
        tvTaxVal        = findViewById(R.id.tvTaxVal)
        tvShippingVal   = findViewById(R.id.tvShippingVal)
        tvTotalVal      = findViewById(R.id.tvTotalVal)
        etNotes         = findViewById(R.id.etNotes)
        tvPaymentVal    = findViewById(R.id.tvPaymentVal)
        tvTermsVal      = findViewById(R.id.tvTermsVal)
        tvSignatureVal  = findViewById(R.id.tvSignatureVal)

        tvInvoiceNumber.text = db.getNextInvoiceNumber()

        val today = java.text.SimpleDateFormat(
            "yyyy/MM/dd", java.util.Locale.getDefault()
        ).format(java.util.Date())
        tvCreatedDate.text = today
        tvDueDate.text     = today

        tvCreatedDate.setOnClickListener { showDatePicker(tvCreatedDate) }
        tvDueDate.setOnClickListener    { showDatePicker(tvDueDate) }

        findViewById<LinearLayout>(R.id.layoutBusiness).setOnClickListener {
            startActivity(Intent(this, BusinessActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.layoutClient).setOnClickListener {
            val intent = Intent(this, ClientActivity::class.java)
            intent.putExtra("select_mode", true)
            startActivityForResult(intent, 200)
        }

        findViewById<Button>(R.id.btnAddItem).setOnClickListener {
            startActivityForResult(Intent(this, AddItemActivity::class.java), 100)
        }

        findViewById<LinearLayout>(R.id.layoutDiscount).setOnClickListener {
            showDiscountDialog()
        }

        findViewById<LinearLayout>(R.id.layoutTax).setOnClickListener {
            showTaxDialog()
        }

        findViewById<LinearLayout>(R.id.layoutShipping).setOnClickListener {
            showShippingDialog()
        }
        findViewById<LinearLayout>(R.id.layoutSignature).setOnClickListener {
            startActivityForResult(Intent(this, SignatureActivity::class.java), 300)
        }

        findViewById<LinearLayout>(R.id.layoutPayment).setOnClickListener {
            showPaymentDialog()
        }

        findViewById<LinearLayout>(R.id.layoutTerms).setOnClickListener {
            showTermsDialog()
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<ImageView>(R.id.btnPreview).setOnClickListener {
            saveAndPreview()
        }

        // بعد - Save يحفظ ويفتح الفاتورة مباشرة
        findViewById<TextView>(R.id.btnSave).setOnClickListener {
            saveAndPreview()
        }
    }

    // ==================== DIALOGS ====================

    private fun showDiscountDialog() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 30, 50, 10)

        val title = TextView(this)
        title.text = "Discount"
        title.textSize = 18f
        title.setTextColor(android.graphics.Color.parseColor("#2196F3"))
        layout.addView(title)

        val divider = android.view.View(this)
        divider.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 2)
        divider.setBackgroundColor(android.graphics.Color.LTGRAY)
        layout.addView(divider)

        val etDiscount = EditText(this)
        etDiscount.hint = "Enter discount percentage"
        etDiscount.inputType = InputType.TYPE_CLASS_NUMBER or
                InputType.TYPE_NUMBER_FLAG_DECIMAL
        etDiscount.setText(if (discountPct > 0) discountPct.toString() else "")
        etDiscount.gravity = Gravity.END
        etDiscount.setPadding(20, 20, 20, 20)
        layout.addView(etDiscount)

        AlertDialog.Builder(this)
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                discountPct = etDiscount.text.toString().toDoubleOrNull() ?: 0.0
                tvDiscountVal.text = "Discount (${discountPct}%)"
                updateTotals()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showTaxDialog() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 30, 50, 10)

        val title = TextView(this)
        title.text = "Tax"
        title.textSize = 18f
        title.setTextColor(android.graphics.Color.parseColor("#2196F3"))
        layout.addView(title)

        val divider = android.view.View(this)
        divider.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 2)
        divider.setBackgroundColor(android.graphics.Color.LTGRAY)
        layout.addView(divider)

        val etTaxName = EditText(this)
        etTaxName.hint = "Enter tax name"
        etTaxName.setText(taxName)
        etTaxName.gravity = Gravity.END
        etTaxName.setPadding(20, 20, 20, 20)
        layout.addView(etTaxName)

        val etTaxPct = EditText(this)
        etTaxPct.hint = "Enter tax percentage"
        etTaxPct.inputType = InputType.TYPE_CLASS_NUMBER or
                InputType.TYPE_NUMBER_FLAG_DECIMAL
        etTaxPct.setText(if (taxPct > 0) taxPct.toString() else "")
        etTaxPct.gravity = Gravity.END
        etTaxPct.setPadding(20, 20, 20, 20)
        layout.addView(etTaxPct)

        AlertDialog.Builder(this)
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                taxName = etTaxName.text.toString()
                taxPct  = etTaxPct.text.toString().toDoubleOrNull() ?: 0.0
                tvTaxVal.text = "$taxName (${taxPct}%)"
                updateTotals()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showShippingDialog() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 30, 50, 10)
        val title = TextView(this)
        title.text = "Shipping"
        title.textSize = 18f
        title.setTextColor(android.graphics.Color.parseColor("#2196F3"))
        layout.addView(title)

        val divider = android.view.View(this)
        divider.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 2)
        divider.setBackgroundColor(android.graphics.Color.LTGRAY)
        layout.addView(divider)

        val etShipping = EditText(this)
        etShipping.hint = "Enter shipping amount"
        etShipping.inputType = InputType.TYPE_CLASS_NUMBER or
                InputType.TYPE_NUMBER_FLAG_DECIMAL
        etShipping.setText(if (shippingVal > 0) shippingVal.toString() else "")
        etShipping.gravity = Gravity.END
        etShipping.setPadding(20, 20, 20, 20)
        layout.addView(etShipping)

        AlertDialog.Builder(this)
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                shippingVal = etShipping.text.toString().toDoubleOrNull() ?: 0.0
                tvShippingVal.text = "Shipping: ${"%.2f".format(shippingVal)}"
                updateTotals()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPaymentDialog() {
        val options = arrayOf(
            "💵 Cash",
            "💳 Card",
            "🏦 Bank Transfer",
            "📝 Check",
            "📱 Mobile Payment",
            "✏️ Custom..."
        )

        android.app.AlertDialog.Builder(this)
            .setTitle("Payment Method")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        paymentMethod = "Cash"
                        tvPaymentVal.text = "💵 Cash"
                    }
                    1 -> {
                        paymentMethod = "Card"
                        tvPaymentVal.text = "💳 Card"
                    }
                    2 -> {
                        paymentMethod = "Bank Transfer"
                        tvPaymentVal.text = "🏦 Bank Transfer"
                    }
                    3 -> {
                        paymentMethod = "Check"
                        tvPaymentVal.text = "📝 Check"
                    }
                    4 -> {
                        paymentMethod = "Mobile Payment"
                        tvPaymentVal.text = "📱 Mobile Payment"
                    }
                    5 -> {
                        // كتابة يدوية
                        showCustomPaymentDialog()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCustomPaymentDialog() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 30, 50, 10)

        val title = TextView(this)
        title.text = "Payment Method"
        title.textSize = 18f
        title.setTextColor(android.graphics.Color.parseColor("#2196F3"))
        layout.addView(title)

        val divider = android.view.View(this)
        divider.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 2)
        divider.setBackgroundColor(android.graphics.Color.LTGRAY)
        layout.addView(divider)

        val etPayment = EditText(this)
        etPayment.hint = "Enter payment method"
        etPayment.setText(paymentMethod)
        etPayment.gravity = Gravity.END
        etPayment.minLines = 3
        etPayment.setPadding(20, 20, 20, 20)
        etPayment.filters = arrayOf(android.text.InputFilter.LengthFilter(100))
        layout.addView(etPayment)

        val tvCount = TextView(this)
        tvCount.text = "${paymentMethod.length}/100"
        tvCount.gravity = Gravity.END
        layout.addView(tvCount)

        etPayment.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                tvCount.text = "${s?.length ?: 0}/100"
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
        })

        android.app.AlertDialog.Builder(this)
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                paymentMethod = etPayment.text.toString()
                tvPaymentVal.text = if (paymentMethod.isEmpty())
                    "Add payment method" else paymentMethod
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showTermsDialog() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 30, 50, 10)

        val title = TextView(this)
        title.text = "Terms and Conditions"
        title.textSize = 18f
        title.setTextColor(android.graphics.Color.parseColor("#2196F3"))
        layout.addView(title)

        val divider = android.view.View(this)
        divider.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 2)
        divider.setBackgroundColor(android.graphics.Color.LTGRAY)
        layout.addView(divider)
        val etTerms = EditText(this)
        etTerms.hint = "Enter Terms and Conditions"
        etTerms.setText(termsText)
        etTerms.gravity = Gravity.END
        etTerms.minLines = 3
        etTerms.setPadding(20, 20, 20, 20)
        etTerms.filters = arrayOf(android.text.InputFilter.LengthFilter(100))
        layout.addView(etTerms)

        val tvCount = TextView(this)
        tvCount.text = "${termsText.length}/100"
        tvCount.gravity = Gravity.END
        layout.addView(tvCount)

        etTerms.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                tvCount.text = "${s?.length ?: 0}/100"
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
        })

        AlertDialog.Builder(this)
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                termsText = etTerms.text.toString()
                tvTermsVal.text = if (termsText.isEmpty())
                    "Add terms and conditions" else termsText
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ==================== HELPERS ====================

    private fun showDatePicker(tv: TextView) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            tv.text = "%04d/%02d/%02d".format(y, m + 1, d)
        }, cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun updateTotals() {
        var subtotal = 0.0
        for (item in items) {
            subtotal += item.quantity * item.price *
                    (1 - item.discount / 100) *
                    (1 + item.tax / 100)
        }
        val discountAmt = subtotal * discountPct / 100
        val taxAmt      = (subtotal - discountAmt) * taxPct / 100
        val total       = subtotal - discountAmt + taxAmt + shippingVal

        tvSubtotal.text    = "Subtotal: ${"%.2f".format(subtotal)}"
        tvDiscountVal.text = "Discount (${discountPct}%): -${"%.2f".format(discountAmt)}"
        tvTaxVal.text      = "$taxName (${taxPct}%): ${"%.2f".format(taxAmt)}"
        tvShippingVal.text = "Shipping: ${"%.2f".format(shippingVal)}"
        tvTotalVal.text    = "Total: ${"%.2f".format(total)}"
    }

    private fun updateItemsList() {
        val sb = StringBuilder()
        for (item in items) {
            val total = item.quantity * item.price *
                    (1 - item.discount / 100) *
                    (1 + item.tax / 100)
            sb.append("• ${item.name}  " +
                    "${item.quantity}×${item.price}" +
                    " = ${"%.2f".format(total)}\n")
        }
        tvItemsList.text = sb.toString().ifEmpty { "Add items" }
        updateTotals()
    }

    // ==================== SAVE ====================

    private fun saveInvoice(): Long {
        if (selectedClientId == -1) {
            Toast.makeText(this, "Please select a client!", Toast.LENGTH_SHORT).show()
            return -1
        }
        if (items.isEmpty()) {
            Toast.makeText(this, "Please add at least one item!", Toast.LENGTH_SHORT).show()
            return -1
        }

        if (editInvoiceId != -1) {
            db.deleteInvoice(editInvoiceId)
        }

        val invoiceId = db.saveInvoice(
            invoiceNumber = tvInvoiceNumber.text.toString(),
            createdDate   = tvCreatedDate.text.toString(),
            dueDate       = tvDueDate.text.toString(),
            businessId    = selectedBusinessId,
            clientId      = selectedClientId,
            discount      = discountPct,
            taxName       = taxName,
            taxPercent    = taxPct,
            shipping      = shippingVal,
            paymentMethod = paymentMethod,
            terms         = termsText,
            notes         = etNotes.text.toString(),
            signature     = signaturePath
        )

        for (item in items) {
            db.saveItem(
                invoiceId   = invoiceId,
                name        = item.name,
                quantity    = item.quantity,
                price       = item.price,
                discount    = item.discount,
                tax         = item.tax,
                description = item.description
            )
        }
        val msg = if (editInvoiceId != -1) "✅ Invoice updated!" else "✅ Invoice saved!"
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        return invoiceId
    }

    private fun saveAndPreview() {
        val id = saveInvoice()
        if (id != -1L) {
            val intent = Intent(this, PreviewInvoiceActivity::class.java)
            intent.putExtra("invoice_id", id.toInt())
            startActivity(intent)
        }
    }

    // ==================== ACTIVITY RESULTS ====================

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            items.add(InvoiceItem(
                name        = data.getStringExtra("name") ?: "",
                quantity    = data.getDoubleExtra("quantity", 1.0),
                price       = data.getDoubleExtra("price", 0.0),
                discount    = data.getDoubleExtra("discount", 0.0),
                tax         = data.getDoubleExtra("tax", 0.0),
                description = data.getStringExtra("description") ?: ""
            ))
            updateItemsList()
        }

        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            selectedClientId  = data.getIntExtra("client_id", -1)
            tvClientName.text = data.getStringExtra("client_name") ?: ""
        }

        if (requestCode == 300 && resultCode == RESULT_OK && data != null) {
            signaturePath = data.getStringExtra("signature_path") ?: ""
            tvSignatureVal.text = if (signaturePath.isEmpty())
                "Add signature" else "✅ Signature added"
        }
    }
}