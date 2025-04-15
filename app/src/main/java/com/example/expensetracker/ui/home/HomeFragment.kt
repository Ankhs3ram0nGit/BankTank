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
            val rectangleView = createRectangleView(account.name, account.balance, account.color)
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

            if (accountName.isNotEmpty() && accountBalanceStr.isNotEmpty()) {
                try {
                    val accountBalance = accountBalanceStr.toDouble()
                    addAccount(accountName, accountBalance, accountColor)
                    alertDialog.dismiss()
                } catch (e: NumberFormatException) {
                    // Handle invalid number format for account balance
                    // Show error message or log the error
                }
            } else {
                // Show error or handle empty input
            }
        }
    }

    private fun addAccount(accountName: String, accountBalance: Double, accountColor: String) {
        // val color = if (accountColor.isNotEmpty()) accountColor else "DefaultColor"
        val accountColor = colorNameMap[accountColor] ?: "account_gray"

        // Create an account object
        val account = Account(accountName, accountBalance, accountColor)
        // account.color = color

        // Initialize DatabaseHandler instance (can be initialized elsewhere if required)
        val databaseHandler = DatabaseHandler(requireContext())

        // Insert the account into the database
        databaseHandler.insertData(account)

        // Update the UI with the new account view
        val accountContainer = binding.container
        val rectangleView = createRectangleView(accountName, accountBalance, accountColor)
        accountContainer.addView(rectangleView)
    }

    private fun createRectangleView(accountName: String, accountBalance: Double, accountColor: String): View {
        val inflater = LayoutInflater.from(requireContext())
        // Inflate the account rectangle layout which already has background styling
        val rectangleLayout = inflater.inflate(R.layout.account_rectangle, null) as ConstraintLayout

        // Set margins between items
        val layoutParams = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 0, 0, 32) // Adjust margins as needed
        rectangleLayout.layoutParams = layoutParams

        rectangleLayout.tag = accountName

        val nameTextView = rectangleLayout.findViewById<TextView>(R.id.accountNameText)
        val balanceTextView = rectangleLayout.findViewById<TextView>(R.id.accountBalanceText)
        val editButton = rectangleLayout.findViewById<Button>(R.id.editAccountButton)

        nameTextView.text = accountName
        balanceTextView.text = "$accountBalance"

        // Set text color if needed (optional)
        nameTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        balanceTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

        // Set background color dynamically based on accountColor (from colors.xml)
        val colorResId = resources.getIdentifier(accountColor, "color", requireContext().packageName)
        if (colorResId != 0) {
            // Set the background color using the predefined color from colors.xml
            val colorInt = ContextCompat.getColor(requireContext(), colorResId)
            // Instead of setting background color directly, set the drawable background dynamically if needed
            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.rectangle_background) as GradientDrawable
            drawable.setColor(colorInt)  // Set the color on the background drawable
            rectangleLayout.background = drawable
        } else {
            // Default to gray if the color name doesn't match any defined colors
            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.rectangle_background) as GradientDrawable
            drawable.setColor(ContextCompat.getColor(requireContext(), R.color.account_gray))
            rectangleLayout.background = drawable
        }

        // After setting the account background color
        val colorInt = if (colorResId != 0) {
            ContextCompat.getColor(requireContext(), colorResId)
        } else {
            ContextCompat.getColor(requireContext(), R.color.account_gray)
        }

        // Set button background color
        editButton.setBackgroundColor(colorInt)

        // Optional: Adjust text color for contrast (white text on dark background)
        editButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))


        editButton.setOnClickListener {
            showEditAccountDialog(accountName, accountBalance)
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



    private fun showEditAccountDialog(accountName: String, accountBalance: Double) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_account, null)
        val colorSpinner = dialogView.findViewById<Spinner>(R.id.AccountColor)
        val editBalance = dialogView.findViewById<EditText>(R.id.editBalance)
        val buttonSave = dialogView.findViewById<Button>(R.id.buttonSave)
        val buttonDelete = dialogView.findViewById<Button>(R.id.buttonDelete)

        editBalance.setText(accountBalance.toString())

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

            if (newBalance == null) {
                Toast.makeText(requireContext(), "Invalid balance entered", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            updateAccount(
                accountName = accountName,
                accountBalance = newBalance,
                newColorName = newColorName
            )

            alertDialog.dismiss()
            loadAccounts()
        }

        buttonDelete.setOnClickListener {
            deleteAccount(accountName)
            alertDialog.dismiss()
        }
    }



    private fun updateAccount(accountName: String, accountBalance: Double, newColorName: String) {
        val databaseHandler = DatabaseHandler(requireContext())

        // Step 1: Create updated account object with color STRING name
        val updatedAccount = Account(accountName, accountBalance, newColorName)
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
