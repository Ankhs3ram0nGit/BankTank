package com.example.expensetracker.ui.home

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.Account
import com.example.expensetracker.DatabaseHandler
import com.example.expensetracker.R
import com.example.expensetracker.TransactionData
import com.example.expensetracker.ui.dashboard.DB_Tank
import com.example.expensetracker.ui.dashboard.UI_Tank
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.android.material.card.MaterialCardView

class AccountDetailFragment : Fragment() {
    private lateinit var databaseHandler: DatabaseHandler
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.account_details, container, false)

        val accountName = arguments?.getString("accountName") ?: "Unknown Account"
        val accountBalance = arguments?.getString("accountBalance") ?: "N/A"
        val currency = arguments?.getString("currency") ?: "N/A"
        val accountColorName = arguments?.getString("accountColor") ?: "Gray"

        val accountNameTextView = view.findViewById<TextView>(R.id.accountNameText)
        val accountBalanceTextView = view.findViewById<TextView>(R.id.accountBalanceText)
        val accountCurrencyTextView = view.findViewById<TextView>(R.id.accountCurrencyText)

        accountNameTextView.text = accountName
        accountBalanceTextView.text = "$accountBalance"
        accountCurrencyTextView.text = "$currency"

        val colorNameMap = mapOf(
            "Green" to "account_green",
            "Red" to "account_red",
            "Yellow" to "account_yellow",
            "Gray" to "account_gray",
            "Forest Green" to "account_forest_green",
            "Mighty Purple" to "account_mighty_purple",
            "Purple" to "account_purple",
            "Seal Blue" to "account_seal_blue",
            "Royal Blue" to "account_royal_blue"
        )

        val selectedColorName = arguments?.getString("color") ?: "account_gray"
        val colorResId = resources.getIdentifier(selectedColorName, "color", requireContext().packageName)

        val accountCard = view.findViewById<MaterialCardView>(R.id.accountCard)
        if (colorResId != 0) {
            accountCard.setStrokeColor(ContextCompat.getColor(requireContext(), colorResId))
        } else {
            accountCard.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.account_gray))
        }





        val dbHandler = DatabaseHandler(requireContext())
        val accountId = arguments?.getString("accountName") ?: "N/A"

        val transactions = dbHandler.getAllTransactionsForAccount(accountId)
        val recyclerView = view.findViewById<RecyclerView>(R.id.transactionsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = TransactionAdapter(transactions) { transaction ->
            showTransactionDetailsDialog(transaction)
        }

        val addTransactionButton = view.findViewById<Button>(R.id.addTransactionButton)
        addTransactionButton.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(requireContext(), android.R.color.white)
        )

        addTransactionButton.setOnClickListener {
            showAddTransactionDialog(requireContext(), accountName) { transaction ->
                val updatedTransactions = dbHandler.getAllTransactionsForAccount(accountId)
                recyclerView.adapter = TransactionAdapter(updatedTransactions) { updatedTransaction ->
                    showTransactionDetailsDialog(updatedTransaction)
                }
            }
        }


        return view
    }



    private fun showTransactionDetailsDialog(transaction: TransactionData) {
        // 1) Inflate your custom layout
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_transaction_details, null)

        // 2) Grab the CardView to set its outline color
        val card = dialogView.findViewById<MaterialCardView>(R.id.transactionCardView)
        val strokeColorRes = if (transaction.transactionType == "expense")
            android.R.color.holo_red_dark
        else
            android.R.color.holo_green_dark
        card.setStrokeColor(ContextCompat.getColor(requireContext(), strokeColorRes))

        // 3) Populate all the fields
        dialogView.findViewById<TextView>(R.id.transactionDetailAmount).apply {
            text = String.format("%.2f", transaction.amount)
        }
        dialogView.findViewById<TextView>(R.id.transactionDetailDescription).text =
            "Description: ${transaction.description ?: "None"}"
        dialogView.findViewById<TextView>(R.id.transactionDetailTags).text =
            "TAG(S): ${transaction.tag ?: ""}"
        dialogView.findViewById<TextView>(R.id.transactionDetailCategory).text =
            "Category: ${transaction.tankId}"
        dialogView.findViewById<TextView>(R.id.transactionDetailAccount).text =
            "Account: ${transaction.accountId}"
        dialogView.findViewById<TextView>(R.id.transactionDetailDateAdded).text =
            "Date Added: ${transaction.date}"
        dialogView.findViewById<TextView>(R.id.transactionDetailBmlReference).text =
            "BML Reference: ${transaction.bmlReference ?: "None"}"
        dialogView.findViewById<TextView>(R.id.transactionDetailBmlDate).text =
            "BML Date: ${transaction.bmlDate ?: "None"}"


        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomDialogStyle)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // make the dialog window background transparent
        dialog.window
            ?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.show()
    }





    fun showAddTransactionDialog(context: Context,     selectedAccountName: String?, onTransactionAdded: (TransactionData) -> Unit) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.add_transaction, null)

        val descriptionEditText = dialogView.findViewById<EditText>(R.id.Description)
        val tagsEditText = dialogView.findViewById<EditText>(R.id.Tags)
        val tankSpinner = dialogView.findViewById<Spinner>(R.id.Tank)
        val accountSpinner = dialogView.findViewById<Spinner>(R.id.Account)
        val amountEditText = dialogView.findViewById<EditText>(R.id.Amount)
        val bmlRefEditText = dialogView.findViewById<EditText>(R.id.BML_Reference)
        val bmlDateEditText = dialogView.findViewById<EditText>(R.id.BML_Date)

        val databaseHandler = DatabaseHandler(context)

        // Populate Account Spinner
        val accountList = databaseHandler.getAllAccounts()
        val accountNames = accountList.map { it.name } // List of account names for spinner
        val accountAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, accountNames)
        accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        accountSpinner.adapter = accountAdapter

        selectedAccountName?.let {
            val selectedIndex = accountNames.indexOf(it)
            if (selectedIndex >= 0) {
                accountSpinner.setSelection(selectedIndex)
            }
        }


        // Populate Tank Spinner
        val tankList = databaseHandler.getAllTanksUI()
        val tankNames = tankList.map { it.name } // List of tank names for spinner
        val tankAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, tankNames)
        tankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        tankSpinner.adapter = tankAdapter

        val dialogBuilder = AlertDialog.Builder(context, R.style.CustomDialogStyle)
            .setView(dialogView)
            .setCancelable(true)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        val expenseButton = Button(context).apply {
            text = "Expense"
            setTextColor(Color.RED)
            background = null
            backgroundTintList = null
            val border = GradientDrawable().apply {
                setStroke(4, Color.RED)
                cornerRadius = 50f
                setColor(Color.TRANSPARENT)
            }
            background = border

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = 32
            }
            layoutParams = params
        }

        val incomeButton = Button(context).apply {
            text = "Income"
            setTextColor(Color.GREEN)
            background = null
            backgroundTintList = null
            val border = GradientDrawable().apply {
                setStroke(4, Color.GREEN)
                cornerRadius = 50f
                setColor(Color.TRANSPARENT)
            }
            background = border
        }

        val radiusLayout = dialogView.findViewById<LinearLayout>(R.id.radius)
        radiusLayout.orientation = LinearLayout.HORIZONTAL
        radiusLayout.addView(expenseButton)
        radiusLayout.addView(incomeButton)

        incomeButton.setOnClickListener {
            handleTransaction(
                context = context,
                db = databaseHandler,
                tankList = tankList,
                accountList = accountList,
                tankSpinner = tankSpinner,
                accountSpinner = accountSpinner,
                descriptionEditText = descriptionEditText,
                tagsEditText = tagsEditText,
                amountEditText = amountEditText,
                bmlRefEditText = bmlRefEditText,
                bmlDateEditText = bmlDateEditText,
                transactionType = "income",
                onTransactionAdded = onTransactionAdded
            )
            alertDialog.dismiss()
        }

        expenseButton.setOnClickListener {
            handleTransaction(
                context = context,
                db = databaseHandler,
                tankList = tankList,
                accountList = accountList,
                tankSpinner = tankSpinner,
                accountSpinner = accountSpinner,
                descriptionEditText = descriptionEditText,
                tagsEditText = tagsEditText,
                amountEditText = amountEditText,
                bmlRefEditText = bmlRefEditText,
                bmlDateEditText = bmlDateEditText,
                transactionType = "expense",
                onTransactionAdded = onTransactionAdded
            )
            alertDialog.dismiss()
        }

    }

    private fun handleTransaction(
        context: Context,
        db: DatabaseHandler,
        tankList: List<UI_Tank>,
        accountList: List<Account>,
        tankSpinner: Spinner,
        accountSpinner: Spinner,
        descriptionEditText: EditText,
        tagsEditText: EditText,
        amountEditText: EditText,
        bmlRefEditText: EditText,
        bmlDateEditText: EditText,
        transactionType: String,
        onTransactionAdded: (TransactionData) -> Unit
    ) {
        // 1) Read & validate the inputs
        val amountText = amountEditText.text.toString().trim()
        val selectedTankName = tankSpinner.selectedItem?.toString() ?: ""
        val selectedAccountName = accountSpinner.selectedItem?.toString() ?: ""

        if (amountText.isEmpty() ||
            selectedTankName.isBlank() ||
            selectedAccountName.isBlank()
        ) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // 2) Parse the amount & apply sign
            var rawAmount = amountText.toDouble()
            if (transactionType == "expense") rawAmount = -rawAmount

            // 3) Get current date & format tags
            val currentDate = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                .format(Date())
            val tagsList = tagsEditText.text
                .toString()
                .trim()
                .split(" ")
                .map { it.replace("_", " ") }
            val formattedTags = tagsList.joinToString(",")

            // 4) Look up your UI objects
            val uiTank    = tankList.firstOrNull { it.name == selectedTankName }
            val uiAccount = accountList.firstOrNull{ it.name == selectedAccountName }
            if (uiTank == null || uiAccount == null) {
                Toast.makeText(context, "Tank or Account not found", Toast.LENGTH_SHORT).show()
                return
            }

            // 5) Fetch the matching DB_Tank so we can call updateTank(...) on it
            val dbTank = db.getAllTanks().firstOrNull { it.name == uiTank.name }
                ?: run {
                    Toast.makeText(context, "Database tank missing", Toast.LENGTH_SHORT).show()
                    return
                }
            val oldAllocation = dbTank.currentAllocation

            // 6) Build the TransactionData
            val tx = TransactionData(
                transactionId  = 0,
                accountId      = uiAccount.name,
                tankId         = dbTank.name,
                amount         = rawAmount,
                date           = currentDate,
                description    = descriptionEditText.text.toString().trim(),
                tag            = formattedTags,
                bmlReference   = bmlRefEditText.text.toString().trim(),
                bmlDate        = bmlDateEditText.text.toString().trim(),
                transactionType= transactionType
            )

            // 7) Try the DB insert first
            val newRow = db.insertTransaction(tx)
            if (newRow < 0) {
                Toast.makeText(context, "Failed to add transaction", Toast.LENGTH_SHORT).show()
                return
            }

            // 8) Only after success, update balances
            uiAccount.balance       += rawAmount

            db.updateTankAfterTransaction(dbTank, rawAmount)
            db.updateAccount(uiAccount)

            // 9) Tell the UI
            onTransactionAdded(tx)

        } catch (e: NumberFormatException) {
            Toast.makeText(context, "Enter a valid amount", Toast.LENGTH_SHORT).show()
        }
    }




}



