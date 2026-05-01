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
    private val currentEmail: String,
    private val onDelete: (String) -> Unit
) : RecyclerView.Adapter<UsersAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val name  = v.findViewById<TextView>(R.id.txtName)
        val email = v.findViewById<TextView>(R.id.txtEmail)
        val role  = v.findViewById<TextView>(R.id.txtRole)
        val del   = v.findViewById<ImageView>(R.id.btnDelete)
    }

    // ⭐ هذا هو الناقص عندك
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {

        val u = list[position]
        val email = u["email"] ?: ""
        val role = u["role"] ?: ""

        holder.name.text = "${u["firstName"]} ${u["lastName"]}"
        holder.email.text = email
        holder.role.text = if (role == "admin") "🛡 Admin" else "👤 User"

        if (email == currentEmail || role == "admin") {
            holder.del.visibility = View.GONE
        } else {
            holder.del.visibility = View.VISIBLE
        }

        holder.del.setOnClickListener {
            onDelete(email)
        }
    }

    override fun getItemCount(): Int = list.size
}