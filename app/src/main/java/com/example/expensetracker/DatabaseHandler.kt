package com.example.expensetracker

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast
import com.example.expensetracker.ui.dashboard.UI_Tank
import com.example.expensetracker.ui.dashboard.DB_Tank


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

val COL_MAX_ALLOCATION = "Max_Allocation"

val COL_TANK_ID = "Tank_ID"
val COL_TANK_NAME = "Tank_Name"
val COL_TANK_ALLOCATION = "Tank_Allocation"
val COL_TANK_COLOR = "Tank_Color"
val COL_CURRENT_ALLOCATION = "Current_Allocation"

val TABLE_TRANSACTIONS = "Transactions"
val COL_TRANSACTION_ID = "Transaction_ID"
val COL_ACCOUNT_ID = "Account_ID"
val COL_TTANK_ID = "Tank_ID"
val COL_TRANSACTION_AMOUNT = "Transaction_Amount"
val COL_TRANSACTION_DATE = "Transaction_Date"
val COL_TRANSACTION_DESCRIPTION = "Transaction_Description"
val COL_TRANSACTION_TAG = "Transaction_Tag"
val COL_TRANSACTION_BML_REFERENCE = "BML_Reference"
val COL_TRANSACTION_BML_DATE = "BML_Date"
val COL_TRANSACTION_TYPE = "Transaction_Type"



class DatabaseHandler(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {

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


        val createTanksTable = """
            CREATE TABLE IF NOT EXISTS Tanks (
            $COL_TANK_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COL_TANK_NAME TEXT NOT NULL UNIQUE,
            $COL_TANK_ALLOCATION DOUBLE NOT NULL,
            $COL_TANK_COLOR TEXT,
            $COL_CURRENT_ALLOCATION DOUBLE NOT NULL DEFAULT 0.0
    );
""".trimIndent()

        db?.execSQL(createTanksTable)


        val createMaxAllocationTable = """
            CREATE TABLE IF NOT EXISTS MaxAllocation (
            $COL_MAX_ALLOCATION DOUBLE
    );
""".trimIndent()

        db?.execSQL(createMaxAllocationTable)

        val createTransactionsTable = """
        CREATE TABLE IF NOT EXISTS $TABLE_TRANSACTIONS (
            $COL_TRANSACTION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COL_ACCOUNT_ID TEXT,
            $COL_TTANK_ID TEXT,
            $COL_TRANSACTION_AMOUNT DOUBLE NOT NULL,
            $COL_TRANSACTION_DATE TEXT NOT NULL,
            $COL_TRANSACTION_DESCRIPTION TEXT,
            $COL_TRANSACTION_BML_REFERENCE TEXT,
            $COL_TRANSACTION_BML_DATE TEXT,
            $COL_TRANSACTION_TYPE TEXT,
            $COL_TRANSACTION_TAG TEXT,
            FOREIGN KEY($COL_ACCOUNT_ID) REFERENCES $TABLE_NAME($COL_NAME),
            FOREIGN KEY($COL_TANK_ID) REFERENCES Tanks($COL_TANK_NAME)
        );
    """.trimIndent()

        db?.execSQL(createTransactionsTable)
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
    fun updateMaxAllocation(allocation: Double) {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_MAX_ALLOCATION, allocation)
        }

        // Check if there's already a value
        val cursor = db.rawQuery("SELECT COUNT(*) FROM MaxAllocation", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()

        if (count == 0) {
            // table empty → insert
            db.insert("MaxAllocation", null, cv)
        } else {
            // update the sole row
            db.update(
                "MaxAllocation",
                cv,
                null,
                null
            )
        }
        db.close()
    }


    fun getMaxAllocation(): Double {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COL_MAX_ALLOCATION FROM MaxAllocation", null)
        val allocation = if (cursor.moveToFirst()) {
            cursor.getDouble(cursor.getColumnIndexOrThrow(COL_MAX_ALLOCATION))
        } else {
            0.0
        }
        cursor.close()
        db.close()
        return allocation
    }

    fun getRemainingAllocation(): Double {
        val db = readableDatabase
        var totalAllocated = 0.0

        val cursor = db.rawQuery("SELECT SUM($COL_TANK_ALLOCATION) FROM Tanks", null)
        if (cursor != null && cursor.moveToFirst()) {
            if (!cursor.isNull(0)) {
                totalAllocated = cursor.getDouble(0)
            }
        }
        cursor?.close()

        val maxAllocation = getMaxAllocation()
        db.close()

        return maxAllocation - totalAllocated
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

    fun updateTankAfterTransaction(dbTank: DB_Tank, transactionAmount: Double) {
        val db = this.writableDatabase
        val contentValues = ContentValues()

        // Update current allocation only (can go negative or exceed max)
        val newCurrentAllocation = dbTank.currentAllocation + transactionAmount
        contentValues.put(COL_CURRENT_ALLOCATION, newCurrentAllocation)

        val result = db.update(
            "Tanks",
            contentValues,
            "$COL_TANK_NAME = ?",
            arrayOf(dbTank.name)
        )

        if (result == 0) {
            Log.e("Database", "Failed to update tank allocation for ${dbTank.name}")
            Toast.makeText(context, "Failed to update tank allocation", Toast.LENGTH_SHORT).show()
        } else {
            Log.d("Database", "Tank allocation updated: ${dbTank.name} -> $newCurrentAllocation (max stays ${dbTank.maxAllocation})")
        }

        db.close()
    }




    fun updateTank(dbTank: DB_Tank, oldAllocation: Double) {
        val db = this.writableDatabase
        val contentValues = ContentValues()

        // Set the new values to be updated
        contentValues.put(COL_TANK_NAME, dbTank.name)
        contentValues.put(COL_TANK_ALLOCATION, dbTank.maxAllocation)
        contentValues.put(COL_TANK_COLOR, dbTank.color)

        if (dbTank.currentAllocation > dbTank.maxAllocation) {
            contentValues.put(COL_CURRENT_ALLOCATION, dbTank.maxAllocation)
        } else {
            val newAllocation = (dbTank.maxAllocation - oldAllocation)
            val updatedAllocation = oldAllocation + newAllocation
            contentValues.put(COL_CURRENT_ALLOCATION, updatedAllocation)

        }

        // Update row where name matches
        val result = db.update(
            "Tanks",
            contentValues,
            "$COL_TANK_NAME = ?",
            arrayOf(dbTank.name)
        )

        if (result == 0) {
            // If no rows were updated
            Toast.makeText(context, "Failed to update account", Toast.LENGTH_SHORT).show()
            Log.e("Database", "Update failed: No matching account for name=${dbTank.name}")
        } else {
            // Successful update
            Toast.makeText(context, "Account updated successfully", Toast.LENGTH_SHORT).show()
            Log.d("Database", "Account updated: Name=${dbTank.name}, New Balance=${dbTank.maxAllocation}, New Color=${dbTank.color}")
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

    // Insert a new tank into the Tanks table
    fun insertTank(dbTank: DB_Tank) {
        val db = this.writableDatabase
        val contentValues = ContentValues()

        contentValues.put(COL_TANK_NAME, dbTank.name)
        contentValues.put(COL_TANK_ALLOCATION, dbTank.maxAllocation)
        contentValues.put(COL_TANK_COLOR, dbTank.color)
        contentValues.put(COL_CURRENT_ALLOCATION, dbTank.currentAllocation)  // Add default value for the new column

        val result = db.insert("Tanks", null, contentValues)

        if (result == -1L) {
            Toast.makeText(context, "Failed to add tank", Toast.LENGTH_SHORT).show()
            Log.e("Database", "Failed to add tank")
        } else {
            Toast.makeText(context, "Tank added successfully", Toast.LENGTH_SHORT).show()
            Log.d("Database", "Tank added: Name=${dbTank.name}, Max_Allocation=${dbTank.maxAllocation}, Color=${dbTank.color}")
        }

        db.close() // Close the database
    }


    // Fetch all tanks from the database
    fun getAllTanksUI(): List<UI_Tank> {
        val uiTankList = mutableListOf<UI_Tank>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Tanks", null)

        if (cursor.moveToFirst()) {
            do {
                // Fetch the data from the DB columns
                val tankName = cursor.getString(cursor.getColumnIndexOrThrow(COL_TANK_NAME))
                val tankAllocation = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TANK_ALLOCATION))
                val currentAllocation = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_CURRENT_ALLOCATION))  // Fetch the new column
                val color = cursor.getString(cursor.getColumnIndexOrThrow(COL_TANK_COLOR))

                // Create a UI_Tank (without tankId)
                val uiTank = UI_Tank(tankName, tankAllocation, color ?: "#FFFFFF", currentAllocation) // Default color to white if null
                uiTankList.add(uiTank)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return uiTankList
    }
    fun getAllTanks(): List<DB_Tank> {
        val dbTankList = mutableListOf<DB_Tank>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Tanks", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TANK_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COL_TANK_NAME))
                val maxAllocation = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TANK_ALLOCATION))
                val currentAllocation = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_CURRENT_ALLOCATION))
                val color = cursor.getString(cursor.getColumnIndexOrThrow(COL_TANK_COLOR))

                val dbTank = DB_Tank(
                    id = id,
                    name = name,
                    maxAllocation = maxAllocation,
                    currentAllocation = currentAllocation,
                    color = color
                )
                dbTankList.add(dbTank)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return dbTankList
    }


    fun deleteTank(dbTank: DB_Tank): Boolean {
        val db = this.writableDatabase

        // Delete row where name matches
        val result = db.delete(
            "Tanks",
            "$COL_TANK_NAME = ?",
            arrayOf(dbTank.name)
        )

        db.close()

        if (result == 0) {
            // If no rows were deleted
            Toast.makeText(context, "Failed to delete account", Toast.LENGTH_SHORT).show()
            Log.e("Database", "Delete failed: No matching tank for name=${dbTank.name}")
            return false
        } else {
            // Successful deletion
            Toast.makeText(context, "Tank deleted successfully", Toast.LENGTH_SHORT).show()
            Log.d("Database", "Tank deleted: Name=${dbTank.name}")
            return true
        }
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

    fun getAllTransactionsForAccount(accountId: String): List<TransactionData> {
        val transactionList = mutableListOf<TransactionData>()
        val db = readableDatabase

        // filter by Account_ID, not Account_Name
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_TRANSACTIONS WHERE $COL_ACCOUNT_ID = ?",
            arrayOf(accountId)
        )

        if (cursor.moveToFirst()) {
            do {
                // read all columns—now including tag, bml, type…
                val id        = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TRANSACTION_ID))
                val accId     = cursor.getString(cursor.getColumnIndexOrThrow(COL_ACCOUNT_ID))
                val tankId    = cursor.getString(cursor.getColumnIndexOrThrow(COL_TANK_ID))
                val amount    = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TRANSACTION_AMOUNT))
                val date      = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANSACTION_DATE))
                val desc      = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANSACTION_DESCRIPTION))
                val tag       = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANSACTION_TAG))
                val bmlRef    = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANSACTION_BML_REFERENCE))
                val bmlDate   = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANSACTION_BML_DATE))
                val txType    = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANSACTION_TYPE))

                transactionList += TransactionData(
                    transactionId   = id,
                    accountId       = accId,
                    tankId          = tankId,
                    amount          = amount,
                    date            = date,
                    description     = desc,
                    tag             = tag,
                    bmlReference    = bmlRef,
                    bmlDate         = bmlDate,
                    transactionType = txType
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return transactionList
    }

    fun getAllTransactionsForTank(tankName: String): List<TransactionData> {
        val transactionList = mutableListOf<TransactionData>()
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_TRANSACTIONS WHERE $COL_TANK_ID = ?",
            arrayOf(tankName)
        )

        if (cursor.moveToFirst()) {
            do {
                val id        = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TRANSACTION_ID))
                val accId     = cursor.getString(cursor.getColumnIndexOrThrow(COL_ACCOUNT_ID))
                val tId       = cursor.getString(cursor.getColumnIndexOrThrow(COL_TANK_ID))
                val amount    = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TRANSACTION_AMOUNT))
                val date      = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANSACTION_DATE))
                val desc      = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANSACTION_DESCRIPTION))
                val tag       = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANSACTION_TAG))
                val bmlRef    = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANSACTION_BML_REFERENCE))
                val bmlDate   = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANSACTION_BML_DATE))
                val txType    = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANSACTION_TYPE))

                transactionList += TransactionData(
                    transactionId   = id,
                    accountId       = accId,
                    tankId          = tId,
                    amount          = amount,
                    date            = date,
                    description     = desc,
                    tag             = tag,
                    bmlReference    = bmlRef,
                    bmlDate         = bmlDate,
                    transactionType = txType
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return transactionList
    }


    fun insertTransaction(transactionData: TransactionData): Long {
        val db = this.writableDatabase
        val cv = ContentValues().apply {
            put(COL_ACCOUNT_ID,        transactionData.accountId)
            put(COL_TANK_ID,           transactionData.tankId)
            put(COL_TRANSACTION_AMOUNT,transactionData.amount)
            put(COL_TRANSACTION_DATE,  transactionData.date)
            put(COL_TRANSACTION_DESCRIPTION, transactionData.description)
            put(COL_TRANSACTION_TAG,   transactionData.tag)
            put(COL_TRANSACTION_BML_REFERENCE, transactionData.bmlReference)
            put(COL_TRANSACTION_BML_DATE,      transactionData.bmlDate)
            put(COL_TRANSACTION_TYPE, transactionData.transactionType)
        }
        val rowId = db.insert(TABLE_TRANSACTIONS, null, cv)
        if (rowId == -1L) {
            Log.e("DB", "Failed to add tx: $transactionData")
            Toast.makeText(context, "Failed to add transaction", Toast.LENGTH_SHORT).show()
        } else {
            Log.d("DB", "Inserted tx #$rowId")
            Toast.makeText(context, "Transaction added", Toast.LENGTH_SHORT).show()
        }
        // don't close the DB here! let the caller finish its work, then close.
        return rowId
    }






}
