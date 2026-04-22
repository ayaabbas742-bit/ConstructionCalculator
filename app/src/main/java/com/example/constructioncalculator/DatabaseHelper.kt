package com.example.constructioncalculator

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "ConstructionDB", null, 3) {

    // ================= CREATE DATABASE =================
    override fun onCreate(db: SQLiteDatabase) {

        // 👤 USERS
        db.execSQL("""
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                firstName TEXT,
                lastName TEXT,
                email TEXT UNIQUE,
                password TEXT,
                otp TEXT
            )
        """)

        // ⭐ FEEDBACK
        db.execSQL("""
            CREATE TABLE feedback (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT,
                rating REAL,
                note TEXT
            )
        """)

        // 🧱 BRICKS RESULTS
        db.execSQL("""
            CREATE TABLE results (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                bricks INTEGER,
                cement REAL,
                sand REAL,
                total REAL
            )
        """)

        // 🧱 PLASTER
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

        // 🎨 PAINT
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
        // 🧱 TABLE TILES فقط
        db.execSQL("""
            CREATE TABLE tiles (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                type TEXT,
                area REAL,
                count REAL,
                date TEXT
            )
        """)
    }

    // ================= UPGRADE =================
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS feedback")
        db.execSQL("DROP TABLE IF EXISTS results")
        db.execSQL("DROP TABLE IF EXISTS plaster")
        db.execSQL("DROP TABLE IF EXISTS paint_results")
        db.execSQL("DROP TABLE IF EXISTS tiles")

        onCreate(db)
    }

    // ================= USERS =================
    fun insertUser(first: String, last: String, email: String, password: String): Boolean {
        val db = writableDatabase

        val values = ContentValues().apply {
            put("firstName", first)
            put("lastName", last)
            put("email", email)
            put("password", password)
            put("otp", (1000..9999).random().toString())
        }

        val result = db.insert("users", null, values)
        db.close()

        return result != -1L
    }

    fun checkUser(email: String, password: String): Boolean {
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT id FROM users WHERE email=? AND password=?",
            arrayOf(email, password)
        )

        val ok = cursor.moveToFirst()
        cursor.close()
        db.close()

        return ok
    }

    fun getUser(email: String): User? {
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT firstName, lastName, email FROM users WHERE email=?",
            arrayOf(email)
        )

        var user: User? = null

        if (cursor.moveToFirst()) {
            user = User(
                cursor.getString(0),
                cursor.getString(1),
                cursor.getString(2)
            )
        }

        cursor.close()
        db.close()

        return user
    }

    // ================= OTP =================
    fun checkOtp(email: String, otp: String): Boolean {
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT id FROM users WHERE email=? AND otp=?",
            arrayOf(email, otp)
        )
        val ok = cursor.moveToFirst()
        cursor.close()
        db.close()

        return ok
    }

    fun generateOtp(email: String): String {
        val db = writableDatabase
        val otp = (1000..9999).random().toString()

        val values = ContentValues()
        values.put("otp", otp)

        db.update("users", values, "email=?", arrayOf(email))
        db.close()

        return otp
    }

    fun updatePassword(email: String, newPassword: String): Boolean {
        val db = writableDatabase

        val values = ContentValues()
        values.put("password", newPassword)

        val result = db.update("users", values, "email=?", arrayOf(email)) > 0

        db.close()
        return result
    }

    // ================= FEEDBACK =================
    fun insertFeedback(email: String, rating: Float, note: String) {
        val db = writableDatabase

        val values = ContentValues()
        values.put("email", email)
        values.put("rating", rating)
        values.put("note", note)

        db.insert("feedback", null, values)
        db.close()
    }

    // ================= BRICKS =================
    fun insertResult(bricks: Int, cement: Double, sand: Double, total: Double): Boolean {
        val db = writableDatabase

        val values = ContentValues()
        values.put("bricks", bricks)
        values.put("cement", cement)
        values.put("sand", sand)
        values.put("total", total)

        val result = db.insert("results", null, values)
        db.close()

        return result != -1L
    }

    fun getAllResults(): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM results ORDER BY id DESC", null)

        val builder = StringBuilder()

        while (cursor.moveToNext()) {
            builder.append(
                "Bricks: ${cursor.getInt(1)}\n" +
                        "Cement: ${cursor.getDouble(2)}\n" +
                        "Sand: ${cursor.getDouble(3)}\n" +
                        "Total: ${cursor.getDouble(4)} DA\n\n"
            )
        }

        cursor.close()
        db.close()

        return builder.toString()
    }

    // ================= PLASTER =================
    fun insertPlaster(
        area: Double,
        thickness: Double,
        ratio: String,
        cement: Double,
        sand: Double,
        water: Double,
        volume: Double
    ) {
        val db = writableDatabase

        val values = ContentValues().apply {
            put("area", area)
            put("thickness", thickness)
            put("ratio", ratio)
            put("cement", cement)
            put("sand", sand)
            put("water", water)
            put("volume", volume)
        }

        db.insert("plaster", null, values)
        db.close()
    }

    // ================= PAINT =================
    fun insertPaint(type: String, area: Double, coats: Int, paint: Double, date: String) {
        val db = writableDatabase

        val values = ContentValues().apply {
            put("type", type)
            put("area", area)
            put("coats", coats)
            put("paint", paint)
            put("date", date)
        }

        db.insert("paint_results", null, values)
        db.close()
    }
    fun getUserName(email: String): String {
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT firstName FROM users WHERE email=?",
            arrayOf(email)
        )

        var name = ""

        if (cursor.moveToFirst()) {
            name = cursor.getString(0)
        }

        cursor.close()
        db.close()

        return name
    }
    // ================= 🧱 INSERT TILES =================
    fun insertTile(type: String, area: Double, count: Double, date: String) {
        val db = writableDatabase
        val values = ContentValues()

        values.put("type", type)
        values.put("area", area)
        values.put("count", count)
        values.put("date", date)

        db.insert("tiles", null, values)
    }

}