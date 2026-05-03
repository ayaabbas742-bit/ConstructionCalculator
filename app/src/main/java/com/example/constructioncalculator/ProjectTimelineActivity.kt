package com.example.constructioncalculator

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

// ===================== DATA MODELS =====================

data class Project(
    val id: Long = 0,
    val name: String,
    val startDate: String,
    val endDate: String,
    val status: String = "active",
    val progress: Int = 0
)

data class Phase(
    val id: Long = 0,
    val projectId: Long,
    val name: String,
    val startDate: String,
    val endDate: String,
    val status: String = "pending", // pending, active, done, late
    val notes: String = ""
)

// ===================== MAIN ACTIVITY =====================

class ProjectTimelineActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Weather Views
    private lateinit var tvCity: TextView
    private lateinit var tvTemp: TextView
    private lateinit var tvWeatherDesc: TextView
    private lateinit var tvHumidity: TextView
    private lateinit var tvWind: TextView
    private lateinit var tvWeatherIcon: TextView
    private lateinit var weatherCard: View

    // Timeline Views
    private lateinit var rvPhases: RecyclerView
    private lateinit var tvProjectName: TextView
    private lateinit var tvDaysLeft: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView
    private lateinit var btnAddPhase: View
    private lateinit var btnAddProject: View

    // FIX: تعريف المتغيرات المفقودة كـ properties
    private lateinit var layoutNoProject: LinearLayout
    private lateinit var layoutProjectInfo: LinearLayout

    private var currentProject: Project? = null
    private var projects = mutableListOf<Project>()
    private var phases = mutableListOf<Phase>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_timeline)

        db = DatabaseHelper(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        initViews()
        initListeners()

        rvPhases.layoutManager = LinearLayoutManager(this)

        ensureTablesExist()
        loadProjects()
        fetchWeather()
        animateEntrance()
    }

    private fun initViews() {
        tvCity = findViewById(R.id.tvCity)
        tvTemp = findViewById(R.id.tvTemperature)
        tvWeatherDesc = findViewById(R.id.tvWeatherDesc)
        tvHumidity = findViewById(R.id.tvHumidity)
        tvWind = findViewById(R.id.tvWind)
        tvWeatherIcon = findViewById(R.id.tvWeatherEmoji)
        weatherCard = findViewById(R.id.weatherCard)
        rvPhases = findViewById(R.id.rvPhases)
        tvProjectName = findViewById(R.id.tvProjectName)
        tvDaysLeft = findViewById(R.id.tvDaysLeft)
        progressBar = findViewById(R.id.progressBar)
        tvProgress = findViewById(R.id.tvProgressPercent)
        // FIX: إضافة تعريف المتغيرات المفقودة
        layoutNoProject = findViewById(R.id.layoutNoProject)
        layoutProjectInfo = findViewById(R.id.layoutProjectInfo)
        btnAddPhase = findViewById(R.id.btnAddPhase)
        btnAddProject = findViewById(R.id.btnNewProject)
    }
    private fun initListeners() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        btnAddProject.setOnClickListener { showAddProjectDialog() }
        btnAddPhase.setOnClickListener { showAddPhaseDialog() }
        tvProjectName.setOnClickListener { showProjectsListDialog() }
    }

    // ===================== DATABASE HELPERS =====================

    private fun ensureTablesExist() {
        val database = db.writableDatabase
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS projects (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                start_date TEXT,
                end_date TEXT,
                status TEXT DEFAULT 'active',
                progress INTEGER DEFAULT 0
            )
        """)
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS project_phases (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                project_id INTEGER,
                name TEXT,
                start_date TEXT,
                end_date TEXT,
                status TEXT DEFAULT 'pending',
                notes TEXT DEFAULT ''
            )
        """)
    }

    private fun loadProjects() {
        val database = db.readableDatabase
        val cursor = database.rawQuery("SELECT * FROM projects ORDER BY id DESC", null)

        projects.clear()
        while (cursor.moveToNext()) {
            projects.add(Project(
                id        = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                name      = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                startDate = cursor.getString(cursor.getColumnIndexOrThrow("start_date")),
                endDate   = cursor.getString(cursor.getColumnIndexOrThrow("end_date")),
                status    = cursor.getString(cursor.getColumnIndexOrThrow("status")),
                progress  = cursor.getInt(cursor.getColumnIndexOrThrow("progress"))
            ))
        }
        cursor.close()

        if (projects.isNotEmpty()) {
            if (currentProject == null) currentProject = projects.first()
            layoutNoProject.visibility   = View.GONE
            layoutProjectInfo.visibility = View.VISIBLE
            displayProject()
            loadPhases()
        } else {
            layoutNoProject.visibility   = View.VISIBLE
            layoutProjectInfo.visibility = View.GONE
            tvProjectName.text = "No Project Yet"
            tvDaysLeft.text    = "Add your first project"
        }
    }

    private fun loadPhases() {
        val proj = currentProject ?: return
        val database = db.readableDatabase
        val cursor = database.rawQuery(
            "SELECT * FROM project_phases WHERE project_id = ? ORDER BY start_date ASC",
            arrayOf(proj.id.toString())
        )
        phases.clear()
        while (cursor.moveToNext()) {
            phases.add(
                Phase(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    projectId = proj.id,
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    startDate = cursor.getString(cursor.getColumnIndexOrThrow("start_date")),
                    endDate = cursor.getString(cursor.getColumnIndexOrThrow("end_date")),
                    status = cursor.getString(cursor.getColumnIndexOrThrow("status")),
                    notes = cursor.getString(cursor.getColumnIndexOrThrow("notes"))
                )
            )
        }
        cursor.close()
        updatePhaseAdapter()
        recalculateProgress()
    }

    private fun recalculateProgress() {
        val total    = phases.size
        val done     = phases.count { it.status == "done" }
        val progress = if (total == 0) 0 else (done * 100) / total

        if (total > 0) {
            val sdf   = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = Date()

            // FIX: تحديث قائمة phases في الذاكرة أيضاً بعد تغيير الحالة إلى late
            val updatedPhases = phases.map { phase ->
                if (phase.status != "done") {
                val end = try { sdf.parse(phase.endDate) } catch (e: Exception) { null }
                if (end != null && end.before(today) && phase.status != "late") {
                    // FIX: استخدام ContentValues بدلاً من SQL مباشر
                    val cv = ContentValues().apply { put("status", "late") }
                    db.writableDatabase.update(
                        "project_phases", cv, "id=?", arrayOf(phase.id.toString())
                    )
                    phase.copy(status = "late")
                } else phase
            } else phase
            }
            phases.clear()
            phases.addAll(updatedPhases)

            // FIX: استخدام ContentValues بدلاً من SQL مباشر
            val cv = ContentValues().apply { put("progress", progress) }
            db.writableDatabase.update(
                "projects", cv, "id=?", arrayOf(currentProject?.id.toString())
            )
            currentProject = currentProject?.copy(progress = progress)
        }

        progressBar.progress = progress
        tvProgress.text      = "$progress%"
        findViewById<TextView>(R.id.tvTotalPhases).text = "$total"
        findViewById<TextView>(R.id.tvDonePhases).text  = "$done"

        if (progress == 100) {
            progressBar.progressDrawable =
                resources.getDrawable(R.drawable.progress_bar_style_green, null)
        }
    }

    // FIX: استخدام ContentValues لتجنب SQL Injection
    private fun savePhase(phase: Phase) {
        val cv = ContentValues().apply {
            put("project_id", phase.projectId)
            put("name",       phase.name)
            put("start_date", phase.startDate)
            put("end_date",   phase.endDate)
            put("status",     phase.status)
            put("notes",      phase.notes)
        }
        db.writableDatabase.insert("project_phases", null, cv)
        loadPhases()
    }

    // FIX: استخدام ContentValues لتجنب SQL Injection
    private fun updatePhaseStatus(phaseId: Long, newStatus: String) {
        val cv = ContentValues().apply { put("status", newStatus) }
        db.writableDatabase.update("project_phases", cv, "id=?", arrayOf(phaseId.toString()))
        loadPhases()
    }

    private fun deletePhase(phaseId: Long) {
        db.writableDatabase.delete("project_phases", "id=?", arrayOf(phaseId.toString()))
        loadPhases()
    }
    private fun deleteProject(projectId: Long) {
        db.writableDatabase.delete("projects", "id=?", arrayOf(projectId.toString()))
        db.writableDatabase.delete("project_phases", "project_id=?", arrayOf(projectId.toString()))
        if (currentProject?.id == projectId) currentProject = null
        loadProjects()
    }

    // FIX: استخدام ContentValues لتجنب SQL Injection
    private fun saveProject(project: Project) {
        val cv = ContentValues().apply {
            put("name",       project.name)
            put("start_date", project.startDate)
            put("end_date",   project.endDate)
            put("status",     "active")
            put("progress",   0)
        }
        db.writableDatabase.insert("projects", null, cv)
        loadProjects()
    }

    // ===================== DISPLAY =====================

    private fun displayProject() {
        val proj = currentProject ?: return

        // FIX: استخدام المتغيرات المعرّفة بشكل صحيح
        layoutNoProject.visibility   = View.GONE
        layoutProjectInfo.visibility = View.VISIBLE

        tvProjectName.text = "📁 ${proj.name}  ▾"
        findViewById<TextView>(R.id.tvProjectDates).text =
            "${proj.startDate}  →  ${proj.endDate}"

        findViewById<TextView>(R.id.tvStatusBadge).text = when (proj.status) {
            "done" -> "✅ Done"
            "late" -> "❌ Late"
            else   -> "🟢 Active"
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.isLenient = false
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
        }.time
        val endDate  = try { sdf.parse(proj.endDate) } catch (e: Exception) { null }
        val daysLeft = endDate?.let {
            ((it.time - today.time) / (1000 * 60 * 60 * 24)).toInt()
        } ?: 0
         tvDaysLeft.text = when {
            daysLeft > 0  -> "$daysLeft"
            daysLeft == 0 -> "0"
            else          -> "${-daysLeft}"
        }

        val tvDaysLabel = findViewById<TextView>(R.id.tvDaysLeftLabel)
        tvDaysLabel?.text = when {
            daysLeft > 0  -> "Days Left"
            daysLeft == 0 -> "Due Today!"
            else          -> "Days Late"
        }
        tvDaysLeft.setTextColor(Color.parseColor(
            if (daysLeft < 0) "#E74C3C" else "#E05C2A"
        ))

        progressBar.progress = proj.progress
        tvProgress.text      = "${proj.progress}%"
    }
    private fun showProjectsListDialog() {
        val db2 = db.readableDatabase
        val c = db2.rawQuery("SELECT * FROM projects ORDER BY id DESC", null)
        projects.clear()
        while (c.moveToNext()) {
            projects.add(Project(
                id        = c.getLong(c.getColumnIndexOrThrow("id")),
                name      = c.getString(c.getColumnIndexOrThrow("name")),
                startDate = c.getString(c.getColumnIndexOrThrow("start_date")),
                endDate   = c.getString(c.getColumnIndexOrThrow("end_date")),
                status    = c.getString(c.getColumnIndexOrThrow("status")),
                progress  = c.getInt(c.getColumnIndexOrThrow("progress"))
            ))
        }
        c.close()
        if (projects.isEmpty()) {
            Toast.makeText(this, "لا توجد مشاريع بعد", Toast.LENGTH_SHORT).show()
            return
        }
        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(android.R.color.white)
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                colors = intArrayOf(Color.parseColor("#1B2A4A"), Color.parseColor("#2E86AB"))
                gradientType = android.graphics.drawable.GradientDrawable.LINEAR_GRADIENT
                orientation = android.graphics.drawable.GradientDrawable.Orientation.TL_BR
            }
            setPadding(60, 40, 60, 40)
        }
        header.addView(TextView(this).apply {
            text = "📁 My Projects  (${projects.size})"
            textSize = 18f; setTextColor(Color.WHITE)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.CENTER
        })
        header.addView(TextView(this).apply {
            text = "Tap to switch  •  🗑 to delete"
            textSize = 11f; setTextColor(Color.parseColor("#C8E6FF"))
            gravity = android.view.Gravity.CENTER
        })
        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 20, 30, 20)
        }
        var openDialog: AlertDialog? = null
        projects.forEach { project ->
            val isSelected = project.id == currentProject?.id
            val item = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                    cornerRadius = 20f
                    setColor(if (isSelected) Color.parseColor("#E8F4FF") else Color.parseColor("#F8F9FA"))
                    setStroke(if (isSelected) 3 else 1,
                        if (isSelected) Color.parseColor("#2E86AB") else Color.parseColor("#E0E0E0"))
                }
                setPadding(30, 24, 30, 24)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 16 }
            }
            val info = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            info.addView(TextView(this).apply {
                text = (if (isSelected) "✅ " else "📁 ") + project.name
                textSize = 15f
                setTextColor(if (isSelected) Color.parseColor("#2E86AB") else Color.parseColor("#1B2A4A"))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            })
            info.addView(TextView(this).apply {
                text = "${project.startDate}  →  ${project.endDate}"
                textSize = 12f; setTextColor(Color.parseColor("#888888"))
            })
            info.addView(TextView(this).apply {
                text = "Progress: ${project.progress}%"
                textSize = 11f; setTextColor(Color.parseColor("#AAAAAA"))
            })
            val btnDelete = TextView(this).apply {
                text = "🗑"; textSize = 20f; setPadding(16, 0, 0, 0)
                setOnClickListener {
                    AlertDialog.Builder(this@ProjectTimelineActivity)
                        .setTitle("حذف المشروع؟")
                        .setMessage("سيتم حذف \"${project.name}\" وكل مراحله.")
                        .setPositiveButton("حذف") { _, _ ->
                            openDialog?.dismiss()
                            deleteProject(project.id)
                        }
                        .setNegativeButton("إلغاء", null)
                        .show()
                }
            }
            item.addView(info); item.addView(btnDelete)
            item.setOnClickListener {
                currentProject = project
                displayProject()
                loadPhases()
                openDialog?.dismiss()
            }
            list.addView(item)
        }
        val scroll = ScrollView(this).apply { addView(list) }
        dialogView.addView(header); dialogView.addView(scroll)
        openDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("إغلاق", null)
            .create()
        openDialog?.show()
    }

    // ===================== WEATHER =====================

    private fun fetchWeather() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            fetchWeatherByCity("Algiers")
            return
        }

        // أولاً جرب lastLocation
        fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
            if (loc != null) {
                fetchWeatherByCoords(loc.latitude, loc.longitude)
            } else {
                // إذا null اطلب موقع جديد
                val request = com.google.android.gms.location.LocationRequest.create().apply {
                    priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
                    numUpdates = 1
                    interval = 0
                }
                val callback = object : com.google.android.gms.location.LocationCallback() {
                    override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                        val newLoc = result.lastLocation
                        if (newLoc != null) {
                            fetchWeatherByCoords(newLoc.latitude, newLoc.longitude)
                        } else {
                            fetchWeatherByCity("Algiers")
                        }
                        fusedLocationClient.removeLocationUpdates(this)
                    }
                }
                fusedLocationClient.requestLocationUpdates(request, callback, mainLooper)
            }
        }.addOnFailureListener {
            fetchWeatherByCity("Algiers")
        }
    }

    private fun fetchWeatherByCoords(lat: Double, lon: Double) {
        thread {
            try {
                // ملاحظة: يُفضّل نقل الـ API key إلى local.properties
                val apiKey = "ff7779fd79f5b912121459ac72420d13"
                val url = "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=$apiKey&units=metric"
                val response = URL(url).readText()
                val json = JSONObject(response)
                runOnUiThread { updateWeatherUI(json) }
            } catch (e: Exception) {
                runOnUiThread { setDefaultWeather() }
            }
        }
    }

    private fun fetchWeatherByCity(city: String) {
        thread {
            try {
                // ملاحظة: يُفضّل نقل الـ API key إلى local.properties
                val apiKey = "ff7779fd79f5b912121459ac72420d13"
                val url = "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey&units=metric"
                val response = URL(url).readText()
                val json = JSONObject(response)
                runOnUiThread { updateWeatherUI(json) }
            } catch (e: Exception) {
                runOnUiThread { setDefaultWeather() }
            }
        }
    }

    private fun updateWeatherUI(json: JSONObject) {
        try {
            val city     = json.getString("name")
            val temp     = json.getJSONObject("main").getDouble("temp").toInt()
            val humidity = json.getJSONObject("main").getInt("humidity")
            val wind     = json.getJSONObject("wind").getDouble("speed").toInt()
            val desc     = json.getJSONArray("weather").getJSONObject(0).getString("description")
            val iconCode = json.getJSONArray("weather").getJSONObject(0).getString("icon")

            tvCity.text        = city
            tvTemp.text        = "$temp°"
            tvWeatherDesc.text = desc.replaceFirstChar { it.uppercase() }
            tvHumidity.text    = "💧 $humidity%"
            tvWind.text        = "🌬 $wind km/h"
            tvWeatherIcon.text = weatherEmoji(iconCode)
        } catch (e: Exception) {
            setDefaultWeather()
        }
    }

    private fun setDefaultWeather() {
        tvCity.text        = "Your City"
        tvTemp.text        = "--°"
        tvWeatherDesc.text = "Enable location"
        tvHumidity.text    = "💧 --%"
        tvWind.text        = "🌬 -- km/h"
        tvWeatherIcon.text = "🌤"
    }

    private fun weatherEmoji(icon: String) = when {
        icon.startsWith("01") -> "☀️"
        icon.startsWith("02") -> "⛅️"
        icon.startsWith("03") || icon.startsWith("04") -> "☁️"
        icon.startsWith("09") || icon.startsWith("10") -> "🌧"
        icon.startsWith("11") -> "⛈"
        icon.startsWith("13") -> "❄️"
        icon.startsWith("50") -> "🌫"
        else -> "🌤"
    }

    private fun weatherBg(icon: String): Int = when {
        icon.startsWith("01") -> Color.parseColor("#FF6B35")
        icon.startsWith("02") || icon.startsWith("03") -> Color.parseColor("#4A90D9")
        icon.startsWith("09") || icon.startsWith("10") -> Color.parseColor("#2C5F8A")
        icon.startsWith("11") -> Color.parseColor("#1A1A2E")
        else -> Color.parseColor("#2E86AB")
    }

    // ===================== DIALOGS =====================

    // FIX: الدالة كانت مبدّلة مع showAddPhaseDialog — تم تصحيح الأسماء والمحتوى
    private fun showAddProjectDialog() {
        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(android.R.color.white)
            setPadding(0, 0, 0, 0)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                colors = intArrayOf(
                    Color.parseColor("#1B2A4A"),
                    Color.parseColor("#2E86AB")
                )
                gradientType = android.graphics.drawable.GradientDrawable.LINEAR_GRADIENT
                orientation = android.graphics.drawable.GradientDrawable.Orientation.TL_BR
            }
            setPadding(60, 50, 60, 50)
        }
        header.addView(TextView(this).apply {
            text = "🏗"
            textSize = 40f
            gravity = android.view.Gravity.CENTER
        })
        header.addView(TextView(this).apply {
            text = "New Project"
            textSize = 20f
            setTextColor(Color.WHITE)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.CENTER
        })
        header.addView(TextView(this).apply {
            text = "Fill in the project details below"
            textSize = 13f
            setTextColor(Color.parseColor("#C8E6FF"))
            gravity = android.view.Gravity.CENTER
        })

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 40, 60, 20)
        }

        fun makeField(hint: String, icon: String): Pair<LinearLayout, EditText> {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                    setColor(Color.parseColor("#F8F9FA"))
                    cornerRadius = 16f
                    setStroke(2, Color.parseColor("#E0E0E0"))
                }
                setPadding(20, 4, 20, 4)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 20 }
            }
            val tvIcon = TextView(this).apply {
                text = icon
                textSize = 20f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = 16 }
                 }
            val et = EditText(this).apply {
                this.hint = hint
                setHintTextColor(Color.parseColor("#AAAAAA"))
                setTextColor(Color.parseColor("#1B2A4A"))
                background = null
                textSize = 15f
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                )
            }
            row.addView(tvIcon)
            row.addView(et)
            return Pair(row, et)
        }

        val (rowName, etName)   = makeField("Project Name", "📋")
        val (rowStart, etStart) = makeField("Start Date (yyyy-MM-dd)", "📅")
        val (rowEnd, etEnd)     = makeField("End Date (yyyy-MM-dd)", "🏁")

        etStart.setText(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        )

        form.addView(rowName)
        form.addView(rowStart)
        form.addView(rowEnd)

        val tvTip = TextView(this).apply {
            text = "💡 Long press on project name to delete it"
            textSize = 11f
            setTextColor(Color.parseColor("#AAAAAA"))
            gravity = android.view.Gravity.CENTER
            setPadding(40, 0, 40, 20)
        }

        dialogView.addView(header)
        dialogView.addView(form)
        dialogView.addView(tvTip)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("🚀 Create Project") { _, _ ->
                val name  = etName.text.toString().trim()
                val start = etStart.text.toString().trim()
                val end   = etEnd.text.toString().trim()

                // FIX: إضافة || المفقود
                if (name.isEmpty() || start.isEmpty()||  end.isEmpty()) {
                    Toast.makeText(this, "⚠️ Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                sdf.isLenient = false
                try { sdf.parse(start); sdf.parse(end) } catch (e: Exception) {
                    Toast.makeText(this, "❌ Wrong date format! Use: yyyy-MM-dd", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }
                saveProject(Project(name = name, startDate = start, endDate = end))
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.parseColor("#1B2A4A"))
    }

    // FIX: الدالة كانت مبدّلة — تم تصحيح المحتوى الكامل لإضافة Phase
    private fun showAddPhaseDialog() {
        val proj = currentProject ?: run {
            Toast.makeText(this, "Create a project first!", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(android.R.color.white)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                colors = intArrayOf(
                    Color.parseColor("#E05C2A"),
                    Color.parseColor("#FF8C42")
                )
                gradientType = android.graphics.drawable.GradientDrawable.LINEAR_GRADIENT
                orientation = android.graphics.drawable.GradientDrawable.Orientation.TL_BR
            }
            setPadding(60, 40, 60, 40)
        }
        header.addView(TextView(this).apply {
            text = "📋"
            textSize = 36f
            gravity = android.view.Gravity.CENTER
        })
        header.addView(TextView(this).apply {
            text = "Add Phase"
            textSize = 18f
            setTextColor(Color.WHITE)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.CENTER
        })
        header.addView(TextView(this).apply {
            text = "📁 ${proj.name}"
            textSize = 12f
            setTextColor(Color.parseColor("#FFE0CC"))
            gravity = android.view.Gravity.CENTER
        })

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 30, 50, 10)
        }

        fun makeDateField(hint: String, icon: String): Pair<LinearLayout, EditText> {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                    setColor(Color.parseColor("#F8F9FA"))
                    cornerRadius = 16f
                    setStroke(2, Color.parseColor("#E0E0E0"))
                }
                setPadding(20, 4, 20, 4)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 16 }
            }
            val tvIcon = TextView(this).apply {
                text = icon
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = 12 }
            }
            val et = EditText(this).apply {
                this.hint = hint
                setHintTextColor(Color.parseColor("#AAAAAA"))
                setTextColor(Color.parseColor("#1B2A4A"))
                background = null
                textSize = 14f
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                )
            }
            row.addView(tvIcon)
            row.addView(et)
            return Pair(row, et)
        }

        // Quick Select chips
        val tvQuickLabel = TextView(this).apply {
            text = "⚡ Quick Select"
            textSize = 13f
            setTextColor(Color.parseColor("#1B2A4A"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        val phaseOptions = listOf(
            "🏗" to "Excavation",
            "🧱" to "Foundation",
            "🏠" to "Structure",
            "🪟" to "Windows",
            "⚡" to "Electrical",
            "🚰" to "Plumbing",
            "🎨" to "Finishing"
        )

        val etName = EditText(this).apply {
            hint = "Phase Name"
            setHintTextColor(Color.parseColor("#AAAAAA"))
            setTextColor(Color.parseColor("#1B2A4A"))
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                setColor(Color.parseColor("#F8F9FA"))
                cornerRadius = 16f
                setStroke(2, Color.parseColor("#E0E0E0"))
            }
            setPadding(30, 20, 30, 20)
            textSize = 15f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 16; bottomMargin = 16 }
        }
        val chipsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        val chipsScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
            addView(chipsRow)
        }

        phaseOptions.forEach { (emoji, phaseName) ->
            val chip = TextView(this).apply {
                text = "$emoji $phaseName"
                textSize = 12f
                setTextColor(Color.parseColor("#1B2A4A"))
                background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                    cornerRadius = 30f
                    setColor(Color.parseColor("#F0F4FF"))
                    setStroke(1, Color.parseColor("#C0C8E0"))
                }
                setPadding(24, 12, 24, 12)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = 12 }
                setOnClickListener { etName.setText(phaseName) }
            }
            chipsRow.addView(chip)
        }

        val (rowStart, etStart) = makeDateField("Start Date (yyyy-MM-dd)", "📅")
        val (rowEnd, etEnd)     = makeDateField("End Date (yyyy-MM-dd)", "🏁")
        val (rowNotes, etNotes) = makeDateField("Notes (optional)", "📝")

        etStart.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))

        form.addView(tvQuickLabel)
        form.addView(chipsScroll)
        form.addView(etName)
        form.addView(rowStart)
        form.addView(rowEnd)
        form.addView(rowNotes)

        dialogView.addView(header)
        dialogView.addView(form)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("➕ Add Phase") { _, _ ->
                val name  = etName.text.toString().trim()
                val start = etStart.text.toString().trim()
                val end   = etEnd.text.toString().trim()
                val notes = etNotes.text.toString().trim()

                // FIX: إضافة || المفقود
                if (name.isEmpty()  ||start.isEmpty() || end.isEmpty()) {
                    Toast.makeText(this, "⚠️ Fill name and dates", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                sdf.isLenient = false
                try { sdf.parse(start); sdf.parse(end) } catch (e: Exception) {
                    Toast.makeText(this, "❌ Wrong date format! Use: yyyy-MM-dd", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }
                savePhase(Phase(
                    projectId = proj.id,
                    name      = name,
                    startDate = start,
                    endDate   = end,
                    notes     = notes
                ))
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.parseColor("#E05C2A"))
    }

    private fun showPhaseOptionsDialog(phase: Phase) {
        val options = arrayOf("✅ Mark Done", "🔄 Mark Active", "⏳ Mark Pending", "🗑 Delete")
        AlertDialog.Builder(this)
            .setTitle(phase.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> updatePhaseStatus(phase.id, "done")
                     1 -> updatePhaseStatus(phase.id, "active")
                    2 -> updatePhaseStatus(phase.id, "pending")
                    3 -> {
                        AlertDialog.Builder(this)
                            .setTitle("Delete Phase?")
                            .setMessage("Are you sure?")
                            .setPositiveButton("Delete") { _, _ -> deletePhase(phase.id) }
                            .setNegativeButton("Cancel", null)
                            .show()
                    }
                }
            }
            .show()
    }

    // ===================== ANIMATIONS =====================

    private fun animateEntrance() {
        weatherCard.alpha = 0f
        weatherCard.translationY = -60f
        val anim1 = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(weatherCard, "alpha", 0f, 1f).setDuration(600),
                ObjectAnimator.ofFloat(weatherCard, "translationY", -60f, 0f).setDuration(600)
            )
            interpolator = DecelerateInterpolator()
        }
        anim1.start()
    }

    // ===================== RECYCLER ADAPTER =====================

    private fun updatePhaseAdapter() {
        // FIX: تغيير PhaseAdapterXML إلى PhaseAdapter (الاسم الصحيح للكلاس)
        rvPhases.adapter = PhaseAdapter(phases) { phase -> showPhaseOptionsDialog(phase) }
    }

    // ===================== PERMISSIONS =====================

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            fetchWeather()
        }
    }
}

// ===================== PHASE ADAPTER =====================

class PhaseAdapter(
    private val phases: List<Phase>,
    private val onLongClick: (Phase) -> Unit
) : RecyclerView.Adapter<PhaseAdapter.PhaseVH>() {

    inner class PhaseVH(val card: LinearLayout) : RecyclerView.ViewHolder(card)

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): PhaseVH {
        val ctx = parent.context
        val card = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 30f
                setColor(Color.WHITE)
            }
            setPadding(40, 30, 40, 30)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 20 }
            elevation = 4f
        }
        return PhaseVH(card)
    }

    override fun onBindViewHolder(holder: PhaseVH, position: Int) {
        val phase = phases[position]
        val ctx = holder.card.context
        holder.card.removeAllViews()

        val dot = View(ctx).apply {
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(statusColor(phase.status))
            }
            layoutParams = LinearLayout.LayoutParams(24, 24).apply { marginEnd = 30 }
        }

        val info = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        info.addView(TextView(ctx).apply {
            text = phase.name
            textSize = 15f
            setTextColor(Color.parseColor("#1B2A4A"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        })
        info.addView(TextView(ctx).apply {
            text = "${phase.startDate}  →  ${phase.endDate}"
            textSize = 12f
            setTextColor(Color.parseColor("#888888"))
        })
        if (phase.notes.isNotEmpty()) {
            info.addView(TextView(ctx).apply {
                text = phase.notes
                textSize = 11f
                setTextColor(Color.parseColor("#AAAAAA"))
            })
        }

        val badge = TextView(ctx).apply {
            text = statusLabel(phase.status)
            textSize = 11f
            setTextColor(Color.WHITE)
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 20f
                setColor(statusColor(phase.status))
            }
            setPadding(20, 8, 20, 8)
        }

        holder.card.addView(dot)
        holder.card.addView(info)
        holder.card.addView(badge)

        holder.card.setOnLongClickListener { onLongClick(phase); true }
        holder.card.setOnClickListener { onLongClick(phase) }
    }

    override fun getItemCount() = phases.size

    private fun statusColor(status: String) = when (status) {
        "done"   -> Color.parseColor("#27AE60")
        "active" -> Color.parseColor("#E05C2A")
        "late"   -> Color.parseColor("#E74C3C")
        else     -> Color.parseColor("#95A5A6")
    }

    private fun statusLabel(status: String) = when (status) {
        "done"   -> "✅ Done"
        "active" -> "🔄 Active"
        "late"   -> "❌ Late"
        else     -> "⏳ Pending"
    }
}