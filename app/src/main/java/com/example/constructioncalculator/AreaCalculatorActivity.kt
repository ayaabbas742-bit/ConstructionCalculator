package com.example.constructioncalculator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AreaCalculatorActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ShapeAdapter
    private lateinit var list: ArrayList<Shape>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_area_calculator)

        recyclerView = findViewById(R.id.recyclerShapes)

        list = ArrayList()

        list.add(Shape("Trapezoid", R.drawable.trapezoid))
        list.add(Shape("Square", R.drawable.square))
        list.add(Shape("Rectangle", R.drawable.rectangle))
        list.add(Shape("Triangle", R.drawable.triangle))

        adapter = ShapeAdapter(this, list)

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter
    }
}