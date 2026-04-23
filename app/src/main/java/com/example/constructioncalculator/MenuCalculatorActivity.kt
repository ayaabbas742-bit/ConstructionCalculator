package com.example.constructioncalculator

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class MenuCalculatorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_calculator)

        // ================= CARDS =================
        val brick = findViewById<MaterialCardView>(R.id.card_bricks)
        val tiles = findViewById<MaterialCardView>(R.id.card_tiles)
        val plaster = findViewById<MaterialCardView>(R.id.card_plaster)
        val concrete = findViewById<MaterialCardView>(R.id.card_concrete)
        val paint = findViewById<MaterialCardView>(R.id.card_paint)

        // ================= CLICK EVENTS =================
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