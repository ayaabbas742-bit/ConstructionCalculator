package com.example.constructioncalculator

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class FeatureAdapter(private val list: List<Feature>) :
    RecyclerView.Adapter<FeatureAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val icon: ImageView = view.findViewById(R.id.icon)
        val card: CardView = view.findViewById(R.id.card)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feature, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.title.text = item.title
        holder.icon.setImageResource(item.icon)

        holder.card.setCardBackgroundColor(Color.parseColor("#DFF5E1"))

        // 🔹 الضغط على البطاقة لكل ميزة
        holder.card.setOnClickListener {
            when (item.title) {

                "Construction Calculator" -> {
                    val intent = Intent(holder.itemView.context, MenuCalculatorActivity::class.java)
                    holder.itemView.context.startActivity(intent)
                }

                "Area Calculator" -> {
                    val intent = Intent(holder.itemView.context, AreaCalculatorActivity::class.java)
                    holder.itemView.context.startActivity(intent)
                }

                "Tank Calculator" -> {
                    val intent = Intent(holder.itemView.context, TankCalculatorActivity::class.java)
                    holder.itemView.context.startActivity(intent)
                }

                "Floor Plan" -> {
                    val intent = Intent(holder.itemView.context, FloorPlanActivity::class.java)
                    holder.itemView.context.startActivity(intent)
                }
                "Plan Drawing" -> {
                    val intent = Intent(holder.itemView.context, PlanDrawingActivity::class.java)
                    holder.itemView.context.startActivity(intent)
                }
                "Construction Notes" -> {
                    val intent = Intent(holder.itemView.context, ConstructionNotesActivity::class.java)
                    holder.itemView.context.startActivity(intent)
                }
            }
        }
    }
}