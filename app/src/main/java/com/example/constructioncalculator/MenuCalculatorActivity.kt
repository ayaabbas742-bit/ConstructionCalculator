package com.example.constructioncalculator

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MenuCalculatorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_calculator)

        val brick = findViewById<LinearLayout>(R.id.card_bricks)
        val tiles = findViewById<LinearLayout>(R.id.card_tiles)
        val plaster = findViewById<LinearLayout>(R.id.card_plaster)
        val concrete = findViewById<LinearLayout>(R.id.card_concrete)
        val paint = findViewById<LinearLayout>(R.id.card_paint)

        brick.setOnClickListener {
            startActivity(Intent(this, BrickActivity::class.java))
        }

        tiles.setOnClickListener {
            startActivity(Intent(this, TilesActivity::class.java))
        }

        plaster.setOnClickListener {
            startActivity(Intent(this, PlasterActivity::class.java))
        }

        concrete.setOnClickListener {
            startActivity(Intent(this, ConcreteActivity::class.java))
        }

        paint.setOnClickListener {
            startActivity(Intent(this, PaintActivity::class.java))
        }
    }
}