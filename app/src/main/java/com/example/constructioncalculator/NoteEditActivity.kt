package com.example.constructioncalculator

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.constructioncalculator.databinding.ActivityNoteEditBinding
import java.text.SimpleDateFormat
import java.util.*

class NoteEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteEditBinding
    private var existingNote: Note? = null
    private var selectedColor: Int = Color.BLACK

    private val colors = listOf(
        Color.parseColor("#e67e22"),
        Color.parseColor("#8e44ad"),
        Color.parseColor("#27ae60"),
        Color.parseColor("#2980b9"),
        Color.parseColor("#c0392b"),
        Color.BLACK
    )

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            uri?.let {
                Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val pickFile = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            uri?.let {
                Toast.makeText(this, "File selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

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

        // Back
        binding.btnBack.setOnClickListener { finish() }

        // Save Top
        binding.btnSaveTop.setOnClickListener { saveNote() }

        // Save Bottom
        binding.btnSave.setOnClickListener { saveNote() }

        // Attach Panel
        binding.btnAttach.setOnClickListener {
            binding.attachPanel.visibility =
                if (binding.attachPanel.visibility == View.GONE) View.VISIBLE
                else View.GONE
        }

        // Image
        binding.btnAddImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImage.launch(intent)
        }

        // File
        binding.btnAddFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            pickFile.launch(intent)
        }
        // Link
        binding.btnAddLink.setOnClickListener {
            val input = EditText(this)
            input.hint = "https://..."
            AlertDialog.Builder(this)
                .setTitle("Add Link")
                .setView(input)
                .setPositiveButton("Add") { _, _ ->
                    val link = input.text.toString().trim()
                    if (link.isNotEmpty()) {
                        val current = binding.etBody.text.toString()
                        binding.etBody.setText("$current\n$link")
                        Toast.makeText(this, "Link added", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
                .show()
        }

        // Share
        binding.btnShare.setOnClickListener {
            val shareText = buildString {
                append(binding.etTitle.text.toString())
                append("\n")
                append(binding.etSubtitle.text.toString())
                append("\n")
                append(binding.etBody.text.toString())
            }
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, shareText)
            startActivity(Intent.createChooser(intent, "Share Note"))
        }
    }

    private fun saveNote() {
        val title    = binding.etTitle.text.toString().trim()
        val subtitle = binding.etSubtitle.text.toString().trim()
        val body     = binding.etBody.text.toString().trim()

        if (title.isEmpty() && body.isEmpty()) {
            Toast.makeText(this, "Please write a title or note", Toast.LENGTH_SHORT).show()
            return
        }

        val date = SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.getDefault()).format(Date())

        if (existingNote == null) {
            NoteManager.add(Note(
                title    = title,
                subtitle = subtitle,
                body     = body,
                color    = selectedColor,
                date     = date
            ))
        } else {
            existingNote!!.apply {
                this.title    = title
                this.subtitle = subtitle
                this.body     = body
                this.color    = selectedColor
            }
            NoteManager.update(existingNote!!)
        }

        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
        finish()
    }
}