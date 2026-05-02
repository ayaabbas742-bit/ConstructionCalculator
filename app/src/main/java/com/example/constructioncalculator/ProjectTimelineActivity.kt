package com.example.constructioncalculator

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
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
    private lateinit var tvNoProject: TextView

    private var currentProject: Project? = null
    private var phases = mutableListOf<Phase>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = DatabaseHelper(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        buildUI()
        ensureTablesExist()
        loadProject()
        fetchWeather()
        animateEntrance()
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

    private fun loadProject() {
        val database = db.readableDatabase
        val cursor = database.rawQuery("SELECT * FROM projects ORDER BY id DESC LIMIT 1", null)
        if (cursor.moveToFirst()) {
            currentProject = Project(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                startDate = cursor.getString(cursor.getColumnIndexOrThrow("start_date")),
                endDate = cursor.getString(cursor.getColumnIndexOrThrow("end_date")),
                status = cursor.getString(cursor.getColumnIndexOrThrow("status")),
                progress = cursor.getInt(cursor.getColumnIndexOrThrow("progress"))
            )
            cursor.close()
            displayProject()
            loadPhases()
        } else {
            cursor.close()
            tvNoProject.visibility = View.VISIBLE
            tvProjectName.text = "No Project Yet"
            tvDaysLeft.text = "Add your first project"
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
        if (phases.isEmpty()) return
        val done = phases.count { it.status == "done" }
        val progress = (done * 100) / phases.size
        val database = db.writableDatabase
        database.execSQL("UPDATE projects SET progress = $progress WHERE id = ${currentProject?.id}")
        currentProject = currentProject?.copy(progress = progress)
        progressBar.progress = progress
        tvProgress.text = "$progress%"
    }

    private fun savePhase(phase: Phase) {
        val database = db.writableDatabase
        database.execSQL("""
            INSERT INTO project_phases (project_id, name, start_date, end_date, status, notes)
            VALUES (${phase.projectId}, '${phase.name}', '${phase.startDate}', '${phase.endDate}', '${phase.status}', '${phase.notes}')
        """)
        loadPhases()
    }

    private fun updatePhaseStatus(phaseId: Long, newStatus: String) {
        val database = db.writableDatabase
        database.execSQL("UPDATE project_phases SET status = '$newStatus' WHERE id = $phaseId")
        loadPhases()
    }

    private fun deletePhase(phaseId: Long) {
        val database = db.writableDatabase
        database.execSQL("DELETE FROM project_phases WHERE id = $phaseId")
        loadPhases()
    }

    private fun saveProject(project: Project) {
        val database = db.writableDatabase
        database.execSQL("""
            INSERT INTO projects (name, start_date, end_date, status, progress)
            VALUES ('${project.name}', '${project.startDate}', '${project.endDate}', 'active', 0)
        """)
        loadProject()
    }

    // ===================== DISPLAY =====================

    private fun displayProject() {
        val proj = currentProject ?: return
        tvNoProject.visibility = View.GONE
        tvProjectName.text = proj.name
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Date()
        val endDate = try { sdf.parse(proj.endDate) } catch (e: Exception) { null }
        val daysLeft = endDate?.let { ((it.time - today.time) / (1000 * 60 * 60 * 24)).toInt() } ?: 0

        tvDaysLeft.text = when {
            daysLeft > 0 -> "$daysLeft days remaining"
            daysLeft == 0 -> "Due today!"
            else -> "${-daysLeft} days overdue"
        }

        progressBar.progress = proj.progress
        tvProgress.text = "${proj.progress}%"
    }

    // ===================== WEATHER =====================

    private fun fetchWeather() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            // Use default city if no permission
            fetchWeatherByCity("Baghdad")
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
            if (loc != null) {
                fetchWeatherByCoords(loc.latitude, loc.longitude)
            } else {
                fetchWeatherByCity("Baghdad")
            }
        }
    }

    private fun fetchWeatherByCoords(lat: Double, lon: Double) {
        thread {
            try {
                val apiKey = "bd5e378503941ddeb2130bab8ce1c805" // free key for demo
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
                val apiKey = "bd5e378503941ddeb2130bab8ce1c805"
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
            val city = json.getString("name")
            val temp = json.getJSONObject("main").getDouble("temp").toInt()
            val humidity = json.getJSONObject("main").getInt("humidity")
            val wind = json.getJSONObject("wind").getDouble("speed").toInt()
            val desc = json.getJSONArray("weather").getJSONObject(0).getString("description")
            val iconCode = json.getJSONArray("weather").getJSONObject(0).getString("icon")

            tvCity.text = city
            tvTemp.text = "$temp°"
            tvWeatherDesc.text = desc.replaceFirstChar { it.uppercase() }
            tvHumidity.text = "💧 $humidity%"
            tvWind.text = "🌬️ $wind km/h"
            tvWeatherIcon.text = weatherEmoji(iconCode)

            weatherCard.setBackgroundColor(weatherBg(iconCode))
        } catch (e: Exception) {
            setDefaultWeather()
        }
    }

    private fun setDefaultWeather() {
        tvCity.text = "Your City"
        tvTemp.text = "--°"
        tvWeatherDesc.text = "Enable location"
        tvHumidity.text = "💧 --%"
        tvWind.text = "🌬️ -- km/h"
        tvWeatherIcon.text = "🌤️"
    }

    private fun weatherEmoji(icon: String) = when {
        icon.startsWith("01") -> "☀️"
        icon.startsWith("02") -> "⛅"
        icon.startsWith("03") || icon.startsWith("04") -> "☁️"
        icon.startsWith("09") || icon.startsWith("10") -> "🌧️"
        icon.startsWith("11") -> "⛈️"
        icon.startsWith("13") -> "❄️"
        icon.startsWith("50") -> "🌫️"
        else -> "🌤️"
    }
    private fun weatherBg(icon: String): Int = when {
        icon.startsWith("01") -> Color.parseColor("#FF6B35")
        icon.startsWith("02") || icon.startsWith("03") -> Color.parseColor("#4A90D9")
        icon.startsWith("09") || icon.startsWith("10") -> Color.parseColor("#2C5F8A")
        icon.startsWith("11") -> Color.parseColor("#1A1A2E")
        else -> Color.parseColor("#2E86AB")
    }

    // ===================== DIALOGS =====================

    private fun showAddProjectDialog() {
        val dialog = AlertDialog.Builder(this, R.style.AlertDialogTheme)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 40, 60, 20)
        }

        val etName = EditText(this).apply {
            hint = "Project Name"
            setHintTextColor(Color.parseColor("#999999"))
        }
        val etStart = EditText(this).apply {
            hint = "Start Date (yyyy-MM-dd)"
            setHintTextColor(Color.parseColor("#999999"))
            setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
        }
        val etEnd = EditText(this).apply {
            hint = "End Date (yyyy-MM-dd)"
            setHintTextColor(Color.parseColor("#999999"))
        }

        layout.addView(TextView(this).apply { text = "🏗️ New Project"; textSize = 18f; setTextColor(Color.BLACK) })
        layout.addView(etName)
        layout.addView(etStart)
        layout.addView(etEnd)

        dialog.setView(layout)
        dialog.setPositiveButton("Create") { _, _ ->
            val name = etName.text.toString().trim()
            val start = etStart.text.toString().trim()
            val end = etEnd.text.toString().trim()
            if (name.isNotEmpty() && start.isNotEmpty() && end.isNotEmpty()) {
                saveProject(Project(name = name, startDate = start, endDate = end))
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.setNegativeButton("Cancel", null)
        dialog.show()
    }

    private fun showAddPhaseDialog() {
        val proj = currentProject ?: run {
            Toast.makeText(this, "Create a project first!", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = AlertDialog.Builder(this, R.style.AlertDialogTheme)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 40, 60, 20)
        }

        val phaseNames = arrayOf(
            "🏗️ Excavation", "🧱 Foundation", "🏠 Structure",
            "🪟 Windows & Doors", "⚡ Electrical", "🚰 Plumbing",
            "🎨 Finishing", "Custom..."
        )

        val etName = EditText(this).apply { hint = "Phase Name" }
        val etStart = EditText(this).apply {
            hint = "Start Date (yyyy-MM-dd)"
            setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
        }
        val etEnd = EditText(this).apply { hint = "End Date (yyyy-MM-dd)" }
        val etNotes = EditText(this).apply { hint = "Notes (optional)" }

        layout.addView(TextView(this).apply { text = "📋 Quick Select"; textSize = 14f; setTextColor(Color.GRAY) })

        val spinner = Spinner(this)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, phaseNames)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                if (pos < phaseNames.size - 1) etName.setText(phaseNames[pos])
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        layout.addView(spinner)
        layout.addView(TextView(this).apply { text = "Or enter custom:"; textSize = 12f; setTextColor(Color.GRAY) })
        layout.addView(etName)
        layout.addView(etStart)
        layout.addView(etEnd)
        layout.addView(etNotes)
        dialog.setView(layout)
        dialog.setPositiveButton("Add Phase") { _, _ ->
            val name = etName.text.toString().trim()
            val start = etStart.text.toString().trim()
            val end = etEnd.text.toString().trim()
            val notes = etNotes.text.toString().trim()
            if (name.isNotEmpty() && start.isNotEmpty() && end.isNotEmpty()) {
                savePhase(Phase(projectId = proj.id, name = name, startDate = start, endDate = end, notes = notes))
            } else {
                Toast.makeText(this, "Fill name and dates", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.setNegativeButton("Cancel", null)
        dialog.show()
    }

    private fun showPhaseOptionsDialog(phase: Phase) {
        val options = arrayOf("✅ Mark Done", "🔄 Mark Active", "⏳ Mark Pending", "🗑️ Delete")
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
        rvPhases.adapter = PhaseAdapter(phases) { phase -> showPhaseOptionsDialog(phase) }
    }

    // ===================== BUILD UI PROGRAMMATICALLY =====================

    private fun buildUI() {
        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#F0F4F8"))
        }

        // ── TOP BAR ──
        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#1B2A4A"))
            setPadding(40, 50, 40, 30)
            gravity = android.view.Gravity.CENTER_VERTICAL
        }
        val btnBack = TextView(this).apply {
            text = "←"
            textSize = 24f
            setTextColor(Color.WHITE)
            setOnClickListener { finish() }
        }
        val tvTitle = TextView(this).apply {
            text = "  Project Timeline"
            textSize = 20f
            setTextColor(Color.WHITE)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        btnAddProject = TextView(this).apply {
            text = "+ Project"
            textSize = 13f
            setTextColor(Color.parseColor("#FFD700"))
            setOnClickListener { showAddProjectDialog() }
        }
        topBar.addView(btnBack)
        topBar.addView(tvTitle)
        topBar.addView(btnAddProject)
        // ── WEATHER CARD ──
        weatherCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#2E86AB"))
            setPadding(50, 50, 50, 50)
            val m = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            m.setMargins(40, 40, 40, 20)
            layoutParams = m
            // Rounded corners via background
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 40f
                setColor(Color.parseColor("#2E86AB"))
            }
        }

        val weatherRow1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }
        tvWeatherIcon = TextView(this).apply {
            text = "🌤️"
            textSize = 48f
        }
        val weatherInfoCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            lp.marginStart = 30
            layoutParams = lp
        }
        tvCity = TextView(this).apply {
            text = "Loading..."
            textSize = 16f
            setTextColor(Color.parseColor("#D0E8FF"))
        }
        tvTemp = TextView(this).apply {
            text = "--°"
            textSize = 42f
            setTextColor(Color.WHITE)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        tvWeatherDesc = TextView(this).apply {
            text = "..."
            textSize = 14f
            setTextColor(Color.parseColor("#D0E8FF"))
        }
        weatherInfoCol.addView(tvCity)
        weatherInfoCol.addView(tvTemp)
        weatherInfoCol.addView(tvWeatherDesc)
        weatherRow1.addView(tvWeatherIcon)
        weatherRow1.addView(weatherInfoCol)

        val weatherRow2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            lp.topMargin = 20
            layoutParams = lp
        }
        tvHumidity = TextView(this).apply {
            text = "💧 --%"
            textSize = 13f
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        tvWind = TextView(this).apply {
            text = "🌬️ -- km/h"
            textSize = 13f
            setTextColor(Color.WHITE)
        }
        val tvWeatherLabel = TextView(this).apply {
            text = "🏗️ Site Weather"
            textSize = 11f
            setTextColor(Color.parseColor("#A0C8E8"))
        }
        weatherRow2.addView(tvHumidity)
        weatherRow2.addView(tvWind)

        (weatherCard as LinearLayout).addView(tvWeatherLabel)
        (weatherCard as LinearLayout).addView(weatherRow1)
        (weatherCard as LinearLayout).addView(weatherRow2)

        // ── PROJECT CARD ──
        val projectCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 40f
                setColor(Color.WHITE)
            }
            setPadding(50, 40, 50, 40)
            val m = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            m.setMargins(40, 0, 40, 20)
            layoutParams = m
            elevation = 8f
        }
        tvNoProject = TextView(this).apply {
            text = "No project yet. Tap '+ Project' to start!"
            textSize = 14f
            setTextColor(Color.parseColor("#888888"))
            gravity = android.view.Gravity.CENTER
            visibility = View.GONE
        }
        tvProjectName = TextView(this).apply {
            text = "Loading..."
            textSize = 20f
            setTextColor(Color.parseColor("#1B2A4A"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        tvDaysLeft = TextView(this).apply {
            text = ""
            textSize = 13f
            setTextColor(Color.parseColor("#E05C2A"))
        }

        // Progress
        val progressRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            lp.topMargin = 20
            layoutParams = lp
        }
        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progress = 0
            progressDrawable = resources.getDrawable(android.R.drawable.progress_horizontal, null)
            layoutParams = LinearLayout.LayoutParams(0, 30, 1f)
        }
        tvProgress = TextView(this).apply {
            text = "0%"
            textSize = 14f
            setTextColor(Color.parseColor("#1B2A4A"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            lp.marginStart = 20
            layoutParams = lp
        }
        progressRow.addView(progressBar)
        progressRow.addView(tvProgress)

        projectCard.addView(tvNoProject)
        projectCard.addView(tvProjectName)
        projectCard.addView(tvDaysLeft)
        projectCard.addView(progressRow)

        // ── PHASES HEADER ──
        val phasesHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            val m = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            m.setMargins(40, 10, 40, 10)
            layoutParams = m
        }
        val tvPhasesTitle = TextView(this).apply {
            text = "📋 Project Phases"
            textSize = 17f
            setTextColor(Color.parseColor("#1B2A4A"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        btnAddPhase = TextView(this).apply {
            text = "+ Add Phase"
            textSize = 13f
            setTextColor(Color.WHITE)
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 30f
                setColor(Color.parseColor("#E05C2A"))
            }
            setPadding(30, 15, 30, 15)
            setOnClickListener { showAddPhaseDialog() }
        }
        phasesHeader.addView(tvPhasesTitle)
        phasesHeader.addView(btnAddPhase)

        // ── RECYCLER VIEW ──
        rvPhases = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@ProjectTimelineActivity)
            val m = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            m.setMargins(40, 0, 40, 40)
            layoutParams = m
            isNestedScrollingEnabled = false
        }

        // ── ASSEMBLE ──
        root.addView(topBar)
        root.addView(weatherCard)
        root.addView(projectCard)
        root.addView(phasesHeader)
        root.addView(rvPhases)

        scroll.addView(root)
        setContentView(scroll)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
            val m = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            m.bottomMargin = 20
            layoutParams = m
            elevation = 4f
        }
        return PhaseVH(card)
    }

    override fun onBindViewHolder(holder: PhaseVH, position: Int) {
        val phase = phases[position]
        val ctx = holder.card.context
        holder.card.removeAllViews()

        // Status dot
        val dot = View(ctx).apply {
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(statusColor(phase.status))
            }
            layoutParams = LinearLayout.LayoutParams(24, 24).apply { marginEnd = 30 }
        }

        // Info column
        val info = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val tvName = TextView(ctx).apply {
            text = phase.name
            textSize = 15f
            setTextColor(Color.parseColor("#1B2A4A"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val tvDates = TextView(ctx).apply {
            text = "${phase.startDate}  →  ${phase.endDate}"
            textSize = 12f
            setTextColor(Color.parseColor("#888888"))
        }
        if (phase.notes.isNotEmpty()) {
            info.addView(tvName)
            info.addView(tvDates)
            info.addView(TextView(ctx).apply {
                text = phase.notes
                textSize = 11f
                setTextColor(Color.parseColor("#AAAAAA"))
            })
        } else {
            info.addView(tvName)
            info.addView(tvDates)
        }

        // Status badge
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
        "done" -> Color.parseColor("#27AE60")
        "active" -> Color.parseColor("#E05C2A")
        "late" -> Color.parseColor("#E74C3C")
        else -> Color.parseColor("#95A5A6")
    }

    private fun statusLabel(status: String) = when (status) {
        "done" -> "✅ Done"
        "active" -> "🔄 Active"
        "late" -> "❌ Late"
        else -> "⏳ Pending"
    }
}