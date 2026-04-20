package com.example.constructioncalculator

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ShapeAdapter(
    private val context: Context,
    private val list: ArrayList<Shape>
) : RecyclerView.Adapter<ShapeAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.shapeImage)
        val name: TextView = itemView.findViewById(R.id.shapeName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shape, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val shape = list[position]

        holder.name.text = shape.name
        holder.image.setImageResource(shape.image)

        holder.itemView.setOnClickListener {

            val intent = when (shape.name) {

                "Trapezoid" -> Intent(context, TrapezoidAreaActivity::class.java)
                "Square" -> Intent(context, SquareAreaActivity::class.java)
                "Rectangle" -> Intent(context, RectangleAreaActivity::class.java)
                "Triangle" -> Intent(context, TriangleAreaActivity::class.java)

                else -> null
            }

            intent?.let { context.startActivity(it) }
        }
    }
}