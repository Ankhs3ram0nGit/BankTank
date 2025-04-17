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
val COL_AccountNumber = "Account_Number"
val COL_CURRENCY = "Currency"


val TABLE_INCOME = "Income"
val COL_INCOME_ID = "Income_ID"
val COL_INCOME_AMOUNT = "Income_Amount"
val COL_INCOME_DESCRIPTION = "Income_Description"


class DatabaseHandler(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, 2) {

    // Create the database table
    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
        CREATE TABLE IF NOT EXISTS $TABLE_NAME (
            $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COL_NAME TEXT NOT NULL UNIQUE,
            $COL_BALANCE DOUBLE,
            $COL_COLOR TEXT,
            $COL_AccountNumber TEXT,
            $COL_CURRENCY TEXT
        );
    """.trimIndent()

        db?.execSQL(createTable)

        // Add unique constraint only for non-null Account Numbers
        val createUniqueIndex = """
        CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_account_number 
        ON $TABLE_NAME($COL_AccountNumber) 
        WHERE $COL_AccountNumber IS NOT NULL;
    """.trimIndent()

        db?.execSQL(createUniqueIndex)

        val createIncomeTable = """
        CREATE TABLE IF NOT EXISTS $TABLE_INCOME (
            $COL_INCOME_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COL_INCOME_AMOUNT DOUBLE NOT NULL,
            $COL_INCOME_DESCRIPTION TEXT
        );
    """.trimIndent()

        db?.execSQL(createIncomeTable)
    }
    fun getAccountByNumber(accountNumber: String): Account? {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME WHERE $COL_AccountNumber = ?"
        val cursor = db.rawQuery(query, arrayOf(accountNumber))

        // Ensure the cursor has a valid row
        if (cursor != null && cursor.moveToFirst()) {
            // Accessing columns with checks to ensure they exist and are not null
            val nameIndex = cursor.getColumnIndex(COL_NAME)
            val balanceIndex = cursor.getColumnIndex(COL_BALANCE)
            val colorIndex = cursor.getColumnIndex(COL_COLOR)
            val currencyIndex = cursor.getColumnIndex(COL_CURRENCY)

            // Check if any of the column indices are invalid (column might not exist)
            if (nameIndex == -1 || balanceIndex == -1 || colorIndex == -1 || currencyIndex == -1) {
                cursor.close()
                return null
            }

            // Now retrieve the data with appropriate null checks
            val name = cursor.getString(nameIndex)
            val balance = cursor.getDouble(balanceIndex)
            val color = cursor.getString(colorIndex)
            val currency = cursor.getString(currencyIndex)

            // Return the account
            return Account(name, balance, color, accountNumber, currency)
        }

        // Close cursor to avoid memory leaks
        cursor?.close()
        return null
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
                val accountNumber = cursor.getString(cursor.getColumnIndexOrThrow("Account_Number"))
                val currency = cursor.getString(cursor.getColumnIndexOrThrow("Currency"))

                val account = Account(id, name, balance, color, accountNumber, currency)
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
            arrayOf("id", "name", "balance", "color", "accountNumber", "currency"),
            "name = ?",
            arrayOf(name),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            val account = Account(
                cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("name")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("balance")),
                cursor.getString(cursor.getColumnIndexOrThrow("color")),
                cursor.getString(cursor.getColumnIndexOrThrow("accountNumber")),
                cursor.getString(cursor.getColumnIndexOrThrow("currency"))
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
        contentValues.put(COL_CURRENCY, account.currency)
        contentValues.put(COL_AccountNumber, account.accountNumber)

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
        contentValues.put(COL_CURRENCY, account.currency)
        contentValues.put(COL_AccountNumber, account.accountNumber)

        // Insert into the database
        val result = db.insert(TABLE_NAME, null, contentValues)

        if (result == -1L) {
            // If insertion failed
            Toast.makeText(context, "Failed to add account", Toast.LENGTH_SHORT).show()
            Log.e("Database", "Failed to add account")
        } else {
            // If insertion succeeded
            Toast.makeText(context, "Account added successfully", Toast.LENGTH_SHORT).show()
            Log.d("Database", "Account added: Name=${account.name}, Balance=${account.balance}, Color=${account.color}, AccountNUmber=${account.accountNumber}, Currency=${account.currency}")
        }

        db.close() // Ensure the database is closed after the operation
    }


    fun insertIncome(amount: Double, description: String) {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COL_INCOME_AMOUNT, amount)
            put(COL_INCOME_DESCRIPTION, description)
        }

        val result = db.insert(TABLE_INCOME, null, contentValues)

        if (result == -1L) {
            Toast.makeText(context, "Failed to add income", Toast.LENGTH_SHORT).show()
            Log.e("Database", "Failed to add income: Amount=$amount, Desc=$description")
        } else {
            Toast.makeText(context, "Income added", Toast.LENGTH_SHORT).show()
            Log.d("Database", "Income added: Amount=$amount, Desc=$description")
        }

        db.close()
    }



}
