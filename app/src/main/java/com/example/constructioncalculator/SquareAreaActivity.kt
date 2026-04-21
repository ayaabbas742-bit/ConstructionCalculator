package com.example.constructioncalculator

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SquareAreaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_square_area)

        val editSide = findViewById<EditText>(R.id.editSide)
        val btnCalculate = findViewById<Button>(R.id.btnCalculate)
        val txtArea = findViewById<TextView>(R.id.txtArea)
        val txtPerimeter = findViewById<TextView>(R.id.txtPerimeter)

        btnCalculate.setOnClickListener {
            val sideText = editSide.text.toString()
            if (sideText.isNotEmpty()) {
                try {
                    val side = sideText.toDouble()
                    txtArea.text = "Area = %.2f m²".format(side * side)
                    txtPerimeter.text = "Perimeter = %.2f m".format(4 * side)
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Enter a valid number", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter side length", Toast.LENGTH_SHORT).show()
            }
        }
    }
}