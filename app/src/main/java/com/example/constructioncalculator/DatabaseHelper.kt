package com.example.constructioncalculator

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "ConstructionDB", null, 10) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                firstName TEXT,
                lastName TEXT,
                email TEXT UNIQUE,
                password TEXT,
                otp TEXT,
                profile_image TEXT
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
    }

    // ================= USERS =================
    fun insertUser(first: String, last: String, email: String, password: String): Boolean {
        val values = ContentValues().apply {
            put("firstName", first)
            put("lastName", last)
            put("email", email)
            put("password", password)
            put("otp", (1000..9999).random().toString())
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
        val values = ContentValues().apply {
            put("bricks", bricks)
            put("cement", cement)
            put("sand", sand)
            put("total", total)
        }
        return writableDatabase.insert("results", null, values) != -1L
    }

    // ================= PLASTER =================
    fun insertPlaster(
        area: Double, thickness: Double, ratio: String,
        cement: Double, sand: Double, water: Double, volume: Double
    ) {
        val values = ContentValues().apply {
            put("area", area)
            put("thickness", thickness)
            put("ratio", ratio)
            put("cement", cement)
            put("sand", sand)
            put("water", water)
            put("volume", volume)
        }
        writableDatabase.insert("plaster", null, values)
    }

    // ================= PAINT =================
    fun insertPaint(type: String, area: Double, coats: Int, paint: Double, date: String) {
        val values = ContentValues().apply {
            put("type", type)
            put("area", area)
            put("coats", coats)
            put("paint", paint)
            put("date", date)
        }
        writableDatabase.insert("paint_results", null, values)
    }

    // ================= TILES =================
    fun insertTile(type: String, area: Double, count: Double, date: String) {
        val values = ContentValues().apply {
            put("type", type)
            put("area", area)
            put("count", count)
            put("date", date)
        }
        writableDatabase.insert("tiles", null, values)
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

    fun insert(type: String, h: Double, steps: Int, length: Double, area: Double) {

        val cv = ContentValues()

        cv.put("type", type)
        cv.put("height", h)
        cv.put("steps", steps)
        cv.put("length", length)
        cv.put("area", area)
        cv.put("date", System.currentTimeMillis()) // ✅ Long وليس String

        writableDatabase.insert("history", null, cv)
    }

    fun getAll(): ArrayList<HistoryItem> {

        val list = ArrayList<HistoryItem>()

        val c = readableDatabase.rawQuery(
            "SELECT * FROM history ORDER BY id DESC",
            null
        )

        while (c.moveToNext()) {

            val item = HistoryItem(
                type = c.getString(1),
                height = c.getDouble(2),
                steps = c.getInt(3),
                length = c.getDouble(4),
                area = c.getDouble(5),
                date = c.getLong(6)
            )

            list.add(item)
        }

        c.close()
        return list
    }
}

