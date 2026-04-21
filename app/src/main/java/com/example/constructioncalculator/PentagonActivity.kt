package com.example.constructioncalculator

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class PentagonActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pentagon)

        val side = findViewById<EditText>(R.id.editSide)
        val apothem = findViewById<EditText>(R.id.editApothem)
        val result = findViewById<TextView>(R.id.txtArea)
        val btn = findViewById<Button>(R.id.btnCalculate)

        btn.setOnClickListener {

            val s = side.text.toString().toDoubleOrNull()
            val a = apothem.text.toString().toDoubleOrNull()

            if (s == null || a == null) {
                Toast.makeText(this, "Enter valid values", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val perimeter = 5 * s
            val area = (perimeter * a) / 2

            result.text = "Area = $area m²"
        }
    }
}