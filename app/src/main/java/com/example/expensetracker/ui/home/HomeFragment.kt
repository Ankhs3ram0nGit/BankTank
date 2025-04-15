package com.example.expensetracker.ui.home

import com.example.expensetracker.Account
import com.example.expensetracker.DatabaseHandler
import android.app.AlertDialog
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.expensetracker.databinding.FragmentHomeBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.core.content.ContextCompat
import com.example.expensetracker.R

class HomeFragment : Fragment() {

    private lateinit var databaseHandler: DatabaseHandler
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val ACCOUNTS_KEY = "accountList"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize databaseHandler and sharedPreferences
        databaseHandler = DatabaseHandler(requireContext())

        // Load accounts from SharedPreferences or the database
        loadAccounts()

        // Floating action button to add new account
        val plusButton: FloatingActionButton = binding.plusButton
        plusButton.setOnClickListener {
            showAddAccountDialog()
        }

        return root
    }

    private fun loadAccounts() {
        // Clear existing views to prevent duplicates
        binding.container.removeAllViews()

        // Retrieve accounts from the database
        val accountList = databaseHandler.getAllAccounts()

        // Create rectangle views for each account
        accountList.forEach { account ->
            val rectangleView = createRectangleView(account.name, account.balance, account.color, account.currency, account.accountNumber)
            binding.container.addView(rectangleView)
        }
    }


    private fun showAddAccountDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_account, null)
        val editTextAccountName = dialogView.findViewById<EditText>(R.id.AccountName)
        val buttonCancel = dialogView.findViewById<Button>(R.id.buttonCancel)
        val buttonConfirm = dialogView.findViewById<Button>(R.id.buttonConfirm)
        val editTextAccountBalance = dialogView.findViewById<EditText>(R.id.AccountBalance)
        val editSpinnerAccountColor = dialogView.findViewById<Spinner>(R.id.AccountColor)
        val editTextAccountNumber = dialogView.findViewById<EditText>(R.id.AccountNumber)
        val editTextCurrency = dialogView.findViewById<Spinner>(R.id.CurrencySpinner)

        val dialogBuilder = AlertDialog.Builder(requireContext(), R.style.CustomDialogStyle)
            .setView(dialogView)
            .setCancelable(false)
        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        buttonConfirm.setOnClickListener {
            val accountName = editTextAccountName.text.toString().trim()
            val accountBalanceStr = editTextAccountBalance.text.toString().trim()
            val accountColor =  editSpinnerAccountColor.selectedItem.toString().trim()
            val accountNumber = editTextAccountNumber.text.toString().trim()
            val currency =  editTextCurrency.selectedItem.toString().trim()

            // Check if account number already exists in the database
            if (databaseHandler.getAccountByNumber(accountNumber) != null) {
                // Show error message and allow user to change account number
                Toast.makeText(requireContext(), "Account number already exists. Please choose a different number.", Toast.LENGTH_SHORT).show()
            } else if (accountName.isNotEmpty() && accountBalanceStr.isNotEmpty()) {
                try {
                    val accountBalance = accountBalanceStr.toDouble()
                    addAccount(accountName, accountBalance, accountColor, accountNumber, currency)
                    alertDialog.dismiss()  // Close the dialog if the account was added successfully
                } catch (e: NumberFormatException) {
                    // Handle invalid number format for account balance
                    // Show error message or log the error
                }
            } else {
                // Show error or handle empty input
            }
        }

    }

    private fun addAccount(accountName: String, accountBalance: Double, accountColor: String, accountNumber: String, currency: String) {
        // Check if account with the same accountNumber already exists
        val existingAccount = databaseHandler.getAccountByNumber(accountNumber)

        if (existingAccount != null) {
            // Account already exists, show an error message
            Toast.makeText(requireContext(), "Account with this number already exists. Please alter it.", Toast.LENGTH_SHORT).show()
            return // Don't proceed with adding the account
        }

        // Continue if the account doesn't exist
        val accountColor = colorNameMap[accountColor] ?: "account_gray"

        // Create an account object
        val account = Account(accountName, accountBalance, accountColor, accountNumber, currency)

        // Insert the account into the database
        databaseHandler.insertData(account)

        // Update the UI with the new account view
        val accountContainer = binding.container
        val rectangleView = createRectangleView(accountName, accountBalance, accountColor, accountNumber, currency)
        accountContainer.addView(rectangleView)
        loadAccounts()
    }


    private fun createRectangleView(accountName: String, accountBalance: Double, accountColor: String, currency: String, accountNumber: String): View {
        val inflater = LayoutInflater.from(requireContext())
        val rectangleLayout = inflater.inflate(R.layout.account_rectangle, null) as ConstraintLayout

        val layoutParams = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 0, 0, 32)
        rectangleLayout.layoutParams = layoutParams

        rectangleLayout.tag = accountName

        val nameTextView = rectangleLayout.findViewById<TextView>(R.id.accountNameText)
        val balanceTextView = rectangleLayout.findViewById<TextView>(R.id.accountBalanceText)
        val editButton = rectangleLayout.findViewById<Button>(R.id.editAccountButton)

        nameTextView.text = accountName
        balanceTextView.text = "$currency $accountBalance"

        nameTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        balanceTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

        val colorResId = resources.getIdentifier(accountColor, "color", requireContext().packageName)
        if (colorResId != 0) {
            val colorInt = ContextCompat.getColor(requireContext(), colorResId)
            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.rectangle_background) as GradientDrawable
            drawable.setColor(colorInt)
            rectangleLayout.background = drawable
        } else {
            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.rectangle_background) as GradientDrawable
            drawable.setColor(ContextCompat.getColor(requireContext(), R.color.account_gray))
            rectangleLayout.background = drawable
        }

        val colorInt = if (colorResId != 0) {
            ContextCompat.getColor(requireContext(), colorResId)
        } else {
            ContextCompat.getColor(requireContext(), R.color.account_gray)
        }

        editButton.setBackgroundColor(colorInt)
        editButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

        editButton.setOnClickListener {
            showEditAccountDialog(accountName, accountBalance, accountNumber, currency)
        }


        return rectangleLayout
    }


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



    private fun showEditAccountDialog(accountName: String, accountBalance: Double,  accountNumber: String, currency: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_account, null)
        val colorSpinner = dialogView.findViewById<Spinner>(R.id.AccountColor)
        val editBalance = dialogView.findViewById<EditText>(R.id.editBalance)
        val editAccountNumber = dialogView.findViewById<EditText>(R.id.AccountNumber)
        val buttonSave = dialogView.findViewById<Button>(R.id.buttonSave)
        val buttonDelete = dialogView.findViewById<Button>(R.id.buttonDelete)

        editBalance.setText(accountBalance.toString())
        editAccountNumber.setText(accountNumber)

        val dialogBuilder = AlertDialog.Builder(requireContext(), R.style.CustomDialogStyle)
            .setView(dialogView)
            .setCancelable(false)
        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        alertDialog.setOnKeyListener { dialog, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                dialog.dismiss()
                true
            } else {
                false
            }
        }


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

        buttonSave.setOnClickListener {
            val selectedColorDisplayName = colorSpinner.selectedItem.toString()
            val newColorName = colorNameMap[selectedColorDisplayName] ?: "account_gray"
            val newBalance = editBalance.text.toString().toDoubleOrNull()
            val newAccountNumber = editAccountNumber.text.toString().trim()

            // Ensure balance is valid
            if (newBalance == null) {
                Toast.makeText(requireContext(), "Invalid balance entered", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if the new account number already exists (but not for the current account)
            val existingAccount = databaseHandler.getAccountByNumber(newAccountNumber)
            if (existingAccount != null && existingAccount.accountNumber != accountNumber) {
                // Show error message and don't proceed
                Toast.makeText(requireContext(), "Account number already exists. Please choose a different number.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Proceed with the update if everything is valid
            updateAccount(
                accountName = accountName,
                accountBalance = newBalance,
                newColorName = newColorName,
                accountNumber = newAccountNumber,
                currency = currency
            )

            alertDialog.dismiss()
            loadAccounts()  // Reload accounts to reflect changes
        }


        buttonDelete.setOnClickListener {
            deleteAccount(accountName)
            alertDialog.dismiss()
        }
    }



    private fun updateAccount(accountName: String, accountBalance: Double, newColorName: String, accountNumber: String, currency: String) {
        val databaseHandler = DatabaseHandler(requireContext())

        // Step 1: Create updated account object with color STRING name
        val updatedAccount = Account(accountName, accountBalance, newColorName, accountNumber, currency)
        databaseHandler.updateAccount(updatedAccount)

        // Step 2: Find and update the UI element (rectangle) by tag
        val accountContainer = binding.container
        val rectangleView = accountContainer.findViewWithTag<View>(accountName)

        rectangleView?.let {
            val nameTextView = it.findViewById<TextView>(R.id.accountNameText)
            val balanceTextView = it.findViewById<TextView>(R.id.accountBalanceText)

            nameTextView.text = accountName
            balanceTextView.text = "$accountBalance"
            it.tag = accountName

            // Step 3: Convert color name to actual color int and apply it
            val colorResId = resources.getIdentifier(newColorName, "color", requireContext().packageName)
            val colorInt = if (colorResId != 0)
                ContextCompat.getColor(requireContext(), colorResId)
            else
                ContextCompat.getColor(requireContext(), R.color.account_gray)

            val backgroundDrawable = GradientDrawable()
            backgroundDrawable.cornerRadius = 20f * resources.displayMetrics.density
            backgroundDrawable.setColor(colorInt)
            it.background = backgroundDrawable
        }
    }







    private fun updateAccount(accountName: String) {
        databaseHandler.getAccountByName(accountName)

    }
    private fun deleteAccount(accountName: String) {
        val databaseHandler = DatabaseHandler(requireContext())

        // Attempt to delete the account and check if it was successful
        val wasDeleted = databaseHandler.deleteAccount(accountName)

        if (wasDeleted) {
            // Account was deleted, refresh the UI to show updated account list
            loadAccounts()
            Toast.makeText(requireContext(), "Account deleted", Toast.LENGTH_SHORT).show()
        } else {
            // Failed to delete account, likely the account wasn't found
            Toast.makeText(requireContext(), "Failed to delete account", Toast.LENGTH_SHORT).show()
        }
    }



}
