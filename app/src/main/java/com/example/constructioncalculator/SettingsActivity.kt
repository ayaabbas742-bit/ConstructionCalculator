package com.example.constructioncalculator

import android.app.AlertDialog
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    lateinit var db: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // إنشاء قاعدة البيانات مباشرة هنا
        val helper = object : SQLiteOpenHelper(this, "app_db", null, 1) {
            override fun onCreate(db: SQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE feedback (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "rating REAL," +
                            "note TEXT)"
                )
            }

            override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
                db.execSQL("DROP TABLE IF EXISTS feedback")
                onCreate(db)
            }
        }

        db = helper.writableDatabase

        showDialog()
    }

    private fun showDialog() {

        val view = layoutInflater.inflate(R.layout.dialog_settings, null)

        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
        val notes = view.findViewById<EditText>(R.id.notes)
        val saveBtn = view.findViewById<Button>(R.id.saveBtn)

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        saveBtn.setOnClickListener {

            val rating = ratingBar.rating
            val note = notes.text.toString()

            val values = ContentValues()
            values.put("rating", rating)
            values.put("note", note)

            db.insert("feedback", null, values)

            Toast.makeText(this, "Saved ✔", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
}