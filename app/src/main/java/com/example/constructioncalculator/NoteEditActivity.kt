package com.example.constructioncalculator

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.constructioncalculator.databinding.ActivityNoteEditBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.util.Base64

class NoteEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteEditBinding
    private var existingNote: Note? = null
    private var selectedColor: Int = Color.BLACK
    private var selectedPriority = "low"
    private var isPinned = false
    private var cameraImageUri: Uri? = null

    // قوائم المرفقات في الذاكرة
    private val imagesList = mutableListOf<String>() // Base64
    private val filesList  = mutableListOf<Pair<String, String>>() // name to base64
    private val linksList  = mutableListOf<String>()

    private val colors = listOf(
        Color.parseColor("#e67e22"),
        Color.parseColor("#8e44ad"),
        Color.parseColor("#27ae60"),
        Color.parseColor("#2980b9"),
        Color.parseColor("#c0392b"),
        Color.BLACK
    )

    // ====== Gallery Launcher ======
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                Toast.makeText(this, "Loading image...", Toast.LENGTH_SHORT).show()
                Thread {
                    try {
                        val base64 = uriToBase64(uri)
                        if (base64 != null) {
                            runOnUiThread {
                                imagesList.add(base64)
                                refreshAttachmentViews()
                                Toast.makeText(this, "Image added", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this, "Image too large", Toast.LENGTH_SHORT).show()
                        }
                    }
                }.start()
            }
        }
    }

    // ====== Camera Launcher ======
    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraImageUri?.let { uri ->
                Toast.makeText(this, "Loading photo...", Toast.LENGTH_SHORT).show()
                Thread {
                    try {
                        val base64 = uriToBase64(uri)
                        if (base64 != null) {
                            runOnUiThread {
                                imagesList.add(base64)
                                refreshAttachmentViews()
                                Toast.makeText(this, "Photo added", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this, "Failed to load photo", Toast.LENGTH_SHORT).show()
                        }
                    }
                }.start()
            }
        }
    }
     // ====== File Launcher ======
    private val pickFile = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val name = getFileName(uri)
                Toast.makeText(this, "Loading file...", Toast.LENGTH_SHORT).show()
                Thread {
                    try {
                        val base64 = uriToBase64(uri)
                        if (base64 != null) {
                            runOnUiThread {
                                filesList.add(Pair(name, base64))
                                refreshAttachmentViews()
                                Toast.makeText(this, "File added: $name", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this, "Failed to load file", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this, "File too large or unsupported", Toast.LENGTH_SHORT).show()
                        }
                    }
                }.start()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val noteId = intent.getLongExtra("note_id", -1L)
        if (noteId != -1L) { existingNote = NoteManager.getById(noteId) }
        existingNote?.let {
            binding.etTitle.setText(it.title)
            binding.etSubtitle.setText(it.subtitle)
            binding.etBody.setText(it.body)
            selectedColor = it.color
            updateSelectedColorUI()


            // تحميل التثبيت
            isPinned = it.isPinned
            binding.switchPin.isChecked = isPinned

            // تحميل المرفقات
            if (it.images.isNotEmpty())
                imagesList.addAll(it.images.split("||").filter { s -> s.isNotEmpty() })
            if (it.files.isNotEmpty())
                it.files.split("||").filter { s -> s.isNotEmpty() }.forEach { entry ->
                    val parts = entry.split("::", limit = 2)
                    if (parts.size == 2) filesList.add(Pair(parts[0], parts[1]))
                }
            if (it.links.isNotEmpty())
                linksList.addAll(it.links.split("||").filter { s -> s.isNotEmpty() })

            refreshAttachmentViews()
        }

        setupColorPicker()
        setupWordCount()
        setupPin()
        setupButtons()
    }



    // ====== Word Count ======
    private fun setupWordCount() {
        binding.etBody.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val words = s?.trim()?.split("\\s+".toRegex())
                    ?.filter { it.isNotEmpty() }
                binding.tvWordCount.text = "${words?.size ?: 0} words"
            }
        })
    }

    // ====== Pin ======
    private fun setupPin() {
        binding.switchPin.setOnCheckedChangeListener { _, isChecked ->
            isPinned = isChecked
        }
    }

    // ====== Camera ======
    private fun openCamera() {
        val imageFile = File(
            getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "note_${System.currentTimeMillis()}.jpg"
        )
        cameraImageUri = FileProvider.getUriForFile(
            this, "${packageName}.provider", imageFile
        )
        cameraImageUri?.let { takePicture.launch(it) }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // ====== تحويل URI إلى Base64 ======
    private fun uriToBase64(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val bytes = inputStream.readBytes()
            inputStream.close()
            val mimeType = contentResolver.getType(uri) ?: ""
            if (mimeType.startsWith("image/")) {
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                val out = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 60, out)
                Base64.encodeToString(out.toByteArray(), Base64.DEFAULT)
            } else {
                if (bytes.size > 5 * 1024 * 1024) throw Exception("File too large")
                Base64.encodeToString(bytes, Base64.DEFAULT)
            }
        } catch (e: Exception) {
            null
        }
    }

    // ====== اسم الملف ======
    private fun getFileName(uri: Uri): String {
        var name = "file_${System.currentTimeMillis()}"
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) name = it.getString(idx)
            }
        }
        return name
    }

    // ====== عرض المرفقات ======
    private fun refreshAttachmentViews() {
        binding.attachmentContainer.removeAllViews()

        // الصور
        imagesList.forEachIndexed { index, base64 ->
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 8.dp, 0, 8.dp) }
        }

            val img = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(100.dp, 100.dp)
                    .apply { marginEnd = 12.dp }
                setBackgroundResource(android.R.drawable.picture_frame)
                setImageBitmap(bitmap)
                scaleType = ImageView.ScaleType.CENTER_CROP
                setOnClickListener { showImageDialog(bitmap) }
            }

            val btnDelete = ImageButton(this).apply {
                setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                background = null
                setOnClickListener {
                    imagesList.removeAt(index)
                    refreshAttachmentViews()
                }
            }

            row.addView(img)
            row.addView(btnDelete)
            binding.attachmentContainer.addView(row)
        }

        // الملفات
        filesList.forEachIndexed { index, (name, _) ->
            val chip = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(12.dp, 8.dp, 12.dp, 8.dp)
                setBackgroundResource(android.R.drawable.dialog_holo_light_frame)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 4.dp, 0, 4.dp) }
            }
            val tv = TextView(this).apply {
                text = "📄 $name"
                textSize = 13f
                setTextColor(Color.parseColor("#333333"))
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val btnDel = ImageButton(this).apply {
                setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                background = null
                setOnClickListener {
                    filesList.removeAt(index)
                    refreshAttachmentViews()
                }
            }
            chip.addView(tv)
            chip.addView(btnDel)
            binding.attachmentContainer.addView(chip)
        }

        // الروابط
        linksList.forEachIndexed { index, link ->
            val chip = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(12.dp, 8.dp, 12.dp, 8.dp)
            }
            val tv = TextView(this).apply {
                text = "🔗 $link"
                textSize = 13f
                setTextColor(Color.parseColor("#2980b9"))
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                }
            }
            val btnDel = ImageButton(this).apply {
                setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                background = null
                setOnClickListener {
                    linksList.removeAt(index)
                    refreshAttachmentViews()
                }
            }
            chip.addView(tv)
            chip.addView(btnDel)
            binding.attachmentContainer.addView(chip)
        }
    }
    // ====== عرض الصورة كاملة ======
    private fun showImageDialog(bitmap: Bitmap) {
        val iv = ImageView(this).apply {
            setImageBitmap(bitmap)
            adjustViewBounds = true
        }
        AlertDialog.Builder(this)
            .setView(iv)
            .setPositiveButton("Close", null)
            .show()
    }

    private val Int.dp get() = (this * resources.displayMetrics.density).toInt()

    // ====== Color Picker ======
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

    // ====== Buttons ======
    private fun setupButtons() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnSaveTop.setOnClickListener { saveNote() }
        binding.btnSave.setOnClickListener { saveNote() }

        // Attach Panel
        binding.btnAttach.setOnClickListener {
            binding.attachPanel.visibility =
                if (binding.attachPanel.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        // Camera
        binding.btnCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.CAMERA), 100
                )
            }
        }

        // Gallery
        binding.btnGallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            pickImage.launch(intent)
        }

        // Image from attach panel
        binding.btnAddImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            pickImage.launch(intent)
        }

        // File
        binding.btnAddFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "*/*" }
            pickFile.launch(intent)
        }

        // Link
        binding.btnAddLink.setOnClickListener {
            val input = EditText(this).apply { hint = "https://..." }
            AlertDialog.Builder(this)
                .setTitle("Add Link")
                .setView(input)
                .setPositiveButton("Add") { _, _ ->
                    val link = input.text.toString().trim()
                    if (link.isNotEmpty()) {
                        linksList.add(link)
                        refreshAttachmentViews()
                        Toast.makeText(this, "Link added", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
                .show()
        }

        // Share
        binding.btnShare.setOnClickListener {
            val shareText = buildString {
                append(binding.etTitle.text)
                if (binding.etSubtitle.text.isNotEmpty()) {
                    append("\n"); append(binding.etSubtitle.text)
                }
                if (binding.etBody.text.isNotEmpty()) {
                    append("\n\n"); append(binding.etBody.text)
                }
                if (linksList.isNotEmpty()) {
                    append("\n\nLinks:\n")
                    linksList.forEach { append("$it\n") }
                }
            }

            val uris = ArrayList<Uri>()
            try {
            val txtFile = File(cacheDir, "note_text.txt")
            txtFile.writeText(shareText)
            val txtUri = FileProvider.getUriForFile(
                this, "${packageName}.provider", txtFile
            )
            uris.add(txtUri)
        } catch (e: Exception) { }

            imagesList.forEachIndexed { i, base64 ->
                try {
                    val bytes = Base64.decode(base64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    val file = File(cacheDir, "share_image_$i.jpg")
                    val out = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    out.close()
                    val uri = FileProvider.getUriForFile(
                        this, "${packageName}.provider", file
                    )
                    uris.add(uri)
                } catch (e: Exception) { }
            }

            filesList.forEach { (name, base64) ->
                try {
                    val bytes = Base64.decode(base64, Base64.DEFAULT)
                    val file = File(cacheDir, name)
                    file.writeBytes(bytes)
                    val uri = FileProvider.getUriForFile(
                        this, "${packageName}.provider", file
                    )
                    uris.add(uri)
                } catch (e: Exception) { }
            }

            if (uris.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "*/*"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(intent, "Share Note"))
            } else {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                startActivity(Intent.createChooser(intent, "Share Note"))
            }
        }
    }

    // ====== Save ======
    private fun saveNote() {
        val title    = binding.etTitle.text.toString().trim()
        val subtitle = binding.etSubtitle.text.toString().trim()
        val body     = binding.etBody.text.toString().trim()

        if (title.isEmpty() && body.isEmpty()) {
            Toast.makeText(this, "Please write a title or note", Toast.LENGTH_SHORT).show()
            return
        }

        val date = SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.getDefault()).format(Date())
        val imagesStr = imagesList.joinToString("||")
        val filesStr  = filesList.joinToString("||") { "${it.first}::${it.second}" }
        val linksStr  = linksList.joinToString("||")

        if (existingNote == null) {
            NoteManager.add(Note(
                title = title, subtitle = subtitle, body = body,
                color = selectedColor, date = date,
                images = imagesStr, files = filesStr, links = linksStr,
                priority = selectedPriority, isPinned = isPinned
            ))
        } else {
            existingNote!!.apply {
                this.title = title
                this.subtitle = subtitle
                this.body = body
                this.color = selectedColor
                this.images = imagesStr
                this.files = filesStr
                this.links = linksStr
                this.priority = selectedPriority
                this.isPinned = isPinned
            }
            NoteManager.update(existingNote!!)
        }

        Toast.makeText(this, "Saved ✅", Toast.LENGTH_SHORT).show()
        finish()
    }
}