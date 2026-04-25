package com.example.constructioncalculator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TankCalculatorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tank_calculator)

        val recycler = findViewById<RecyclerView>(R.id.recyclerTanks)

        val list = arrayListOf(
            Shape("Vertical Cylinder", R.drawable.ic_vertical_cylinder),
            Shape("Horizontal Cylinder", R.drawable.ic_horizontal_cylinder),
            Shape("Rectangular Tank", R.drawable.ic_rectangular_prism),
            Shape("Cube Tank", R.drawable.ic_cube)
        )

        recycler.layoutManager = GridLayoutManager(this, 2)
        recycler.adapter = TankAdapter(this, list)
    }
}