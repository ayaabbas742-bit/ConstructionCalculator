package com.example.constructioncalculator

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class DesignAdapter(private val list: List<Design>) :
    RecyclerView.Adapter<DesignAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgDesign: ImageView = view.findViewById(R.id.imgDesign)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_design, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        // عرض البيانات
        holder.tvTitle.text = item.title
        holder.imgDesign.setImageResource(item.imageRes)

        // 🔹 الضغط على التصميم
        holder.itemView.setOnClickListener {

            // 🔸 اختبار (تأكد أن الضغط يعمل)
            Toast.makeText(holder.itemView.context, "تم الضغط", Toast.LENGTH_SHORT).show()

            // 🔸 فتح صفحة الصورة
            val intent = Intent(holder.itemView.context, DesignDetailActivity::class.java)
            intent.putExtra("imageRes", item.imageRes)
            holder.itemView.context.startActivity(intent)
        }
    }
}