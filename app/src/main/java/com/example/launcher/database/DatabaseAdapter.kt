package com.example.launcher

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log


class DatabaseAdapter(c: Context) {
    private val DATABASE_TABLE = "AppsDB"
    private val DATABASE_NOTE_TABLE = "NoteDB"

    private var dbHelper: DatabaseHelper? = null
    private val DATABASE_NAME = "app_launcher.db"
    private val DATABASE_VER = 1

    private var _db: SQLiteDatabase? = null

    val KEY_ID = "_id"
    val COLUMN_KEY_ID = 0

    // FOR APPS TABLE
    val ENTRY_NAME = "entry_name"
    val COLUMN_NAME_ID = 1
    val ENTRY_PACKAGE_NAME = "entry_package_name"
    val COLUMN_PACKAGE_NAME_ID = 2

    // FOR NOTES TABLE
    val ENTRY_TEXT = "entry_text"
    val COLUMN_TEXT_ID = 1


    protected val DATABASE_CREATE = ("create table " + DATABASE_TABLE + " " + "("
            + KEY_ID + " integer primary key autoincrement, "
            + ENTRY_NAME + " Text, "
            + ENTRY_PACKAGE_NAME + " Text not null);")

    protected val DATABASE_NOTE_CREATE = ("create table " + DATABASE_NOTE_TABLE + " " + "("
            + KEY_ID + " integer, "
            + ENTRY_TEXT + " Text not null);")

    private val MYDBADAPTER_LOG_CAT = "MY_LOG"


    init {
        // Create a MyDBOpenHelper object
        dbHelper = DatabaseHelper(c)
    }

    fun close() {
        // close the table using the SQLite database handler
        _db?.close()
    }


    fun open() {
        // Open DB using the appropriate methods
        try {
            _db = dbHelper?.getWritableDatabase()
        } catch (e: SQLiteException) {
            _db = dbHelper?.getReadableDatabase()
        }
    }

    fun insertEntry(name: String, package_name: String, c: Context): Long? {
        // insert record into table
        val newEntryValues = ContentValues()

        newEntryValues.put(ENTRY_NAME, name)
        newEntryValues.put(ENTRY_PACKAGE_NAME, package_name)

        return _db?.insert(DATABASE_TABLE, null, newEntryValues)
    }


    fun removeEntry(packageName: String): Boolean {
        //  remove record from table

        val whereClause = ENTRY_PACKAGE_NAME + "=?"
        val whereArgs = arrayOf<String>(java.lang.String.valueOf(packageName))

        if (_db?.delete(DATABASE_TABLE, whereClause, whereArgs)!! <= 0
        ) {
            Log.w(MYDBADAPTER_LOG_CAT, "Removing entry where id = $packageName failed")

            return false
        }
        return true
    }

    fun retrieveAllEntriesCursor(): Cursor? {
        // retrieve all records from table
        var c: Cursor? = null
        try {
            c = _db?.query(
                DATABASE_TABLE,
                arrayOf(KEY_ID, ENTRY_NAME, ENTRY_PACKAGE_NAME),
                null,
                null,
                null,
                null,
                null
            )
        } catch (e: SQLiteException) {
            Log.w(DATABASE_TABLE, "Retrieve fail")
        }

        return c
    }

    fun insertNoteEntry(text: String, c: Context): Long? {
        // insert or update record into table

        val contentValues = ContentValues()
        contentValues.put(KEY_ID, 1)
        contentValues.put(ENTRY_TEXT, text)

        // this will insert if record is new, update otherwise
        return _db?.insertWithOnConflict(DATABASE_NOTE_TABLE, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun retrieveNote(): Cursor? {
        // retrieve all records from table

        var c: Cursor? = null
        try {
            c = _db?.query(
                DATABASE_NOTE_TABLE,
                arrayOf(KEY_ID, ENTRY_TEXT),
                null,
                null,
                null,
                null,
                null
            )
        } catch (e: SQLiteException) {
            Log.w(DATABASE_TABLE, "Retrieve fail")
        }

        return c
    }

    inner class DatabaseHelper(c: Context) : SQLiteOpenHelper(c, DATABASE_NAME, null, DATABASE_VER) {
        // helper class to create db
        override fun onCreate(db: SQLiteDatabase?) {
            db!!.execSQL(DATABASE_CREATE)
            db.execSQL(DATABASE_NOTE_CREATE)
            Log.w(MYDBADAPTER_LOG_CAT, "HELPER : DB $DATABASE_TABLE and $DATABASE_NOTE_TABLE created!")

        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            if (db != null) {
                db.execSQL("DROP TABLE IF EXISTS AppsDB")
            };
            if (db != null) {
                db.execSQL("DROP TABLE IF EXISTS NoteDB")
            };
            onCreate(db);
        }

    }

}