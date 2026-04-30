package com.example.constructioncalculator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

//  مُصحَّح: onDelete يستقبل email (String) بدل id (Int)
class UsersAdapter(
    private val list: List<Map<String, String>>,
    private val onDelete: (String) -> Unit   // ← String (email)
) : RecyclerView.Adapter<UsersAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val name  = v.findViewById<TextView>(R.id.txtName)
        val email = v.findViewById<TextView>(R.id.txtEmail)
        val role  = v.findViewById<TextView>(R.id.txtRole)
        val del   = v.findViewById<ImageView>(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val u = list[position]

        holder.name.text  = "${u["firstName"]} ${u["lastName"]}"
        holder.email.text = u["email"]

        //  عرض الدور بشكل واضح
        holder.role.text = if (u["role"] == "admin") "🛡 Admin" else "👤 User"

        //  تمرير email بدل id
        holder.del.setOnClickListener {
            onDelete(u["email"] ?: "")
        }
    }

    override fun getItemCount() = list.size
}