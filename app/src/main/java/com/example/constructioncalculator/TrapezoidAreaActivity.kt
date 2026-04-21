package com.example.constructioncalculator

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TrapezoidAreaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trapezoid_area)

        val editBase1 = findViewById<EditText>(R.id.editBase1)
        val editBase2 = findViewById<EditText>(R.id.editBase2)
        val editHeight = findViewById<EditText>(R.id.editHeight)
        val editSide1 = findViewById<EditText>(R.id.editSide1)
        val editSide2 = findViewById<EditText>(R.id.editSide2)
        val btnCalculate = findViewById<Button>(R.id.btnCalculate)
        val txtArea = findViewById<TextView>(R.id.txtArea)
        val txtPerimeter = findViewById<TextView>(R.id.txtPerimeter)

        btnCalculate.setOnClickListener {
            val base1Text = editBase1.text.toString()
            val base2Text = editBase2.text.toString()
            val heightText = editHeight.text.toString()
            val side1Text = editSide1.text.toString()
            val side2Text = editSide2.text.toString()

            if (base1Text.isNotEmpty() && base2Text.isNotEmpty() && heightText.isNotEmpty()) {
                try {
                    val base1 = base1Text.toDouble()
                    val base2 = base2Text.toDouble()
                    val height = heightText.toDouble()

                    // حساب المساحة
                    val area = (base1 + base2) * height / 2
                    txtArea.text = "Area = %.2f m²".format(area)

                    // حساب المحيط إذا أدخل المستخدم الجانبين، وإلا نضع "-"
                    val perimeter = if (side1Text.isNotEmpty() && side2Text.isNotEmpty()) {
                        val side1 = side1Text.toDouble()
                        val side2 = side2Text.toDouble()
                        base1 + base2 + side1 + side2
                    } else {
                        null
                    }

                    txtPerimeter.text = if (perimeter != null)
                        "Perimeter = %.2f m".format(perimeter)
                    else
                        "Perimeter = -"
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Enter valid numbers", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter bases and height", Toast.LENGTH_SHORT).show()
            }
        }
    }
}