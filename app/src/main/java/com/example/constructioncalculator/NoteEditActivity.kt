package com.example.constructioncalculator

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.constructioncalculator.databinding.ActivityNoteEditBinding
import java.text.SimpleDateFormat
import java.util.*

class NoteEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteEditBinding
    private var existingNote: Note? = null
    private var selectedColor: Int = Color.BLACK

    private val colors = listOf(
        Color.BLACK,
        Color.parseColor("#c0392b"),
        Color.parseColor("#2980b9"),
        Color.parseColor("#27ae60"),
        Color.parseColor("#8e44ad"),
        Color.parseColor("#e67e22")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            title = "ملاحظات البناء"
            setDisplayHomeAsUpEnabled(true)
        }

        existingNote = intent.getSerializableExtra("note") as? Note
        existingNote?.let {
            binding.etTitle.setText(it.title)
            binding.etSubtitle.setText(it.subtitle)
            binding.etBody.setText(it.body)
            selectedColor = it.color
            updateSelectedColorUI()
        }

        setupColorPicker()
        setupButtons()
    }

    private fun setupColorPicker() {
        val colorViews = listOf(
            binding.color1, binding.color2, binding.color3,
            binding.color4, binding.color5, binding.color6
        )
        colorViews.forEachIndexed { index, view ->
            view.setBackgroundColor(colors[index])
            view.setOnClickListener {
                selectedColor = colors[index]
                updateSelectedColorUI()
            }
        }
    }

    private fun updateSelectedColorUI() {
        binding.viewSelectedColor.setBackgroundColor(selectedColor)
    }

    private fun setupButtons() {
        binding.btnSave.setOnClickListener { saveNote() }

        binding.btnAttach.setOnClickListener {
            binding.attachPanel.visibility =
                if (binding.attachPanel.visibility == View.GONE) View.VISIBLE
                else View.GONE
        }

        binding.btnAddImage.setOnClickListener {
            Toast.makeText(this, "إضافة صورة", Toast.LENGTH_SHORT).show()
        }

        binding.btnAddLink.setOnClickListener {
            Toast.makeText(this, "إضافة رابط", Toast.LENGTH_SHORT).show()
        }

        binding.btnAddFile.setOnClickListener {
            Toast.makeText(this, "إرفاق ملف", Toast.LENGTH_SHORT).show()
        }

        binding.btnShare.setOnClickListener {
            val shareText = "${binding.etTitle.text}\n${binding.etBody.text}"
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(android.content.Intent.EXTRA_TEXT, shareText)
            startActivity(android.content.Intent.createChooser(intent, "مشاركة الملاحظة"))
        }
    }

    private fun saveNote() {
        val title    = binding.etTitle.text.toString().trim()
        val subtitle = binding.etSubtitle.text.toString().trim()
        val body     = binding.etBody.text.toString().trim()

        if (title.isEmpty() && body.isEmpty()) {
            Toast.makeText(this, "يرجى كتابة عنوان أو ملاحظة", Toast.LENGTH_SHORT).show()
            return
        }

        val date = SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.getDefault()).format(Date())

        if (existingNote == null) {
            NoteManager.add(Note(title = title, subtitle = subtitle,
                body = body, color = selectedColor, date = date))
        } else {
            existingNote!!.apply {
                this.title    = title
                this.subtitle = subtitle
                this.body     = body
                this.color    = selectedColor
            }
            NoteManager.update(existingNote!!)
        }

        Toast.makeText(this, "تم الحفظ", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}