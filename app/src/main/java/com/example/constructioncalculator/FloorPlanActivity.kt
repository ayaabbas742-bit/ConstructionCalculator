package com.example.constructioncalculator

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FloorPlanActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: DesignAdapter
    private lateinit var allDesigns: List<Design>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_floor_plan)

        val spinner = findViewById<Spinner>(R.id.spinnerSize)
        recycler = findViewById(R.id.recyclerDesigns)

        recycler.layoutManager = GridLayoutManager(this, 2)

        // ✅ جميع التصاميم (كل مساحة فيها 5)
        allDesigns = listOf(

            // 100 m²
            Design("Design 1", R.drawable.design1, "100 m²"),
            Design("Design 2", R.drawable.design2, "100 m²"),
            Design("Design 3", R.drawable.design3, "100 m²"),
            Design("Design 4", R.drawable.design4, "100 m²"),
            Design("Design 5", R.drawable.design5, "100 m²"),
            Design("Design 5", R.drawable.design6, "100 m²"),


            // 120 m²
            Design("Design 6", R.drawable.design7, "120 m²"),
            Design("Design 7", R.drawable.design7, "120 m²"),
            Design("Design 8", R.drawable.design8, "120 m²"),
            Design("Design 9", R.drawable.design9, "120 m²"),
            Design("Design 10", R.drawable.design10, "120 m²"),

            // 150 m²
            Design("Design 11", R.drawable.design11, "150 m²"),
            Design("Design 12", R.drawable.design12, "150 m²"),
            Design("Design 13", R.drawable.design13, "150 m²"),
            Design("Design 14", R.drawable.design14, "150 m²"),
            Design("Design 15", R.drawable.design15, "150 m²"),

            // 180 m²
            Design("Design 16", R.drawable.design16, "180 m²"),
            Design("Design 17", R.drawable.design17, "180 m²"),
            Design("Design 18", R.drawable.design18, "180 m²"),
            Design("Design 19", R.drawable.design19, "180 m²"),
            Design("Design 20", R.drawable.design20, "180 m²"),

            // 200 m²
            Design("Design 21", R.drawable.design21, "200 m²"),
            Design("Design 22", R.drawable.design22, "200 m²"),
            Design("Design 23", R.drawable.design23, "200 m²"),
            Design("Design 24", R.drawable.design24, "200 m²"),
            Design("Design 25", R.drawable.design25, "200 m²"),
            // 250 m²
            Design("Design 26", R.drawable.design26, "250 m²"),
            Design("Design 27", R.drawable.design27, "250 m²"),
            Design("Design 28", R.drawable.design28, "250 m²"),
            Design("Design 29", R.drawable.design29, "250 m²"),
            Design("Design 30", R.drawable.design30, "250 m²")
        )

        // ✅ Spinner
        val sizes = arrayOf("100 m²", "120 m²", "150 m²", "180 m²", "200 m²","250 m²")

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sizes)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        // أول عرض
        filterDesigns("100 m²")

        // عند تغيير الاختيار
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedSize = sizes[position]
                filterDesigns(selectedSize)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // ✅ فلترة التصاميم
    private fun filterDesigns(size: String) {
        val filteredList = allDesigns.filter { it.size == size }
        adapter = DesignAdapter(filteredList)
        recycler.adapter = adapter
    }
}