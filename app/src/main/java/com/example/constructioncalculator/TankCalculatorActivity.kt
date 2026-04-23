package com.example.constructioncalculator

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView

class TankCalculatorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tank_calculator)

        // 🔷 Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // 🔙 زر الرجوع
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Tank Calculator"

        // 📦 Cards
        val vertical = findViewById<CardView>(R.id.card_vertical_cylinder)
        val horizontal = findViewById<CardView>(R.id.card_horizontal_cylinder)
        val rectangular = findViewById<CardView>(R.id.card_rectangular)
        val cube = findViewById<CardView>(R.id.card_cube)

        // ================= CLICK EVENTS =================

        vertical.setOnClickListener {
            startActivity(
                Intent(this, VerticalCylinderActivity::class.java)
            )
        }

        horizontal.setOnClickListener {
            startActivity(
                Intent(this, HorizontalCylinderActivity::class.java)
            )
        }

        rectangular.setOnClickListener {
            startActivity(
                Intent(this, RectangularTankActivity::class.java)
            )
        }

        cube.setOnClickListener {
            startActivity(
                Intent(this, CubeTankActivity::class.java)
            )
        }
    }

    // 🔙 الرجوع من Toolbar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}