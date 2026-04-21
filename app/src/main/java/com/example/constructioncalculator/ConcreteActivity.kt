package com.example.constructioncalculator

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ConcreteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_concrete)

        val img = findViewById<ImageView>(R.id.imgElement)
        val elementSp = findViewById<Spinner>(R.id.spinnerElement)
        val gradeSp = findViewById<Spinner>(R.id.spinnerGrade)

        val L = findViewById<EditText>(R.id.etLength)
        val W = findViewById<EditText>(R.id.etWidth)
        val H = findViewById<EditText>(R.id.etHeight)
        val O = findViewById<EditText>(R.id.etOpenings)

        val wasteBar = findViewById<SeekBar>(R.id.wasteBar)
        val btn = findViewById<Button>(R.id.btnCalc)
        val result = findViewById<TextView>(R.id.tvResult)

        // ELEMENTS
        val elements = arrayOf("Slab", "Beam", "Column", "Footing")
        elementSp.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, elements)

        // GRADES
        val grades = arrayOf("C20", "C25", "C30", "C35", "C40")
        gradeSp.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, grades)

        // IMAGE SWITCH
        elementSp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) {
                when (elements[pos]) {
                    "Slab" -> img.setImageResource(R.drawable.slab)
                    "Beam" -> img.setImageResource(R.drawable.beam)
                    "Column" -> img.setImageResource(R.drawable.column)
                    "Footing" -> img.setImageResource(R.drawable.footing)
                }
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        btn.setOnClickListener {

            val length = L.text.toString().toDoubleOrNull() ?: 0.0
            val width = W.text.toString().toDoubleOrNull() ?: 0.0
            val height = H.text.toString().toDoubleOrNull() ?: 0.0
            val openings = O.text.toString().toDoubleOrNull() ?: 0.0

            val waste = 1 + (wasteBar.progress / 100.0)

            // VOLUME
            val volume = (length * width * height - openings) * waste

            // MIX
            val (c, s, g) = when (gradeSp.selectedItem.toString()) {
                "C20" -> Triple(1.0, 2.5, 4.5)
                "C25" -> Triple(1.0, 2.0, 4.0)
                "C30" -> Triple(1.0, 1.5, 3.0)
                "C35" -> Triple(1.0, 1.2, 2.5)
                else -> Triple(1.0, 1.0, 2.0)
            }

            val total = c + s + g
            val dry = volume * 1.54

            val cementKg = dry * (c / total) * 1440
            val bags = cementKg / 50

            // STEEL ENGINEER RANGE
            val (minR, maxR) = when (elementSp.selectedItem.toString()) {
                "Slab" -> 90 to 110
                "Beam" -> 140 to 180
                "Column" -> 180 to 250
                "Footing" -> 70 to 100
                else -> 100 to 120
            }

            val steel = volume * ((minR + maxR) / 2)

            result.text = """
                VOLUME: ${"%.2f".format(volume)} m³

                GRADE: ${gradeSp.selectedItem}

                CEMENT: ${bags.toInt()} bags
                MIX: $c:$s:$g

                STEEL: ${steel.toInt()} kg
            """.trimIndent()
        }
    }
}