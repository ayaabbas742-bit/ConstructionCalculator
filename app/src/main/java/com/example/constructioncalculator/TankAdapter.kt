package com.example.constructioncalculator

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TankAdapter(
    private val context: Context,
    private val list: ArrayList<Shape>
) : RecyclerView.Adapter<TankAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.shapeImage)
        val name: TextView = itemView.findViewById(R.id.shapeName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shape, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.name.text = item.name
        holder.image.setImageResource(item.image)

        holder.itemView.setOnClickListener {
            val intent = when (item.name) {
                "Vertical Cylinder" -> Intent(context, VerticalCylinderActivity::class.java)
                "Horizontal Cylinder" -> Intent(context, HorizontalCylinderActivity::class.java)
                "Rectangular Tank" -> Intent(context, RectangularTankActivity::class.java)
                "Cube Tank" -> Intent(context, CubeTankActivity::class.java)
                else -> null
            }
            intent?.let { context.startActivity(it) }
        }
    }
}