package com.example.constructioncalculator

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class TankCalculatorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tank_calculator)

        val verticalCylinder = findViewById<CardView>(R.id.card_vertical_cylinder)
        val horizontalCylinder = findViewById<CardView>(R.id.card_horizontal_cylinder)
        val rectangular = findViewById<CardView>(R.id.card_rectangular)
        val cube = findViewById<CardView>(R.id.card_cube)

        // ✔ Vertical Cylinder
        verticalCylinder.setOnClickListener {
            startActivity(Intent(this, VerticalCylinderActivity::class.java))
        }

        // ✔ Horizontal Cylinder
        horizontalCylinder.setOnClickListener {
            startActivity(Intent(this, HorizontalCylinderActivity::class.java))
        }

        // ✔ Rectangular Prism
        rectangular.setOnClickListener {
            startActivity(Intent(this, RectangularTankActivity::class.java))
        }

        // ✔ Cube
        cube.setOnClickListener {
            startActivity(Intent(this, CubeTankActivity::class.java))
        }
    }
}