package com.example.amirapplication

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.annotation.OptIn
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi

data class UserProfile(val name: String, val age: Int, val image: ByteArray?)

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "userDatabase"
        private const val DATABASE_VERSION = 2

        private const val TABLE_USERS = "users"
        private const val COL_ID = "id"
        private const val COL_USERNAME = "username"
        private const val COL_PASSWORD = "password"

        private const val TABLE_EMERGENCY_CONTACTS = "emergency_contacts"
        private const val COL_CONTACT_ID = "id"
        private const val COL_CONTACT_NUMBER = "contact_number"
        private const val COL_USERNAME_EMERGENCY = "username"

        private const val TABLE_PROFILE = "user_profile"
        private const val COL_PROFILE_ID = "id"
        private const val COL_NAME = "name"
        private const val COL_AGE = "age"
        private const val COL_IMAGE = "image"
        private const val COL_USERNAME_PROFILE = "username"


    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createUserTable = """
            CREATE TABLE $TABLE_USERS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USERNAME TEXT,
                $COL_PASSWORD TEXT
            )
        """.trimIndent()

        val createContactsTable = """
            CREATE TABLE $TABLE_EMERGENCY_CONTACTS (
                $COL_CONTACT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CONTACT_NUMBER TEXT,
                $COL_USERNAME_EMERGENCY TEXT
            )
        """.trimIndent()

        val createProfileTable = """
        CREATE TABLE IF NOT EXISTS $TABLE_PROFILE (
            $COL_PROFILE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COL_USERNAME TEXT NOT NULL,
            $COL_NAME TEXT NOT NULL,
            $COL_AGE INTEGER NOT NULL,
            $COL_IMAGE BLOB
        )
        """.trimIndent()

        db?.execSQL(createProfileTable)



        db?.execSQL(createUserTable)
        db?.execSQL(createContactsTable)
        db?.execSQL(createProfileTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_EMERGENCY_CONTACTS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PROFILE")
        onCreate(db)
    }

    fun insertOrUpdateProfile(username: String, name: String, age: Int, image: ByteArray?) {
        val db = writableDatabase
        db.delete(TABLE_PROFILE, "$COL_USERNAME = ?", arrayOf(username))

        val values = ContentValues().apply {
            put(COL_USERNAME, username)
            put(COL_NAME, name)
            put(COL_AGE, age.toString())
            put(COL_IMAGE, image)
        }
        db.insert(TABLE_PROFILE, null, values)
    }

    @OptIn(UnstableApi::class)
    fun getProfile(username: String): Profile? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='$TABLE_PROFILE';",
            null
        )

        if (cursor.count == 0) {
            // Handle the case where the table doesn't exist
            Log.e("DatabaseHelper", "Table $TABLE_PROFILE not found.")
            cursor.close()
            return null
        }

        cursor.close()

        // Proceed with the query
        val query = "SELECT * FROM $TABLE_PROFILE WHERE $COL_USERNAME = ? LIMIT 1"
        val profileCursor = db.rawQuery(query, arrayOf(username))

        return if (profileCursor.moveToFirst()) {
            val name = profileCursor.getString(profileCursor.getColumnIndexOrThrow(COL_NAME))
            val age = profileCursor.getString(profileCursor.getColumnIndexOrThrow(COL_AGE)).toInt()
            val image = profileCursor.getBlob(profileCursor.getColumnIndexOrThrow(COL_IMAGE))
            profileCursor.close()
            Profile(name, age, image)
        } else {
            profileCursor.close()
            null
        }
    }


    fun insertEmergencyContacts(username: String, contactNumbers: String) {
        val db = writableDatabase
        db.delete(TABLE_EMERGENCY_CONTACTS, "$COL_USERNAME_EMERGENCY = ?", arrayOf(username))
        contactNumbers.split(",").map { it.trim() }.forEach { contact ->
            val values = ContentValues().apply {
                put(COL_CONTACT_NUMBER, contact)
                put(COL_USERNAME_EMERGENCY, username)
            }
            db.insert(TABLE_EMERGENCY_CONTACTS, null, values)
        }
    }

    fun getAllEmergencyContacts(username: String): List<String> {
        val contacts = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_CONTACT_NUMBER FROM $TABLE_EMERGENCY_CONTACTS WHERE $COL_USERNAME_EMERGENCY = ?",
            arrayOf(username)
        )
        if (cursor.moveToFirst()) {
            do {
                contacts.add(cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTACT_NUMBER)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return contacts
    }

    fun addUser(username: String, password: String): Long {
        val db = writableDatabase
        val values = ContentValues()
        values.put(COL_USERNAME, username)
        values.put(COL_PASSWORD, password)
        return db.insert(TABLE_USERS, null, values)
    }

    fun getUser(username: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USERS WHERE $COL_USERNAME = ? AND $COL_PASSWORD = ?",
            arrayOf(username, password)
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }
}
