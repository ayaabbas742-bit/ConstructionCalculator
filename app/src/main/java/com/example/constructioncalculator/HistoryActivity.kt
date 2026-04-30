package com.example.constructioncalculator

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class HistoryActivity : AppCompatActivity() {

    private lateinit var db:         DatabaseHelper
    private lateinit var concreteDb: ConcreteDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        db         = DatabaseHelper(this)
        concreteDb = ConcreteDB(this)

        val tabHost = findViewById<TabHost>(R.id.tabHost)
        tabHost.setup()

        // ── 6 Tabs ───────────────────────────────────────────────────────────
        tabHost.addTab(tabHost.newTabSpec("stairs")
            .setIndicator("🪜 Stairs")   .setContent(R.id.tabStairs))
        tabHost.addTab(tabHost.newTabSpec("tiles")
            .setIndicator("🧱 Tiles")    .setContent(R.id.tabTiles))
        tabHost.addTab(tabHost.newTabSpec("concrete")
            .setIndicator("🏗️ Concrete") .setContent(R.id.tabConcrete))
        tabHost.addTab(tabHost.newTabSpec("brick")
            .setIndicator("🧱 Brick")    .setContent(R.id.tabBrick))
        tabHost.addTab(tabHost.newTabSpec("plaster")
            .setIndicator("🪣 Plaster")  .setContent(R.id.tabPlaster))
        tabHost.addTab(tabHost.newTabSpec("paint")
            .setIndicator("🎨 Paint")    .setContent(R.id.tabPaint))

        // ── Load all ─────────────────────────────────────────────────────────
        loadStairs()
        loadTiles()
        loadConcrete()
        loadBrick()
        loadPlaster()
        loadPaint()

        // ── Clear All ─────────────────────────────────────────────────────────
        findViewById<Button>(R.id.btnClearAll).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Clear History")
                .setMessage("Delete ALL records from all categories?")
                .setPositiveButton("Delete") { _, _ ->
                    db.writableDatabase.execSQL("DELETE FROM stair_history")
                    db.writableDatabase.execSQL("DELETE FROM tile_history")
                    db.writableDatabase.execSQL("DELETE FROM brick_history")
                    db.writableDatabase.execSQL("DELETE FROM plaster_history")
                    db.writableDatabase.execSQL("DELETE FROM paint_history")
                    concreteDb.deleteAll()
                    loadStairs(); loadTiles(); loadConcrete()
                    loadBrick();  loadPlaster(); loadPaint()
                    Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // HELPER — builds a simple ListView tab with long-press delete
    // ═════════════════════════════════════════════════════════════════════════
    private fun setupListTab(
        listViewId: Int,
        emptyViewId: Int,
        items: List<String>,
        onDelete: (Int) -> Unit
    ) {
        val lv    = findViewById<ListView>(listViewId)
        val empty = findViewById<TextView>(emptyViewId)

        if (items.isEmpty()) {
            lv.visibility    = View.GONE
            empty.visibility = View.VISIBLE
            return
        }
        lv.visibility    = View.VISIBLE
        empty.visibility = View.GONE

        lv.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)

        lv.setOnItemLongClickListener { _, _, pos, _ ->
            AlertDialog.Builder(this)
                .setTitle("Delete Record")
                .setMessage("Delete this record?")
                .setPositiveButton("Delete") { _, _ -> onDelete(pos) }
                .setNegativeButton("Cancel", null)
                .show()
            true
        }
    }
    // ═════════════════════════════════════════════════════════════════════════
    // STAIRS
    // ═════════════════════════════════════════════════════════════════════════
    private fun loadStairs() {
        val list = db.getAllStairs()
        val items = list.map { r ->
            """
            🪜 ${r["type"]}  |  ${r["date"]}
            H=${r["height"]} m  |  N=${r["steps"]}
            R=${String.format("%.3f", r["riser"]?.toDoubleOrNull() ?: 0.0)} m  |  T=${String.format("%.3f", r["tread"]?.toDoubleOrNull() ?: 0.0)} m
            Blondel=${String.format("%.3f", r["blondel"]?.toDoubleOrNull() ?: 0.0)} m  |  L=${String.format("%.2f", r["length"]?.toDoubleOrNull() ?: 0.0)} m
            Status: ${r["status"]}
            """.trimIndent()
        }
        setupListTab(R.id.lvStairs, R.id.tvStairEmpty, items) { pos ->
            val id = list[pos]["id"]?.toIntOrNull() ?: return@setupListTab
            db.deleteStair(id); loadStairs()
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // TILES
    // ═════════════════════════════════════════════════════════════════════════
    private fun loadTiles() {
        val list = db.getAllTileHistory()
        val items = list.map { r ->
            """
            🧱 ${r["tile_type"]}  |  ${r["date"]}
            📐 Floor=${String.format("%.2f", r["floor_area"]?.toDoubleOrNull() ?: 0.0)} m²
            🧩 Tile=${r["tile_l_cm"]}×${r["tile_w_cm"]} cm
            🔧 Install=${r["install_type"]}
            🔢 Base=${r["base_tiles"]} pcs  |  +${r["waste_pct"]}% waste
            ✅ Total=${r["total_tiles"]} pcs
            """.trimIndent()
        }
        setupListTab(R.id.lvTiles, R.id.tvTileEmpty, items) { pos ->
            val id = list[pos]["id"]?.toIntOrNull() ?: return@setupListTab
            db.deleteTileHistory(id); loadTiles()
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // CONCRETE
    // ═════════════════════════════════════════════════════════════════════════
    private fun loadConcrete() {
        val list = concreteDb.getAll()
        val items = list.map { item ->
            """
            🏗️ ${item.element}  |  ${item.grade}  |  ${item.date}
            📦 Volume  : ${"%.3f".format(item.volume)} m³
            🧱 Cement  : ${item.cementBags} bags × 50 kg
            🪨 Sand    : ${"%.3f".format(item.sandM3)} m³
            🪨 Gravel  : ${"%.3f".format(item.gravelM3)} m³
            ⚙️ Steel   : ${item.steelKg.toInt()} kg
            📐 Mix     : ${item.mixRatio}
            """.trimIndent()
        }
        setupListTab(R.id.lvConcrete, R.id.tvConcreteEmpty, items) { pos ->
            val id = list[pos].id
            concreteDb.writableDatabase.delete("history", "id=?", arrayOf(id.toString()))
            loadConcrete()
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // BRICK
    // ═════════════════════════════════════════════════════════════════════════
    private fun loadBrick() {
        val list = db.getAllBrickHistory()
        val items = list.map { r ->
            """
            🧱 ${r["date"]}
            Wall: ${r["wall_length"]}×${r["wall_height"]}×${r["wall_thick"]} m
            Brick: ${r["brick_l"]}×${r["brick_h"]}×${r["brick_w"]} cm
            Mortar: ${r["mortar_ratio"]}
            🔢 Bricks: ${r["bricks"]}  |  Cement: ${r["cement_bags"]} bags
            🪨 Sand: ${r["sand_m3"]} m³  |  Area: ${r["wall_area"]} m²
            Status: ${r["status"]}
            """.trimIndent()
        }
        setupListTab(R.id.lvBrick, R.id.tvBrickEmpty, items) { pos ->
            val id = list[pos]["id"]?.toIntOrNull() ?: return@setupListTab
            db.deleteBrickHistory(id); loadBrick()
        }
    }
    // ═════════════════════════════════════════════════════════════════════════
    // PLASTER
    // ═════════════════════════════════════════════════════════════════════════
    private fun loadPlaster() {
        val list = db.getAllPlasterHistory()
        val items = list.map { r ->
            """
            🪣 ${r["surface"]}  |  ${r["date"]}
            📐 Area: ${r["area"]} m²  |  Thickness: ${r["thickness"]} mm
            Ratio: ${r["ratio"]}  |  Coats: ${r["coats"]}
            🧱 Cement: ${r["cement_bags"]} bags
            🪨 Sand: ${r["sand_m3"]} m³  |  💧 Water: ${r["water_l"]} L
            📦 Volume: ${r["volume_m3"]} m³
            """.trimIndent()
        }
        setupListTab(R.id.lvPlaster, R.id.tvPlasterEmpty, items) { pos ->
            val id = list[pos]["id"]?.toIntOrNull() ?: return@setupListTab
            db.deletePlasterHistory(id); loadPlaster()
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PAINT
    // ═════════════════════════════════════════════════════════════════════════
    private fun loadPaint() {
        val list = db.getAllPaintHistory()
        val items = list.map { r ->
            """
            🎨 ${r["paint_type"]}  |  ${r["date"]}
            Surface: ${r["surface"]}  |  Area: ${r["area"]} m²
            Coats: ${r["coats"]}  |  Paint: ${r["paint_liters"]} L
            🥫 Cans: ${r["cans_needed"]}  |  Primer: ${r["primer_l"]} L
            """.trimIndent()
        }
        setupListTab(R.id.lvPaint, R.id.tvPaintEmpty, items) { pos ->
            val id = list[pos]["id"]?.toIntOrNull() ?: return@setupListTab
            db.deletePaintHistory(id); loadPaint()
        }
    }
}