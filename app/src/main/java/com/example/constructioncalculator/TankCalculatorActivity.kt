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

        verticalCylinder.setOnClickListener {
            val intent = Intent(this, TankVolumeActivity::class.java)
            startActivity(intent)
        }

        horizontalCylinder.setOnClickListener {
            val intent = Intent(this, TankVolumeActivity::class.java)
            startActivity(intent)
        }

        rectangular.setOnClickListener {
            val intent = Intent(this, TankVolumeActivity::class.java)
            startActivity(intent)
        }

        cube.setOnClickListener {
            val intent = Intent(this, TankVolumeActivity::class.java)
            startActivity(intent)
        }
    }
}