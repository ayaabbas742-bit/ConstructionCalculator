package com.example.constructioncalculator

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SteelActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_steel)

        val img = findViewById<ImageView>(R.id.imgSteel)
        val spinner = findViewById<Spinner>(R.id.spinnerType)

        val etL = findViewById<EditText>(R.id.etLength)
        val etW = findViewById<EditText>(R.id.etWidth)
        val etH = findViewById<EditText>(R.id.etHeight)

        val tvVolume = findViewById<TextView>(R.id.tvVolume)
        val tvSteel = findViewById<TextView>(R.id.tvSteel)
        val tvTon = findViewById<TextView>(R.id.tvTon)

        val btn = findViewById<Button>(R.id.btnCalc)

        val types = arrayOf("Slab", "Beam", "Column")

        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            types
        )

        // تغيير الصورة
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {

                when (types[position]) {
                    "Slab" -> img.setImageResource(R.drawable.steel_slab)
                    "Beam" -> img.setImageResource(R.drawable.steel_beam)
                    "Column" -> img.setImageResource(R.drawable.steel_column)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // زر الحساب
        btn.setOnClickListener {

            val l = etL.text.toString().toDoubleOrNull() ?: 0.0
            val w = etW.text.toString().toDoubleOrNull() ?: 0.0
            val h = etH.text.toString().toDoubleOrNull() ?: 0.0

            if (l <= 0 ||w <= 0 || h <= 0) {
            Toast.makeText(this, "أدخل القيم", Toast.LENGTH_SHORT).show()
            return@setOnClickListener
        }

            val type = spinner.selectedItem.toString()

            var volume = 0.0
            var steel = 0.0

            when (type) {

                "Slab" -> {
                    volume = l * w * h
                    steel = volume * 100
                }

                "Beam" -> {
                    volume = l * w * h
                    steel = volume * 150
                }

                "Column" -> {
                    volume = w * w * h
                    steel = volume * 200
                }
            }

            val ton = steel / 1000

            tvVolume.text = "%.2f m³".format(volume)
            tvSteel.text = "%.2f kg".format(steel)
            tvTon.text = "%.2f t".format(ton)
        }
    }
}