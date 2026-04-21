package com.example.constructioncalculator

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.sqrt

class TriangleAreaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_triangle_area)

        val editA = findViewById<EditText>(R.id.editSideA)
        val editB = findViewById<EditText>(R.id.editSideB)
        val editC = findViewById<EditText>(R.id.editSideC)
        val btnCalculate = findViewById<Button>(R.id.btnCalculate)
        val txtArea = findViewById<TextView>(R.id.txtArea)
        val txtPerimeter = findViewById<TextView>(R.id.txtPerimeter)

        btnCalculate.setOnClickListener {
            val aText = editA.text.toString()
            val bText = editB.text.toString()
            val cText = editC.text.toString()

            if (aText.isNotEmpty() && bText.isNotEmpty() && cText.isNotEmpty()) {
                try {
                    val a = aText.toDouble()
                    val b = bText.toDouble()
                    val c = cText.toDouble()

                    // تحقق إذا المثلث صالح
                    if (a + b > c && a + c > b && b + c > a) {
                        val perimeter = a + b + c
                        val s = perimeter / 2
                        val area = sqrt(s * (s - a) * (s - b) * (s - c))

                        txtArea.text = "Area = %.2f m²".format(area)
                        txtPerimeter.text = "Perimeter = %.2f m".format(perimeter)
                    } else {
                        Toast.makeText(this, "Invalid triangle sides", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Enter valid numbers", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter all three sides", Toast.LENGTH_SHORT).show()
            }
        }
    }
}