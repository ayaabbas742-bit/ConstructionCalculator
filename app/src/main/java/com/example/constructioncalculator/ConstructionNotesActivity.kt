package com.example.constructioncalculator

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.constructioncalculator.databinding.ActivityConstructionNotesBinding

class ConstructionNotesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConstructionNotesBinding
    private lateinit var adapter: NotesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConstructionNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        setupRecyclerView()
        setupSearch()
        setupFab()
        setupBack()
    }

    private fun setupRecyclerView() {
        adapter = NotesAdapter(
            notes = NoteManager.getAllNotes(),
            onClick = { note ->
                val intent = Intent(this, NoteEditActivity::class.java)
                intent.putExtra("note_id", note.id)
                startActivity(intent)
            },
            onDelete = { note ->
                NoteManager.delete(note.id)
                adapter.updateList(NoteManager.getAllNotes())
            }
        )
        binding.recyclerNotes.layoutManager = LinearLayoutManager(this)
        binding.recyclerNotes.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                val result = if (query.isEmpty())
                    NoteManager.getAllNotes()
                else
                    NoteManager.search(query)
                adapter.updateList(result)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, NoteEditActivity::class.java))
        }
    }

    private fun setupBack() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.updateList(NoteManager.getAllNotes())
    }
}