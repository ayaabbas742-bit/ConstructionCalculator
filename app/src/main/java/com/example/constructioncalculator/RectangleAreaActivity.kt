package com.example.constructioncalculator

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RectangleAreaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rectangle_area)

        val editLength = findViewById<EditText>(R.id.editLength)
        val editWidth = findViewById<EditText>(R.id.editWidth)
        val btnCalculate = findViewById<Button>(R.id.btnCalculate)
        val txtArea = findViewById<TextView>(R.id.txtArea)
        val txtPerimeter = findViewById<TextView>(R.id.txtPerimeter)

        btnCalculate.setOnClickListener {
            val lengthText = editLength.text.toString()
            val widthText = editWidth.text.toString()

            if (lengthText.isNotEmpty() && widthText.isNotEmpty()) {
                try {
                    val length = lengthText.toDouble()
                    val width = widthText.toDouble()

                    val area = length * width
                    val perimeter = 2 * (length + width)

                    txtArea.text = "Area = %.2f m²".format(area)
                    txtPerimeter.text = "Perimeter = %.2f m".format(perimeter)
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Enter valid numbers", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter length and width", Toast.LENGTH_SHORT).show()
            }
        }
    }
}