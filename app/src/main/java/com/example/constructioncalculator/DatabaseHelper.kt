package com.example.constructioncalculator

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "ConstructionDB", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {

        // ================= USERS =================
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

        // ================= FEEDBACK =================
        db.execSQL("""
            CREATE TABLE feedback (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT,
                rating REAL,
                note TEXT
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS feedback")
        onCreate(db)
    }

    // ================= SIGN UP =================
    fun insertUser(first: String, last: String, email: String, password: String): Boolean {

        val db = writableDatabase
        val values = ContentValues()

        values.put("firstName", first)
        values.put("lastName", last)
        values.put("email", email)
        values.put("password", password)

        // OTP initial
        values.put("otp", (1000..9999).random().toString())

        return db.insert("users", null, values) != -1L
    }

    // ================= LOGIN =================
    fun checkUser(email: String, password: String): Boolean {

        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT id FROM users WHERE email=? AND password=?",
            arrayOf(email, password)
        )

        val ok = cursor.moveToFirst()
        cursor.close()

        return ok
    }

    // ================= GET USER NAME =================
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
        return name
    }

    // ================= GET FULL USER =================
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
        return user
    }

    // ================= OTP CHECK =================
    fun checkOtp(email: String, otp: String): Boolean {

        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT id FROM users WHERE email=? AND otp=?",
            arrayOf(email, otp)
        )

        val ok = cursor.moveToFirst()
        cursor.close()

        return ok
    }

    // ================= GENERATE OTP =================
    fun generateOtp(email: String): String {

        val db = writableDatabase

        val otp = (1000..9999).random().toString()

        val values = ContentValues()
        values.put("otp", otp)

        db.update(
            "users",
            values,
            "email=?",
            arrayOf(email)
        )

        return otp
    }

    // ================= UPDATE PASSWORD =================
    fun updatePassword(email: String, newPassword: String): Boolean {

        val db = writableDatabase
        val values = ContentValues()
        values.put("password", newPassword)

        return db.update(
            "users",
            values,
            "email=?",
            arrayOf(email)
        ) > 0
    }

    // ================= FEEDBACK =================
    fun insertFeedback(email: String, rating: Float, note: String) {

        val db = writableDatabase
        val values = ContentValues()

        values.put("email", email)
        values.put("rating", rating)
        values.put("note", note)

        db.insert("feedback", null, values)
    }
}