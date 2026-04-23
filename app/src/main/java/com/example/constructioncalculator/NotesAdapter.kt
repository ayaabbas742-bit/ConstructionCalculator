package com.example.constructioncalculator

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.constructioncalculator.databinding.ItemNoteBinding

class NotesAdapter(
    private var notes: MutableList<Note>,
    private val onClick: (Note) -> Unit,
    private val onDelete: (Note) -> Unit
) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    inner class NoteViewHolder(val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.binding.tvTitle.text    = note.title.ifEmpty { "(بدون عنوان)" }
        holder.binding.tvSubtitle.text = note.subtitle
        holder.binding.tvBody.text     = if (note.body.length > 80)
            note.body.take(80) + "..." else note.body
        holder.binding.tvDate.text     = note.date
        holder.binding.colorBar.setBackgroundColor(note.color)
        holder.binding.root.setOnClickListener { onClick(note) }
        holder.binding.btnDelete.setOnClickListener { onDelete(note) }
    }

    override fun getItemCount() = notes.size

    fun updateList(newList: MutableList<Note>) {
        notes = newList
        notifyDataSetChanged()
    }
}