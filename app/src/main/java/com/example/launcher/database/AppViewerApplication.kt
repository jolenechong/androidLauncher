package com.example.launcher.database

import android.app.Application
import android.content.Context
import android.database.Cursor
import android.util.Log
import com.example.launcher.DatabaseAdapter

class AppViewerApplication(): Application(){

    private var appList: ArrayList<String> = ArrayList<String>()
    private var appIdList: ArrayList<Int> = ArrayList<Int>()
    private var note: String = String()

    fun addToDatabase(name: String, package_name: String, c: Context): Long? {

        val db = DatabaseAdapter(c)
        db.open()

        val rowIDofInsertedEntry = db.insertEntry(name, package_name,  c)

        db.close()

        return rowIDofInsertedEntry
    }

    fun deleteFrmDatabase(packageName: String, c: Context): Boolean {
        val db = DatabaseAdapter(c)
        db.open()

        // TODO: appIdList is empty here idk y
        retrieveAll(c)

        val updateStatus = db.removeEntry(packageName)

        db.close()
        return updateStatus
    }

    fun retrieveAll(c: Context): ArrayList<String> {

        val myCursor: Cursor?
        val db = DatabaseAdapter(c)

        db.open()

        appList.clear()
        appIdList.clear()

        myCursor = db.retrieveAllEntriesCursor()
        if (myCursor != null && myCursor.count > 0) {
            myCursor.moveToFirst()
            do{
                appIdList.add(myCursor.getInt(db.COLUMN_KEY_ID))
                // add to list of package names saved
                appList.add(myCursor.getString(db.COLUMN_PACKAGE_NAME_ID))

            }while(myCursor.moveToNext())
        }

        db.close()
        return appList

    }

    fun insertNoteEntry(text: String, c: Context): Long? {
        val db = DatabaseAdapter(c)
        db.open()

        val rowIDofInsertedEntry = db.insertNoteEntry(text,  c)

        db.close()

        return rowIDofInsertedEntry
    }

    fun retrieveNote(c: Context): String {

        val myCursor: Cursor?
        val db = DatabaseAdapter(c)

        db.open()
        note = ""

        myCursor = db.retrieveNote()
        if (myCursor != null && myCursor.count > 0) {
            myCursor.moveToFirst()
            do{
                note = myCursor.getString(db.COLUMN_TEXT_ID)
            }while(myCursor.moveToNext())
        }

        db.close()
        return note

    }

}