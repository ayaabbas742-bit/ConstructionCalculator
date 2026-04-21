package com.example.constructioncalculator

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class LShapeAreaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lshape_area)

        val bigL = findViewById<EditText>(R.id.editBigLength)
        val bigW = findViewById<EditText>(R.id.editBigWidth)
        val cutL = findViewById<EditText>(R.id.editCutLength)
        val cutW = findViewById<EditText>(R.id.editCutWidth)

        val btn = findViewById<Button>(R.id.btnCalculate)
        val result = findViewById<TextView>(R.id.txtArea)

        btn.setOnClickListener {

            val L = bigL.text.toString().toDoubleOrNull()
            val W = bigW.text.toString().toDoubleOrNull()
            val l = cutL.text.toString().toDoubleOrNull()
            val w = cutW.text.toString().toDoubleOrNull()

            if (L == null||  W == null || l == null || w == null) {
            Toast.makeText(this, "Enter valid values", Toast.LENGTH_SHORT).show()
            return@setOnClickListener
        }

            val area = (L * W) - (l * w)

            result.text = "Area = $area m²"
        }
    }
}