package com.example.constructioncalculator

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "ConstructionDB", null, 13) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                firstName TEXT,
                lastName TEXT,
                email TEXT UNIQUE,
                password TEXT,
                otp TEXT,
                profile_image TEXT,
                role TEXT DEFAULT 'user'
            )
        """)
        db.execSQL("""
            CREATE TABLE feedback (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT,
                rating REAL,
                note TEXT
            )
        """)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS otp_table (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT,
                otp TEXT,
                created_at INTEGER
            )
        """)
        db.execSQL("""
            CREATE TABLE results (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                bricks INTEGER,
                cement REAL,
                sand REAL,
                total REAL
            )
        """)
        db.execSQL("""
            CREATE TABLE plaster (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                area REAL,
                thickness REAL,
                ratio TEXT,
                cement REAL,
                sand REAL,
                water REAL,
                volume REAL
            )
        """)
        db.execSQL("""
            CREATE TABLE paint_results (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                type TEXT,
                area REAL,
                coats INTEGER,
                paint REAL,
                date TEXT
            )
        """)
        db.execSQL("""
            CREATE TABLE tiles (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                type TEXT,
                area REAL,
                count REAL,
                date TEXT
            )
        """)
        db.execSQL("""
            CREATE TABLE draw_plans (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                data TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
        """)
        db.execSQL("""
    CREATE TABLE IF NOT EXISTS construction_notes (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        title TEXT,
        subtitle TEXT,
        body TEXT,
        color INTEGER,
        date TEXT,
        images TEXT DEFAULT '',
        files TEXT DEFAULT '',
        links TEXT DEFAULT ''
    )
""")
        db.execSQL("""
            CREATE TABLE business (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                email TEXT,
                phone TEXT,
                address TEXT,
                website TEXT,
                logo TEXT
            )
        """)

        // جدول العملاء
        db.execSQL("""
            CREATE TABLE clients (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                email TEXT,
                phone TEXT,
                address1 TEXT,
                address2 TEXT,
                ship_address1 TEXT,
                ship_address2 TEXT,
                notes TEXT
            )
        """)

        // جدول الفواتير
        db.execSQL("""
            CREATE TABLE invoices (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                invoice_number TEXT,
                created_date TEXT,
                due_date TEXT,
                business_id INTEGER,
                client_id INTEGER,
                discount REAL DEFAULT 0,
                tax_name TEXT,
                tax_percent REAL DEFAULT 0,
                shipping REAL DEFAULT 0,
                payment_method TEXT,
                terms TEXT,
                notes TEXT,
                signature TEXT,
                FOREIGN KEY(business_id) REFERENCES business(id),
                FOREIGN KEY(client_id) REFERENCES clients(id)
            )
        """)

        // جدول عناصر الفاتورة
        db.execSQL("""
            CREATE TABLE invoice_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                invoice_id INTEGER,
                name TEXT,
                quantity REAL,
                price REAL,
                discount REAL DEFAULT 0,
                tax REAL DEFAULT 0,
                description TEXT,
                FOREIGN KEY(invoice_id) REFERENCES invoices(id)
            )
        """)
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 4) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS draw_plans (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    data TEXT NOT NULL,
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL
                )
            """)
        }
        if (oldVersion < 5) {
            db.execSQL("""
        CREATE TABLE IF NOT EXISTS construction_notes (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            title TEXT,
            subtitle TEXT,
            body TEXT,
            color INTEGER,
            date TEXT
        )
    """)
            db.execSQL("""
    CREATE TABLE history (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        type TEXT NOT NULL,
        height REAL NOT NULL,
        steps INTEGER NOT NULL,
        length REAL NOT NULL,
        area REAL NOT NULL,
        date INTEGER NOT NULL
        )
         """)
            db.execSQL("""
            CREATE TABLE stair_history (
                id      INTEGER PRIMARY KEY AUTOINCREMENT,
                type    TEXT,
                height  REAL,
                steps   INTEGER,
                riser   REAL,
                tread   REAL,
                blondel REAL,
                length  REAL,
                area    REAL,
                status  TEXT,
                date    TEXT
            )
        """)
            db.execSQL("""
            CREATE TABLE tile_history (
                id           INTEGER PRIMARY KEY AUTOINCREMENT,
                tile_type    TEXT,
                floor_area   REAL,
                tile_l_cm    REAL,
                tile_w_cm    REAL,
                base_tiles   INTEGER,
                total_tiles  INTEGER,
                waste_pct    REAL,
                install_type TEXT,
                date         TEXT
            )
        """)
            // ── تاريخ الطابوق (Brick) ──
            db.execSQL("""
            CREATE TABLE brick_history (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                wall_length REAL,
                wall_height REAL,
                wall_thick  REAL,
                brick_l     REAL,
                brick_h     REAL,
                brick_w     REAL,
                mortar_ratio TEXT,
                bricks      INTEGER,
                cement_bags REAL,
                sand_m3     REAL,
                wall_area   REAL,
                wall_volume REAL,
                status      TEXT,
                date        TEXT
            )
        """)
            // ── تاريخ اللياسة (Plaster) ──
            db.execSQL("""
            CREATE TABLE plaster_history (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                surface     TEXT,
                area        REAL,
                thickness   REAL,
                ratio       TEXT,
                cement_bags REAL,
                sand_m3     REAL,
                water_l     REAL,
                volume_m3   REAL,
                coats       INTEGER,
                date        TEXT
            )
        """)

            // ── تاريخ الطلاء (Paint) ──
            db.execSQL("""
            CREATE TABLE paint_history (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                paint_type  TEXT,
                surface     TEXT,
                area        REAL,
                coats       INTEGER,
                paint_liters REAL,
                cans_needed INTEGER,
                primer_l    REAL,
                date        TEXT
            )
        """)

            // ── جدول history القديم (للتوافق مع الكود الموجود) ──
            db.execSQL("""
            CREATE TABLE IF NOT EXISTS history (
                id     INTEGER PRIMARY KEY AUTOINCREMENT,
                type   TEXT    NOT NULL,
                height REAL    NOT NULL,
                steps  INTEGER NOT NULL,
                length REAL    NOT NULL,
                area   REAL    NOT NULL,
                date   INTEGER NOT NULL
            )
        """)
        }
        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE users ADD COLUMN profile_image TEXT")
        }
    // نهاية onUpgrade
        if (oldVersion < 7) {
            db.execSQL("CREATE TABLE IF NOT EXISTS business (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, email TEXT, phone TEXT, address TEXT, website TEXT, logo TEXT)")
            db.execSQL("CREATE TABLE IF NOT EXISTS clients (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, email TEXT, phone TEXT, address1 TEXT, address2 TEXT, ship_address1 TEXT, ship_address2 TEXT, notes TEXT)")
            db.execSQL("CREATE TABLE IF NOT EXISTS invoices (id INTEGER PRIMARY KEY AUTOINCREMENT, invoice_number TEXT, created_date TEXT, due_date TEXT, business_id INTEGER, client_id INTEGER, discount REAL DEFAULT 0, tax_name TEXT, tax_percent REAL DEFAULT 0, shipping REAL DEFAULT 0, payment_method TEXT, terms TEXT, notes TEXT, signature TEXT)")
            db.execSQL("CREATE TABLE IF NOT EXISTS invoice_items (id INTEGER PRIMARY KEY AUTOINCREMENT, invoice_id INTEGER, name TEXT, quantity REAL, price REAL, discount REAL DEFAULT 0, tax REAL DEFAULT 0, description TEXT)")
        }
        if (oldVersion < 9) {
            db.execSQL("ALTER TABLE construction_notes ADD COLUMN images TEXT DEFAULT ''")
            db.execSQL("ALTER TABLE construction_notes ADD COLUMN files TEXT DEFAULT ''")
            db.execSQL("ALTER TABLE construction_notes ADD COLUMN links TEXT DEFAULT ''")
        }
        if (oldVersion < 10) {
            db.execSQL("""
        CREATE TABLE IF NOT EXISTS otp_table (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            email TEXT,
            otp TEXT,
            created_at INTEGER
        )
    """)
        }
        if (oldVersion < 11) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS stair_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT, type TEXT,
                    height REAL, steps INTEGER, riser REAL, tread REAL,
                    blondel REAL, length REAL, area REAL, status TEXT, date TEXT
                )
            """)

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS tile_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT, tile_type TEXT,
                    floor_area REAL, tile_l_cm REAL, tile_w_cm REAL,
                    base_tiles INTEGER, total_tiles INTEGER, waste_pct REAL,
                    install_type TEXT, date TEXT
                )
            """)
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS brick_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    wall_length REAL, wall_height REAL, wall_thick REAL,
                    brick_l REAL, brick_h REAL, brick_w REAL,
                    mortar_ratio TEXT, bricks INTEGER, cement_bags REAL,
                    sand_m3 REAL, wall_area REAL, wall_volume REAL,
                    status TEXT, date TEXT
                )
            """)
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS plaster_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT, surface TEXT,
                    area REAL, thickness REAL, ratio TEXT, cement_bags REAL,
                    sand_m3 REAL, water_l REAL, volume_m3 REAL,
                    coats INTEGER, date TEXT
                )
            """)
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS paint_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT, paint_type TEXT,
                    surface TEXT, area REAL, coats INTEGER,
                    paint_liters REAL, cans_needed INTEGER,
                    primer_l REAL, date TEXT
                )
            """)
        }
        if (oldVersion < 12) {
            db.execSQL("ALTER TABLE users ADD COLUMN role TEXT DEFAULT 'user'")
        }
        if (oldVersion < 13) {
            db.execSQL("""
        INSERT OR IGNORE INTO users (firstName, lastName, email, password, role)
        VALUES ('Admin', 'System', 'admin@gmail.com', 'ayouch12345', 'admin')
    """)
        }
    }

    // ================= USERS =================
    fun insertUser(first: String, last: String, email: String, password: String): Boolean {
        val values = ContentValues().apply {
            put("firstName", first)
            put("lastName", last)
            put("email", email)
            put("password", password)
            put("otp", (1000..9999).random().toString())
            put("role","user")
        }
        return writableDatabase.insert("users", null, values) != -1L
    }

    fun checkUser(email: String, password: String): Boolean {
        val cursor = readableDatabase.rawQuery(
            "SELECT id FROM users WHERE email=? AND password=?",
            arrayOf(email, password)
        )
        val ok = cursor.moveToFirst()
        cursor.close()
        return ok
    }
    fun getUserName(email: String): String {
        val cursor = readableDatabase.rawQuery(
            "SELECT firstName FROM users WHERE email=?",
            arrayOf(email)
        )
        var name = ""
        if (cursor.moveToFirst()) name = cursor.getString(0)
        cursor.close()
        return name
    }

    fun updatePassword(email: String, newPassword: String): Boolean {
        val values = ContentValues()
        values.put("password", newPassword)
        return writableDatabase.update("users", values, "email=?", arrayOf(email)) > 0
    }

    fun getUser(email: String): User? {
        val cursor = readableDatabase.rawQuery(
            "SELECT firstName, lastName, email, profile_image FROM users WHERE email=?",
            arrayOf(email)
        )
        var user: User? = null
        if (cursor.moveToFirst()) {
            user = User(
                firstName    = cursor.getString(0),
                lastName     = cursor.getString(1),
                email        = cursor.getString(2),
                profileImage = cursor.getString(3)
            )
        }
        cursor.close()
        return user
    }

    fun updateProfileImage(email: String, imageUri: String): Int {
        val cv = ContentValues()
        cv.put("profile_image", imageUri)
        return writableDatabase.update("users", cv, "email=?", arrayOf(email))
    }

    // ================= FEEDBACK =================
    fun insertFeedback(email: String, rating: Float, note: String) {
        val values = ContentValues().apply {
            put("email", email)
            put("rating", rating)
            put("note", note)
        }
        writableDatabase.insert("feedback", null, values)
    }

    // ================= RESULTS =================
    fun insertResult(bricks: Int, cement: Double, sand: Double, total: Double): Boolean {
        val v = ContentValues().apply {
            put("bricks", bricks); put("cement", cement)
            put("sand", sand);     put("total", total)
        }
        return writableDatabase.insert("results", null, v) != -1L
    }
    fun insertBrickHistory(
        wallLength: Double, wallHeight: Double, wallThick: Double,
        brickL: Double,     brickH: Double,     brickW: Double,
        mortarRatio: String,
        bricks: Int,        cementBags: Double, sandM3: Double,
        wallArea: Double,   wallVolume: Double,
        status: String,     date: String
    ): Long {
        val cv = ContentValues().apply {
            put("wall_length",  wallLength)
            put("wall_height",  wallHeight)
            put("wall_thick",   wallThick)
            put("brick_l",      brickL)
            put("brick_h",      brickH)
            put("brick_w",      brickW)
            put("mortar_ratio", mortarRatio)
            put("bricks",       bricks)
            put("cement_bags",  cementBags)
            put("sand_m3",      sandM3)
            put("wall_area",    wallArea)
            put("wall_volume",  wallVolume)
            put("status",       status)
            put("date",         date)
        }
        return writableDatabase.insert("brick_history", null, cv)
    }

    fun getAllBrickHistory(): List<Map<String, String>> {
        val list = mutableListOf<Map<String, String>>()
        val c = readableDatabase.rawQuery(
            "SELECT * FROM brick_history ORDER BY id DESC", null)
        c.use {
            while (it.moveToNext()) {
                list.add(mapOf(
                    "id"          to it.getString(it.getColumnIndexOrThrow("id")),
                    "wall_length" to it.getString(it.getColumnIndexOrThrow("wall_length")),
                    "wall_height" to it.getString(it.getColumnIndexOrThrow("wall_height")),
                    "wall_thick"  to it.getString(it.getColumnIndexOrThrow("wall_thick")),
                    "brick_l"     to it.getString(it.getColumnIndexOrThrow("brick_l")),
                    "brick_h"     to it.getString(it.getColumnIndexOrThrow("brick_h")),
                    "brick_w"     to it.getString(it.getColumnIndexOrThrow("brick_w")),
                    "mortar_ratio" to it.getString(it.getColumnIndexOrThrow("mortar_ratio")),
                    "bricks"      to it.getString(it.getColumnIndexOrThrow("bricks")),
                    "cement_bags" to it.getString(it.getColumnIndexOrThrow("cement_bags")),
                    "sand_m3"     to it.getString(it.getColumnIndexOrThrow("sand_m3")),
                    "wall_area"   to it.getString(it.getColumnIndexOrThrow("wall_area")),
                    "wall_volume" to it.getString(it.getColumnIndexOrThrow("wall_volume")),
                    "status"      to it.getString(it.getColumnIndexOrThrow("status")),
                    "date"        to it.getString(it.getColumnIndexOrThrow("date"))
                ))
            }
        }
        return list
    }

    fun deleteBrickHistory(id: Int) {
        writableDatabase.delete("brick_history", "id=?", arrayOf(id.toString()))
    }
    fun clearBrickHistory() {
        writableDatabase.execSQL("DELETE FROM brick_history")
    }

    // ================= PLASTER =================
    fun insertPlaster(
        area: Double, thickness: Double, ratio: String,
        cement: Double, sand: Double, water: Double, volume: Double
    ) {
        val v = ContentValues().apply {
            put("area", area); put("thickness", thickness); put("ratio", ratio)
            put("cement", cement); put("sand", sand); put("water", water); put("volume", volume)
        }
        writableDatabase.insert("plaster", null, v)
    }

    /**
     * حفظ حساب اللياسة كاملاً في plaster_history
     */
    fun insertPlasterHistory(
        surface: String,
        area: Double,     thickness: Double, ratio: String,
        cementBags: Double, sandM3: Double,  waterL: Double,
        volumeM3: Double, coats: Int,        date: String
    ): Long {
        val cv = ContentValues().apply {
            put("surface",     surface)
            put("area",        area)
            put("thickness",   thickness)
            put("ratio",       ratio)
            put("cement_bags", cementBags)
            put("sand_m3",     sandM3)
            put("water_l",     waterL)
            put("volume_m3",   volumeM3)
            put("coats",       coats)
            put("date",        date)
        }
        return writableDatabase.insert("plaster_history", null, cv)
    }

    fun getAllPlasterHistory(): List<Map<String, String>> {
        val list = mutableListOf<Map<String, String>>()
        val c = readableDatabase.rawQuery(
            "SELECT * FROM plaster_history ORDER BY id DESC", null)
        c.use {
            while (it.moveToNext()) {
                list.add(mapOf(
                    "id"          to it.getString(it.getColumnIndexOrThrow("id")),
                    "surface"     to it.getString(it.getColumnIndexOrThrow("surface")),
                    "area"        to it.getString(it.getColumnIndexOrThrow("area")),
                    "thickness"   to it.getString(it.getColumnIndexOrThrow("thickness")),
                    "ratio"       to it.getString(it.getColumnIndexOrThrow("ratio")),
                    "cement_bags" to it.getString(it.getColumnIndexOrThrow("cement_bags")),
                    "sand_m3"     to it.getString(it.getColumnIndexOrThrow("sand_m3")),
                    "water_l"     to it.getString(it.getColumnIndexOrThrow("water_l")),
                    "volume_m3"   to it.getString(it.getColumnIndexOrThrow("volume_m3")),
                    "coats"       to it.getString(it.getColumnIndexOrThrow("coats")),
                    "date"        to it.getString(it.getColumnIndexOrThrow("date"))
                ))
            }
        }
        return list
    }

    fun deletePlasterHistory(id: Int) {
        writableDatabase.delete("plaster_history", "id=?", arrayOf(id.toString()))
    }
    fun clearPlasterHistory() {
        writableDatabase.execSQL("DELETE FROM plaster_history")
    }

    // ================= PAINT =================
    fun insertPaint(type: String, area: Double, coats: Int, paint: Double, date: String) {
        val v = ContentValues().apply {
            put("type", type); put("area", area)
            put("coats", coats); put("paint", paint); put("date", date)
        }
        writableDatabase.insert("paint_results", null, v)
    }

    /**
     * حفظ حساب الطلاء كاملاً في paint_history
     */
    fun insertPaintHistory(
        paintType: String, surface: String,
        area: Double,      coats: Int,
        paintLiters: Double, cansNeeded: Int,
        primerL: Double,   date: String
    ): Long {
        val cv = ContentValues().apply {
            put("paint_type",   paintType)
            put("surface",      surface)
            put("area",         area)
            put("coats",        coats)
            put("paint_liters", paintLiters)
            put("cans_needed",  cansNeeded)
            put("primer_l",     primerL)
            put("date",         date)
        }
        return writableDatabase.insert("paint_history", null, cv)
    }

    fun getAllPaintHistory(): List<Map<String, String>> {
        val list = mutableListOf<Map<String, String>>()
        val c = readableDatabase.rawQuery(
            "SELECT * FROM paint_history ORDER BY id DESC", null)
        c.use {
            while (it.moveToNext()) {
                list.add(mapOf(
                    "id"           to it.getString(it.getColumnIndexOrThrow("id")),
                    "paint_type"   to it.getString(it.getColumnIndexOrThrow("paint_type")),
                    "surface"      to it.getString(it.getColumnIndexOrThrow("surface")),
                    "area"         to it.getString(it.getColumnIndexOrThrow("area")),
                    "coats"        to it.getString(it.getColumnIndexOrThrow("coats")),
                    "paint_liters" to it.getString(it.getColumnIndexOrThrow("paint_liters")),
                    "cans_needed"  to it.getString(it.getColumnIndexOrThrow("cans_needed")),
                    "primer_l"     to it.getString(it.getColumnIndexOrThrow("primer_l")),
                    "date"         to it.getString(it.getColumnIndexOrThrow("date"))
                ))
            }
        }
        return list
    }

    fun deletePaintHistory(id: Int) {
        writableDatabase.delete("paint_history", "id=?", arrayOf(id.toString()))
    }
    fun clearPaintHistory() {
        writableDatabase.execSQL("DELETE FROM paint_history")
    }
    // ================= TILES =================
    fun insertTile(type: String, area: Double, count: Double, date: String) {
        val v = ContentValues().apply {
            put("type", type); put("area", area); put("count", count); put("date", date)
        }
        writableDatabase.insert("tiles", null, v)
    }

    /**
     * حفظ حساب البلاط كاملاً في tile_history
     */
    fun insertTileHistory(
        tileType: String,  floorArea: Double,
        tileLcm: Double,   tileWcm: Double,
        baseTiles: Int,    totalTiles: Int,
        wastePct: Double,  installType: String,
        date: String
    ): Long {
        val cv = ContentValues().apply {
            put("tile_type",    tileType)
            put("floor_area",   floorArea)
            put("tile_l_cm",    tileLcm)
            put("tile_w_cm",    tileWcm)
            put("base_tiles",   baseTiles)
            put("total_tiles",  totalTiles)
            put("waste_pct",    wastePct)
            put("install_type", installType)
            put("date",         date)
        }
        return writableDatabase.insert("tile_history", null, cv)
    }

    fun getAllTileHistory(): List<Map<String, String>> {
        val list = mutableListOf<Map<String, String>>()
        val c = readableDatabase.rawQuery(
            "SELECT * FROM tile_history ORDER BY id DESC", null)
        c.use {
            while (it.moveToNext()) {
                list.add(mapOf(
                    "id"           to it.getString(it.getColumnIndexOrThrow("id")),
                    "tile_type"    to it.getString(it.getColumnIndexOrThrow("tile_type")),
                    "floor_area"   to it.getString(it.getColumnIndexOrThrow("floor_area")),
                    "tile_l_cm"    to it.getString(it.getColumnIndexOrThrow("tile_l_cm")),
                    "tile_w_cm"    to it.getString(it.getColumnIndexOrThrow("tile_w_cm")),
                    "base_tiles"   to it.getString(it.getColumnIndexOrThrow("base_tiles")),
                    "total_tiles"  to it.getString(it.getColumnIndexOrThrow("total_tiles")),
                    "waste_pct"    to it.getString(it.getColumnIndexOrThrow("waste_pct")),
                    "install_type" to it.getString(it.getColumnIndexOrThrow("install_type")),
                    "date"         to it.getString(it.getColumnIndexOrThrow("date"))
                ))
            }
        }
        return list
    }

    fun deleteTileHistory(id: Int) {
        writableDatabase.delete("tile_history", "id=?", arrayOf(id.toString()))
    }
    // ═══════════════════════════════════════════════════════
    //  STAIR (الدرج) — stair_history
    // ═══════════════════════════════════════════════════════
    fun insertStair(
        type: String,   height: Double, steps: Int,
        riser: Double,  tread: Double,  blondel: Double,
        length: Double, area: Double,   status: String, date: String
    ): Long {
        val cv = ContentValues().apply {
            put("type",    type);    put("height",  height)
            put("steps",   steps);   put("riser",   riser)
            put("tread",   tread);   put("blondel", blondel)
            put("length",  length);  put("area",    area)
            put("status",  status);  put("date",    date)
        }
        return writableDatabase.insert("stair_history", null, cv)
    }

    fun getAllStairs(): List<Map<String, String>> {
        val list = mutableListOf<Map<String, String>>()
        val c = readableDatabase.rawQuery(
            "SELECT * FROM stair_history ORDER BY id DESC", null)
        c.use {
            while (it.moveToNext()) {
                list.add(mapOf(
                    "id"      to it.getString(it.getColumnIndexOrThrow("id")),
                    "type"    to it.getString(it.getColumnIndexOrThrow("type")),
                    "height"  to it.getString(it.getColumnIndexOrThrow("height")),
                    "steps"   to it.getString(it.getColumnIndexOrThrow("steps")),
                    "riser"   to it.getString(it.getColumnIndexOrThrow("riser")),
                    "tread"   to it.getString(it.getColumnIndexOrThrow("tread")),
                    "blondel" to it.getString(it.getColumnIndexOrThrow("blondel")),
                    "length"  to it.getString(it.getColumnIndexOrThrow("length")),
                    "area"    to it.getString(it.getColumnIndexOrThrow("area")),
                    "status"  to it.getString(it.getColumnIndexOrThrow("status")),
                    "date"    to it.getString(it.getColumnIndexOrThrow("date"))
                ))
            }
        }
        return list
    }

    fun deleteStair(id: Int) {
        writableDatabase.delete("stair_history", "id=?", arrayOf(id.toString()))
    }


    // ================= OTP =================
    fun generateOtp(email: String): String {
        val otp = (1000..9999).random().toString()
        val cv = ContentValues().apply {
            put("email", email)
            put("otp", otp)
            put("created_at", System.currentTimeMillis())
        }
        writableDatabase.insert("otp_table", null, cv)
        return otp
    }
    fun checkOtp(email: String, otp: String): Boolean {
        val cursor = readableDatabase.rawQuery(
            """
        SELECT otp, created_at FROM otp_table 
        WHERE email=? 
        ORDER BY created_at DESC 
        LIMIT 1
        """,
            arrayOf(email)
        )

        var isValid = false

        if (cursor.moveToFirst()) {
            val dbOtp = cursor.getString(0)
            val createdAt = cursor.getLong(1)

            val currentTime = System.currentTimeMillis()
            val diff = currentTime - createdAt

            // صالح لمدة 5 دقائق فقط
            if (dbOtp == otp && diff <= 300000) {
                isValid = true
            }
        }

        cursor.close()
        return isValid
    }

    // ================= FLOOR PLANS =================
    fun insertPlan(name: String, data: String): Long {
        val values = ContentValues().apply {
            put("name", name)
            put("data", data)
            put("created_at", System.currentTimeMillis())
            put("updated_at", System.currentTimeMillis())
        }
        return writableDatabase.insert("draw_plans", null, values)
    }

    fun updatePlan(id: Long, name: String, data: String): Boolean {
        val values = ContentValues().apply {
            put("name", name)
            put("data", data)
            put("updated_at", System.currentTimeMillis())
        }
        return writableDatabase.update("draw_plans", values, "id=?", arrayOf(id.toString())) > 0
    }

    fun getPlanById(id: Long): SavedPlan? {
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM draw_plans WHERE id=?",
            arrayOf(id.toString())
        )
        return if (cursor.moveToFirst()) {
            val plan = SavedPlan(
                id   = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                data = cursor.getString(cursor.getColumnIndexOrThrow("data"))
            )
            cursor.close()
            plan
        } else {
            cursor.close()
            null
        }
    }

    fun getAllPlans(): List<SavedPlan> {
        val list = mutableListOf<SavedPlan>()
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM draw_plans ORDER BY updated_at DESC",
            null
        )
        while (cursor.moveToNext()) {
            list.add(SavedPlan(
                id   = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                data = cursor.getString(cursor.getColumnIndexOrThrow("data"))
            ))
        }
        cursor.close()
        return list
    }

    fun deletePlan(id: Long): Boolean {
        return writableDatabase.delete("draw_plans", "id=?", arrayOf(id.toString())) > 0
    }
    // ================= CONSTRUCTION NOTES =================

    fun insertNote(note: Note): Long {
        val values = ContentValues().apply {
            put("title", note.title)
            put("subtitle", note.subtitle)
            put("body", note.body)
            put("color", note.color)
            put("date", note.date)
            put("images", note.images)
            put("files", note.files)
            put("links", note.links)
        }
        return writableDatabase.insert("construction_notes", null, values)
    }

    fun updateNote(note: Note): Boolean {
        val values = ContentValues().apply {
            put("title", note.title)
            put("subtitle", note.subtitle)
            put("body", note.body)
            put("color", note.color)
            put("date", note.date)
            put("images", note.images)
            put("files", note.files)
            put("links", note.links)
        }
        return writableDatabase.update(
            "construction_notes", values, "id=?", arrayOf(note.id.toString())
        ) > 0
    }

    fun deleteNote(id: Long): Boolean {
        return writableDatabase.delete(
            "construction_notes", "id=?", arrayOf(id.toString())
        ) > 0
    }

    fun getAllNotes(): MutableList<Note> {
        val notes = mutableListOf<Note>()
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM construction_notes ORDER BY id DESC", null
        )
        if (cursor.moveToFirst()) {
            do {
                notes.add(
                    Note(
                        id       = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        title    = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        subtitle = cursor.getString(cursor.getColumnIndexOrThrow("subtitle")),
                        body     = cursor.getString(cursor.getColumnIndexOrThrow("body")),
                        color    = cursor.getInt(cursor.getColumnIndexOrThrow("color")),
                        date     = cursor.getString(cursor.getColumnIndexOrThrow("date")),
                        images   = cursor.getString(cursor.getColumnIndexOrThrow("images")) ?: "",
                        files    = cursor.getString(cursor.getColumnIndexOrThrow("files")) ?: "",
                        links    = cursor.getString(cursor.getColumnIndexOrThrow("links")) ?: ""
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return notes
    }

    fun searchNotes(query: String): MutableList<Note> {
        val notes = mutableListOf<Note>()
        val cursor = readableDatabase.rawQuery(
            """SELECT * FROM construction_notes 
           WHERE title LIKE ? OR subtitle LIKE ? OR body LIKE ?
           ORDER BY id DESC""",
            arrayOf("%$query%", "%$query%", "%$query%")
        )
        if (cursor.moveToFirst()) {
            do {
                notes.add(
                    Note(
                        id       = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        title    = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        subtitle = cursor.getString(cursor.getColumnIndexOrThrow("subtitle")),
                        body     = cursor.getString(cursor.getColumnIndexOrThrow("body")),
                        color    = cursor.getInt(cursor.getColumnIndexOrThrow("color")),
                        date     = cursor.getString(cursor.getColumnIndexOrThrow("date")),
                        images   = cursor.getString(cursor.getColumnIndexOrThrow("images")) ?: "",
                        files    = cursor.getString(cursor.getColumnIndexOrThrow("files")) ?: "",
                        links    = cursor.getString(cursor.getColumnIndexOrThrow("links")) ?: ""
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return notes
    }

    // ==================== BUSINESS ====================

    fun saveBusiness(name: String, email: String, phone: String,
                     address: String, website: String, logo: String) {
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT id FROM business LIMIT 1", null)
        if (cursor.moveToFirst()) {
            db.execSQL("""
                UPDATE business SET name=?, email=?, phone=?,
                address=?, website=?, logo=? WHERE id=1
            """, arrayOf(name, email, phone, address, website, logo))
        } else {
            db.execSQL("""
                INSERT INTO business (name,email,phone,address,website,logo)
                VALUES (?,?,?,?,?,?)
            """, arrayOf(name, email, phone, address, website, logo))
        }
        cursor.close()
    }

    fun getBusiness(): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM business LIMIT 1", null)
        if (cursor.moveToFirst()) {
            result["id"]      = cursor.getInt(0).toString()
            result["name"]    = cursor.getString(1) ?: ""
            result["email"]   = cursor.getString(2) ?: ""
            result["phone"]   = cursor.getString(3) ?: ""
            result["address"] = cursor.getString(4) ?: ""
            result["website"] = cursor.getString(5) ?: ""
            result["logo"]    = cursor.getString(6) ?: ""
        }
        cursor.close()
        return result
    }

    // ==================== CLIENTS ====================

    fun saveClient(name: String, email: String, phone: String,
                   address1: String, address2: String,
                   shipAddress1: String, shipAddress2: String,
                   notes: String): Long {
        var id = -1L
        writableDatabase.execSQL("""
            INSERT INTO clients 
            (name,email,phone,address1,address2,
             ship_address1,ship_address2,notes)
            VALUES (?,?,?,?,?,?,?,?)
        """, arrayOf(name, email, phone, address1, address2,
            shipAddress1, shipAddress2, notes))
        val cursor = readableDatabase.rawQuery(
            "SELECT last_insert_rowid()", null)
        if (cursor.moveToFirst()) id = cursor.getLong(0)
        cursor.close()
        return id
    }

    fun getAllClients(): List<Map<String, String>> {
        val list = mutableListOf<Map<String, String>>()
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM clients ORDER BY id DESC", null)
        while (cursor.moveToNext()) {
            list.add(mapOf(
                "id"           to cursor.getInt(0).toString(),
                "name"         to (cursor.getString(1) ?: ""),
                "email"        to (cursor.getString(2) ?: ""),
                "phone"        to (cursor.getString(3) ?: ""),
                "address1"     to (cursor.getString(4) ?: ""),
                "address2"     to (cursor.getString(5) ?: ""),
                "ship_address1" to (cursor.getString(6) ?: ""),
                "ship_address2" to (cursor.getString(7) ?: ""),
                "notes"        to (cursor.getString(8) ?: "")
            ))
        }
        cursor.close()
        return list
    }

    // ==================== INVOICES ====================

    fun saveInvoice(
        invoiceNumber: String, createdDate: String, dueDate: String,
        businessId: Int, clientId: Int, discount: Double,
        taxName: String, taxPercent: Double, shipping: Double,
        paymentMethod: String, terms: String, notes: String,
        signature: String
    ): Long {
        writableDatabase.execSQL("""
            INSERT INTO invoices 
            (invoice_number, created_date, due_date, business_id,
             client_id, discount, tax_name, tax_percent, shipping,
             payment_method, terms, notes, signature)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)
        """, arrayOf(invoiceNumber, createdDate, dueDate, businessId,
            clientId, discount, taxName, taxPercent, shipping,
            paymentMethod, terms, notes, signature))
        var id = -1L
        val cursor = readableDatabase.rawQuery(
            "SELECT last_insert_rowid()", null)
        if (cursor.moveToFirst()) id = cursor.getLong(0)
        cursor.close()
        return id
    }

    fun getAllInvoices(): List<Map<String, String>> {
        val list = mutableListOf<Map<String, String>>()
        val cursor = readableDatabase.rawQuery("""
            SELECT i.*, c.name as client_name 
            FROM invoices i
            LEFT JOIN clients c ON i.client_id = c.id
            ORDER BY i.id DESC
        """, null)
        while (cursor.moveToNext()) {
            list.add(mapOf(
                "id"             to cursor.getInt(0).toString(),
                "invoice_number" to (cursor.getString(1) ?: ""),
                "created_date"   to (cursor.getString(2) ?: ""),
                "due_date"       to (cursor.getString(3) ?: ""),
                "discount"       to cursor.getDouble(6).toString(),
                "tax_name"       to (cursor.getString(7) ?: ""),
                "tax_percent"    to cursor.getDouble(8).toString(),
                "shipping"       to cursor.getDouble(9).toString(),
                "payment_method" to (cursor.getString(10) ?: ""),
                "terms"          to (cursor.getString(11) ?: ""),
                "notes"          to (cursor.getString(12) ?: ""),
                "signature"      to (cursor.getString(13) ?: ""),
                "client_name"    to (cursor.getString(14) ?: "")
            ))
        }
        cursor.close()
        return list
    }

    fun getInvoiceById(id: Int): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val cursor = readableDatabase.rawQuery("""
            SELECT i.*, c.name as client_name 
            FROM invoices i
            LEFT JOIN clients c ON i.client_id = c.id
            WHERE i.id = ?
        """, arrayOf(id.toString()))
        if (cursor.moveToFirst()) {
            result["id"]             = cursor.getInt(0).toString()
            result["invoice_number"] = cursor.getString(1) ?: ""
            result["created_date"]   = cursor.getString(2) ?: ""
            result["due_date"]       = cursor.getString(3) ?: ""
            result["business_id"]    = cursor.getInt(4).toString()
            result["client_id"]      = cursor.getInt(5).toString()
            result["discount"]       = cursor.getDouble(6).toString()
            result["tax_name"]       = cursor.getString(7) ?: ""
            result["tax_percent"]    = cursor.getDouble(8).toString()
            result["shipping"]       = cursor.getDouble(9).toString()
            result["payment_method"] = cursor.getString(10) ?: ""
            result["terms"]          = cursor.getString(11) ?: ""
            result["notes"]          = cursor.getString(12) ?: ""
            result["signature"]      = cursor.getString(13) ?: ""
            result["client_name"]    = cursor.getString(14) ?: ""
        }
        cursor.close()
        return result
    }

    fun deleteInvoice(id: Int) {
        writableDatabase.execSQL(
            "DELETE FROM invoice_items WHERE invoice_id=?", arrayOf(id))
        writableDatabase.execSQL(
            "DELETE FROM invoices WHERE id=?", arrayOf(id))
    }

    // ==================== INVOICE ITEMS ====================
    fun saveItem(invoiceId: Long, name: String, quantity: Double,
                 price: Double, discount: Double, tax: Double,
                 description: String) {
        writableDatabase.execSQL("""
            INSERT INTO invoice_items 
            (invoice_id, name, quantity, price, discount, tax, description)
            VALUES (?,?,?,?,?,?,?)
        """, arrayOf(invoiceId, name, quantity, price,
            discount, tax, description))
    }

    fun getItemsByInvoice(invoiceId: Int): List<Map<String, String>> {
        val list = mutableListOf<Map<String, String>>()
        val cursor = readableDatabase.rawQuery("""
            SELECT * FROM invoice_items WHERE invoice_id=?
        """, arrayOf(invoiceId.toString()))
        while (cursor.moveToNext()) {
            list.add(mapOf(
                "id"          to cursor.getInt(0).toString(),
                "invoice_id"  to cursor.getInt(1).toString(),
                "name"        to (cursor.getString(2) ?: ""),
                "quantity"    to cursor.getDouble(3).toString(),
                "price"       to cursor.getDouble(4).toString(),
                "discount"    to cursor.getDouble(5).toString(),
                "tax"         to cursor.getDouble(6).toString(),
                "description" to (cursor.getString(7) ?: "")
            ))
        }
        cursor.close()
        return list
    }

    fun deleteItem(id: Int) {
        writableDatabase.execSQL(
            "DELETE FROM invoice_items WHERE id=?", arrayOf(id))
    }

    // ==================== INVOICE NUMBER ====================

    fun getNextInvoiceNumber(): String {
        val cursor = readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM invoices", null)
        var count = 0
        if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close()
        return "INV%05d".format(count + 1)
    }
    fun getNoteById(id: Long): Note? {
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM construction_notes WHERE id=?",
            arrayOf(id.toString())
        )
        var note: Note? = null
        if (cursor.moveToFirst()) {
            note = Note(
                id       = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                title    = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                subtitle = cursor.getString(cursor.getColumnIndexOrThrow("subtitle")),
                body     = cursor.getString(cursor.getColumnIndexOrThrow("body")),
                color    = cursor.getInt(cursor.getColumnIndexOrThrow("color")),
                date     = cursor.getString(cursor.getColumnIndexOrThrow("date")),
                images   = cursor.getString(cursor.getColumnIndexOrThrow("images")) ?: "",
                files    = cursor.getString(cursor.getColumnIndexOrThrow("files")) ?: "",
                links    = cursor.getString(cursor.getColumnIndexOrThrow("links")) ?: ""
            )
        }
        cursor.close()
        return note
    }
    // إضافة admin يدوياً
    fun insertAdmin(first: String, last: String,
                    email: String, password: String): Boolean {
        val values = ContentValues().apply {
            put("firstName", first)
            put("lastName", last)
            put("email", email)
            put("password", password)
            put("role", "admin")
        }
        return writableDatabase.insert("users", null, values) != -1L
    }

    // تحقق هل المستخدم admin؟
    fun isAdmin(email: String): Boolean {
        val cursor = readableDatabase.rawQuery(
            "SELECT role FROM users WHERE email=?",
            arrayOf(email)
        )
        var admin = false
        if (cursor.moveToFirst()) {
            admin = cursor.getString(0) == "admin"
        }
        cursor.close()
        return admin
    }

    // جلب كل المستخدمين (للـ admin فقط)
    fun getAllUsers(): List<Map<String, String>> {
        val list = mutableListOf<Map<String, String>>()
        val cursor = readableDatabase.rawQuery(
            "SELECT id, firstName, lastName, email, role FROM users", null)
        while (cursor.moveToNext()) {
            list.add(mapOf(
                "id"        to cursor.getString(0),
                "firstName" to cursor.getString(1),
                "lastName"  to cursor.getString(2),
                "email"     to cursor.getString(3),
                "role"      to cursor.getString(4)
            ))
        }
        cursor.close()
        return list
    }
    fun getTotalUsers(): Int {
        val cursor = readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM users",
            null
        )

        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }

        cursor.close()
        return count
    }
    fun getTotalFeedback(): Int {
        val cursor = readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM feedback",
            null
        )

        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }

        cursor.close()
        return count
    }
    fun getAverageRating(): Double {
        val cursor = readableDatabase.rawQuery(
            "SELECT AVG(rating) FROM feedback",
            null
        )

        var avg = 0.0
        if (cursor.moveToFirst()) {
            avg = cursor.getDouble(0)
        }

        cursor.close()

        // حماية من null (إذا ما كاش بيانات)
        return if (avg.isNaN()) 0.0 else avg
    }
    fun getAllFeedback(): List<Map<String, String>> {
        val list = mutableListOf<Map<String, String>>()
        val cursor = readableDatabase.rawQuery(
            "SELECT email, rating, note FROM feedback ORDER BY id DESC", null
        )
        while (cursor.moveToNext()) {
            list.add(mapOf(
                "email"  to (cursor.getString(0) ?: ""),
                "rating" to (cursor.getString(1) ?: "0"),
                "note"   to (cursor.getString(2) ?: "")
            ))
        }
        cursor.close()
        return list
    }

    //  يمنع حذف Admin + يستقبل email بدل id
    fun deleteUser(email: String, currentEmail: String): Boolean {

        // ❌ منع حذف نفسه
        if (email == currentEmail) {
            return false
        }

        // ❌ منع حذف admin
        if (isAdmin(email)) {
            return false
        }

        return writableDatabase.delete(
            "users",
            "email=?",
            arrayOf(email)
        ) > 0
    }
    fun updateUser(email: String, newFirstName: String, newLastName: String, newRole: String): Boolean {
        val values = ContentValues().apply {
            put("firstName", newFirstName)
            put("lastName", newLastName)
            put("role", newRole)
        }

        return writableDatabase.update(
            "users",
            values,
            "email=?",
            arrayOf(email)
        ) > 0
    }

}

