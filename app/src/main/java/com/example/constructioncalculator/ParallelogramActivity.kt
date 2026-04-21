package com.example.constructioncalculator

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ParallelogramActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parallelogram)

        val base = findViewById<EditText>(R.id.editBase)
        val height = findViewById<EditText>(R.id.editHeight)
        val result = findViewById<TextView>(R.id.txtArea)
        val btn = findViewById<Button>(R.id.btnCalculate)

        btn.setOnClickListener {

            val b = base.text.toString().toDoubleOrNull()
            val h = height.text.toString().toDoubleOrNull()

            if (b == null || h == null) {
                Toast.makeText(this, "Enter valid values", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val area = b * h
            result.text = "Area = $area m²"
        }
    }
}