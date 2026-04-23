package com.example.constructioncalculator

import android.content.Context

object NoteManager {

    private lateinit var db: DatabaseHelper

    fun init(context: Context) {
        db = DatabaseHelper(context.applicationContext)
    }

    fun getAllNotes(): MutableList<Note> = db.getAllNotes()

    fun add(note: Note): Note {
        val newId = db.insertNote(note)
        return note.copy(id = newId)
    }

    fun update(note: Note) = db.updateNote(note)

    fun delete(id: Long) = db.deleteNote(id)

    fun search(query: String): MutableList<Note> = db.searchNotes(query)
}