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

    private val colors = listOf(
        "#B0BEC5", // رمادي زيتوني
        "#B0BEC5",
        "#B0BEC5",
        "#B0BEC5",
        "#B0BEC5",
        "#B0BEC5",
        "#B0BEC5",
        "#B0BEC5",
        "#B0BEC5"
    )
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val icon: ImageView = view.findViewById(R.id.icon)
        val card: CardView = view.findViewById(R.id.card)
        val colorBox: CardView = view.findViewById(R.id.colorBox)
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
        holder.colorBox.setCardBackgroundColor(Color.parseColor(colors[position]))

        holder.card.setOnClickListener {
            when (item.title) {
                "Construction Calculator" -> holder.itemView.context.startActivity(Intent(holder.itemView.context, MenuCalculatorActivity::class.java))
                "Area Calculator" -> holder.itemView.context.startActivity(Intent(holder.itemView.context, AreaCalculatorActivity::class.java))
                "Tank Calculator" -> holder.itemView.context.startActivity(Intent(holder.itemView.context, TankCalculatorActivity::class.java))
                "Floor Plan" -> holder.itemView.context.startActivity(Intent(holder.itemView.context, FloorPlanActivity::class.java))
                "Plan Drawing" -> holder.itemView.context.startActivity(Intent(holder.itemView.context, PlanDrawingActivity::class.java))
                "Construction Notes" -> holder.itemView.context.startActivity(Intent(holder.itemView.context, ConstructionNotesActivity::class.java))
                "Create Invoice" -> holder.itemView.context.startActivity(Intent(holder.itemView.context, InvoiceListActivity::class.java))
            }
        }
    }
}