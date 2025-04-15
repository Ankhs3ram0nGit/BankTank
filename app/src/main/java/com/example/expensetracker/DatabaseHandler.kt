package com.example.expensetracker

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast

// Constants for database columns
val DATABASE_NAME = "ExpenseDB"
val TABLE_NAME = "Accounts"
val COL_ID = "Account_ID"
val COL_NAME = "Account_Name"
val COL_BALANCE = "Balance"
val COL_COLOR = "Color"

class DatabaseHandler(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {

    // Create the database table
    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NAME TEXT,
                $COL_BALANCE DOUBLE,
                $COL_COLOR TEXT
            );
        """
        db?.execSQL(createTable)
    }
    fun getAllAccounts(): List<Account> {
        val accountList = mutableListOf<Account>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Accounts", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("Account_ID"))
                val name = cursor.getString(cursor.getColumnIndexOrThrow("Account_Name"))
                val balance = cursor.getDouble(cursor.getColumnIndexOrThrow("Balance"))
                val color = cursor.getString(cursor.getColumnIndexOrThrow("Color"))

                val account = Account(id, name, balance, color)
                accountList.add(account)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return accountList
    }

    fun getAccountByName(name: String): Account? {
        val db = readableDatabase
        val cursor = db.query(
            "Accounts",
            arrayOf("id", "name", "balance", "color"),
            "name = ?",
            arrayOf(name),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            val account = Account(
                cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("name")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("balance")),
                cursor.getString(cursor.getColumnIndexOrThrow("color"))
            )
            cursor.close()
            account
        } else {
            cursor.close()
            null
        }
    }

    fun deleteAccount(accountName: String): Boolean {
        val db = this.writableDatabase

        // Delete row where name matches
        val result = db.delete(
            TABLE_NAME,
            "$COL_NAME = ?",
            arrayOf(accountName)
        )

        db.close()

        if (result == 0) {
            // If no rows were deleted
            Toast.makeText(context, "Failed to delete account", Toast.LENGTH_SHORT).show()
            Log.e("Database", "Delete failed: No matching account for name=${accountName}")
            return false
        } else {
            // Successful deletion
            Toast.makeText(context, "Account deleted successfully", Toast.LENGTH_SHORT).show()
            Log.d("Database", "Account deleted: Name=${accountName}")
            return true
        }
    }





    fun updateAccount(account: Account) {
        val db = this.writableDatabase
        val contentValues = ContentValues()

        // Set the new values to be updated
        contentValues.put(COL_BALANCE, account.balance)
        contentValues.put(COL_COLOR, account.color)

        // Update row where name matches
        val result = db.update(
            TABLE_NAME,
            contentValues,
            "$COL_NAME = ?",
            arrayOf(account.name)
        )

        if (result == 0) {
            // If no rows were updated
            Toast.makeText(context, "Failed to update account", Toast.LENGTH_SHORT).show()
            Log.e("Database", "Update failed: No matching account for name=${account.name}")
        } else {
            // Successful update
            Toast.makeText(context, "Account updated successfully", Toast.LENGTH_SHORT).show()
            Log.d("Database", "Account updated: Name=${account.name}, New Balance=${account.balance}, New Color=${account.color}")
        }

        db.close()
    }



    // Upgrade database if the schema changes
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Insert a new account into the database
    fun insertData(account: Account) {
        val db = this.writableDatabase
        val contentValues = ContentValues()

        contentValues.put(COL_NAME, account.name)
        contentValues.put(COL_BALANCE, account.balance)
        contentValues.put(COL_COLOR, account.color)

        // Insert into the database
        val result = db.insert(TABLE_NAME, null, contentValues)

        if (result == -1L) {
            // If insertion failed
            Toast.makeText(context, "Failed to add account", Toast.LENGTH_SHORT).show()
            Log.e("Database", "Failed to add account")
        } else {
            // If insertion succeeded
            Toast.makeText(context, "Account added successfully", Toast.LENGTH_SHORT).show()
            Log.d("Database", "Account added: Name=${account.name}, Balance=${account.balance}, Color=${account.color}")
        }

        db.close() // Ensure the database is closed after the operation
    }
}
