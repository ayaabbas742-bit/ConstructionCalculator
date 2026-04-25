package com.example.constructioncalculator

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "ConstructionDB", null, 6) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                firstName TEXT,
                lastName TEXT,
                email TEXT UNIQUE,
                password TEXT,
                otp TEXT,
                profile_image TEXT
            )
        """)
        db.execSQL("""
            CREATE TABLE feedback (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT,
                rating REAL,
                note TEXT
            )
        """)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS otp_table (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT,
                otp TEXT,
                created_at INTEGER
            )
        """)
        db.execSQL("""
            CREATE TABLE results (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                bricks INTEGER,
                cement REAL,
                sand REAL,
                total REAL
            )
        """)
        db.execSQL("""
            CREATE TABLE plaster (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                area REAL,
                thickness REAL,
                ratio TEXT,
                cement REAL,
                sand REAL,
                water REAL,
                volume REAL
            )
        """)
        db.execSQL("""
            CREATE TABLE paint_results (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                type TEXT,
                area REAL,
                coats INTEGER,
                paint REAL,
                date TEXT
            )
        """)
        db.execSQL("""
            CREATE TABLE tiles (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                type TEXT,
                area REAL,
                count REAL,
                date TEXT
            )
        """)
        db.execSQL("""
            CREATE TABLE draw_plans (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                data TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
        """)
        db.execSQL("""
    CREATE TABLE IF NOT EXISTS construction_notes (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        title TEXT,
        subtitle TEXT,
        body TEXT,
        color INTEGER,
        date TEXT
    )
""")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 4) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS draw_plans (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    data TEXT NOT NULL,
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL
                )
            """)
        }
        if (oldVersion < 5) {
            db.execSQL("""
        CREATE TABLE IF NOT EXISTS construction_notes (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            title TEXT,
            subtitle TEXT,
            body TEXT,
            color INTEGER,
            date TEXT
        )
    """)
        }
        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE users ADD COLUMN profile_image TEXT")
        }            // نهاية onUpgrade
    }

    // ================= USERS =================
    fun insertUser(first: String, last: String, email: String, password: String): Boolean {
        val values = ContentValues().apply {
            put("firstName", first)
            put("lastName", last)
            put("email", email)
            put("password", password)
            put("otp", (1000..9999).random().toString())
        }
        return writableDatabase.insert("users", null, values) != -1L
    }

    fun checkUser(email: String, password: String): Boolean {
        val cursor = readableDatabase.rawQuery(
            "SELECT id FROM users WHERE email=? AND password=?",
            arrayOf(email, password)
        )
        val ok = cursor.moveToFirst()
        cursor.close()
        return ok
    }
    fun getUserName(email: String): String {
        val cursor = readableDatabase.rawQuery(
            "SELECT firstName FROM users WHERE email=?",
            arrayOf(email)
        )
        var name = ""
        if (cursor.moveToFirst()) name = cursor.getString(0)
        cursor.close()
        return name
    }

    fun updatePassword(email: String, newPassword: String): Boolean {
        val values = ContentValues()
        values.put("password", newPassword)
        return writableDatabase.update("users", values, "email=?", arrayOf(email)) > 0
    }

    fun getUser(email: String): User? {
        val cursor = readableDatabase.rawQuery(
            "SELECT firstName, lastName, email, profile_image FROM users WHERE email=?",
            arrayOf(email)
        )
        var user: User? = null
        if (cursor.moveToFirst()) {
            user = User(
                firstName    = cursor.getString(0),
                lastName     = cursor.getString(1),
                email        = cursor.getString(2),
                profileImage = cursor.getString(3)
            )
        }
        cursor.close()
        return user
    }

    fun updateProfileImage(email: String, imageUri: String): Int {
        val cv = ContentValues()
        cv.put("profile_image", imageUri)
        return writableDatabase.update("users", cv, "email=?", arrayOf(email))
    }

    // ================= FEEDBACK =================
    fun insertFeedback(email: String, rating: Float, note: String) {
        val values = ContentValues().apply {
            put("email", email)
            put("rating", rating)
            put("note", note)
        }
        writableDatabase.insert("feedback", null, values)
    }

    // ================= RESULTS =================
    fun insertResult(bricks: Int, cement: Double, sand: Double, total: Double): Boolean {
        val values = ContentValues().apply {
            put("bricks", bricks)
            put("cement", cement)
            put("sand", sand)
            put("total", total)
        }
        return writableDatabase.insert("results", null, values) != -1L
    }

    // ================= PLASTER =================
    fun insertPlaster(
        area: Double, thickness: Double, ratio: String,
        cement: Double, sand: Double, water: Double, volume: Double
    ) {
        val values = ContentValues().apply {
            put("area", area)
            put("thickness", thickness)
            put("ratio", ratio)
            put("cement", cement)
            put("sand", sand)
            put("water", water)
            put("volume", volume)
        }
        writableDatabase.insert("plaster", null, values)
    }

    // ================= PAINT =================
    fun insertPaint(type: String, area: Double, coats: Int, paint: Double, date: String) {
        val values = ContentValues().apply {
            put("type", type)
            put("area", area)
            put("coats", coats)
            put("paint", paint)
            put("date", date)
        }
        writableDatabase.insert("paint_results", null, values)
    }

    // ================= TILES =================
    fun insertTile(type: String, area: Double, count: Double, date: String) {
        val values = ContentValues().apply {
            put("type", type)
            put("area", area)
            put("count", count)
            put("date", date)
        }
        writableDatabase.insert("tiles", null, values)
    }

    // ================= OTP =================
    fun generateOtp(email: String): String {
        val otp = (100000..999999).random().toString()
        val cv = ContentValues().apply {
            put("email", email)
            put("otp", otp)
            put("created_at", System.currentTimeMillis())
        }
        writableDatabase.insert("otp_table", null, cv)
        return otp
    }
    fun checkOtp(email: String, otp: String): Boolean {
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM otp_table WHERE email=? AND otp=?",
            arrayOf(email, otp)
        )
        val isValid = cursor.count > 0
        cursor.close()
        return isValid
    }

    // ================= FLOOR PLANS =================
    fun insertPlan(name: String, data: String): Long {
        val values = ContentValues().apply {
            put("name", name)
            put("data", data)
            put("created_at", System.currentTimeMillis())
            put("updated_at", System.currentTimeMillis())
        }
        return writableDatabase.insert("draw_plans", null, values)
    }

    fun updatePlan(id: Long, name: String, data: String): Boolean {
        val values = ContentValues().apply {
            put("name", name)
            put("data", data)
            put("updated_at", System.currentTimeMillis())
        }
        return writableDatabase.update("draw_plans", values, "id=?", arrayOf(id.toString())) > 0
    }

    fun getPlanById(id: Long): SavedPlan? {
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM draw_plans WHERE id=?",
            arrayOf(id.toString())
        )
        return if (cursor.moveToFirst()) {
            val plan = SavedPlan(
                id   = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                data = cursor.getString(cursor.getColumnIndexOrThrow("data"))
            )
            cursor.close()
            plan
        } else {
            cursor.close()
            null
        }
    }

    fun getAllPlans(): List<SavedPlan> {
        val list = mutableListOf<SavedPlan>()
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM draw_plans ORDER BY updated_at DESC",
            null
        )
        while (cursor.moveToNext()) {
            list.add(SavedPlan(
                id   = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                data = cursor.getString(cursor.getColumnIndexOrThrow("data"))
            ))
        }
        cursor.close()
        return list
    }

    fun deletePlan(id: Long): Boolean {
        return writableDatabase.delete("draw_plans", "id=?", arrayOf(id.toString())) > 0
    }
    // ================= CONSTRUCTION NOTES =================

    fun insertNote(note: Note): Long {
        val values = ContentValues().apply {
            put("title", note.title)
            put("subtitle", note.subtitle)
            put("body", note.body)
            put("color", note.color)
            put("date", note.date)
        }
        return writableDatabase.insert("construction_notes", null, values)
    }

    fun updateNote(note: Note): Boolean {
        val values = ContentValues().apply {
            put("title", note.title)
            put("subtitle", note.subtitle)
            put("body", note.body)
            put("color", note.color)
            put("date", note.date)
        }
        return writableDatabase.update(
            "construction_notes", values, "id=?", arrayOf(note.id.toString())
        ) > 0
    }

    fun deleteNote(id: Long): Boolean {
        return writableDatabase.delete(
            "construction_notes", "id=?", arrayOf(id.toString())
        ) > 0
    }

    fun getAllNotes(): MutableList<Note> {
        val notes = mutableListOf<Note>()
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM construction_notes ORDER BY id DESC", null
        )
        if (cursor.moveToFirst()) {
            do {
                notes.add(
                    Note(
                        id       = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        title    = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        subtitle = cursor.getString(cursor.getColumnIndexOrThrow("subtitle")),
                        body     = cursor.getString(cursor.getColumnIndexOrThrow("body")),
                        color    = cursor.getInt(cursor.getColumnIndexOrThrow("color")),
                        date     = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return notes
    }

    fun searchNotes(query: String): MutableList<Note> {
        val notes = mutableListOf<Note>()
        val cursor = readableDatabase.rawQuery(
            """SELECT * FROM construction_notes 
           WHERE title LIKE ? OR subtitle LIKE ? OR body LIKE ?
           ORDER BY id DESC""",
            arrayOf("%$query%", "%$query%", "%$query%")
        )
        if (cursor.moveToFirst()) {
            do {
                notes.add(
                    Note(
                        id       = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        title    = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        subtitle = cursor.getString(cursor.getColumnIndexOrThrow("subtitle")),
                        body     = cursor.getString(cursor.getColumnIndexOrThrow("body")),
                        color    = cursor.getInt(cursor.getColumnIndexOrThrow("color")),
                        date     = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return notes
    }
}