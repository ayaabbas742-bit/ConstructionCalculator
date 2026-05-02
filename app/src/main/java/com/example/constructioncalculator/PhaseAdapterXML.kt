package com.example.constructioncalculator

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PhaseAdapterXML(
    private val phases: List<Phase>,
    private val onClick: (Phase) -> Unit
) : RecyclerView.Adapter<PhaseAdapterXML.PhaseVH>() {

    inner class PhaseVH(view: View) : RecyclerView.ViewHolder(view) {
        val statusBar: View = view.findViewById(R.id.statusBar)
        val ivPhaseIcon: ImageView = view.findViewById(R.id.ivPhaseIcon)
        val tvPhaseName: TextView = view.findViewById(R.id.tvPhaseName)
        val tvPhaseDates: TextView = view.findViewById(R.id.tvPhaseDates)
        val tvPhaseNotes: TextView = view.findViewById(R.id.tvPhaseNotes)
        val tvPhaseBadge: TextView = view.findViewById(R.id.tvPhaseBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhaseVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_phase, parent, false)
        return PhaseVH(view)
    }

    override fun onBindViewHolder(holder: PhaseVH, position: Int) {
        val phase = phases[position]

        holder.tvPhaseName.text = phase.name
        holder.tvPhaseDates.text = "${phase.startDate}  →  ${phase.endDate}"

        // Notes visibility
        if (phase.notes.isNotEmpty()) {
            holder.tvPhaseNotes.text = phase.notes
            holder.tvPhaseNotes.visibility = View.VISIBLE
        } else {
            holder.tvPhaseNotes.visibility = View.GONE
        }

        // ── Status colors ──
        when (phase.status) {
            "done" -> {
                holder.statusBar.setBackgroundResource(R.drawable.status_bar_done)
                holder.tvPhaseBadge.text = "✓ Done"
                holder.tvPhaseBadge.setBackgroundResource(R.drawable.bg_badge_green)
                holder.tvPhaseName.setTextColor(Color.parseColor("#27AE60"))
            }
            "active" -> {
                holder.statusBar.setBackgroundResource(R.drawable.status_bar_active)
                holder.tvPhaseBadge.text = "● Active"
                holder.tvPhaseBadge.setBackgroundResource(R.drawable.bg_badge_orange)
                holder.tvPhaseName.setTextColor(Color.parseColor("#E05C2A"))
            }
            "late" -> {
                holder.statusBar.setBackgroundResource(R.drawable.status_bar_late)
                holder.tvPhaseBadge.text = "! Late"
                holder.tvPhaseBadge.setBackgroundResource(R.drawable.bg_badge_red)
                holder.tvPhaseName.setTextColor(Color.parseColor("#E74C3C"))
            }
            else -> {
                holder.statusBar.setBackgroundResource(R.drawable.status_bar_pending)
                holder.tvPhaseBadge.text = "⏳ Pending"
                holder.tvPhaseBadge.setBackgroundResource(R.drawable.bg_badge_gray)
                holder.tvPhaseName.setTextColor(Color.parseColor("#1B2A4A"))
            }
        }

        // ── Phase Icon based on name ──
        val icon = when {
            phase.name.contains("Excavat", true) || phase.name.contains("حفر", true) ->
                R.drawable.ic_phase_excavation
            phase.name.contains("Found", true) || phase.name.contains("أساس", true) ->
                R.drawable.ic_phase_foundation
            phase.name.contains("Struct", true) || phase.name.contains("هيكل", true) ->
                R.drawable.ic_phase_structure
            phase.name.contains("Electric", true) || phase.name.contains("كهرب", true) ->
                R.drawable.ic_phase_electric
            phase.name.contains("Plumb", true) || phase.name.contains("سباكة", true) ->
                R.drawable.ic_phase_plumbing
            phase.name.contains("Paint", true) || phase.name.contains("دهان", true) ->
                R.drawable.ic_phase_paint
            phase.name.contains("Window", true) || phase.name.contains("نافذة", true) ->
                R.drawable.ic_phase_windows
            phase.name.contains("Finish", true) || phase.name.contains("تشطيب", true) ->
                R.drawable.ic_phase_finish
            else -> R.drawable.ic_phase_default
        }
        holder.ivPhaseIcon.setImageResource(icon)

        holder.itemView.setOnClickListener { onClick(phase) }
        holder.itemView.setOnLongClickListener { onClick(phase); true }

        // Animate entry
        holder.itemView.alpha = 0f
        holder.itemView.translationX = -30f
        holder.itemView.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(300)
            .setStartDelay((position * 60).toLong())
            .start()
    }

    override fun getItemCount() = phases.size
}