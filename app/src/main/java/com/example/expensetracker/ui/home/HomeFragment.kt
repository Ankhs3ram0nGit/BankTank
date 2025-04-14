package com.example.expensetracker.ui.home

import com.example.expensetracker.Account
import com.example.expensetracker.DatabaseHandler
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.example.expensetracker.databinding.FragmentHomeBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.core.content.ContextCompat
import com.example.expensetracker.R

class HomeFragment : Fragment() {

    private lateinit var databaseHandler: DatabaseHandler
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences
    private val accountList = mutableListOf<String>()
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
        sharedPreferences = requireActivity().getSharedPreferences("AccountData", Context.MODE_PRIVATE)

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

    private fun saveAccounts() {
        // Convert accountList to a set and save it to SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putStringSet(ACCOUNTS_KEY, accountList.toSet())
        editor.apply()
    }

    private fun showAddAccountDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_account, null)
        val editTextAccountName = dialogView.findViewById<EditText>(R.id.AccountName)
        val buttonCancel = dialogView.findViewById<Button>(R.id.buttonCancel)
        val buttonConfirm = dialogView.findViewById<Button>(R.id.buttonConfirm)
        val editTextAccountBalance = dialogView.findViewById<EditText>(R.id.AccountBalance)

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

            if (accountName.isNotEmpty() && accountBalanceStr.isNotEmpty()) {
                try {
                    val accountBalance = accountBalanceStr.toDouble()
                    addAccount(accountName, accountBalance)
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

    private fun addAccount(accountName: String, accountBalance: Double, accountColor: String = "Gray") {
        val color = if (accountColor.isNotEmpty()) accountColor else "DefaultColor"

        // Create an account object
        val account = Account(accountName, accountBalance, accountColor)
        account.color = color

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

        editButton.setOnClickListener {
            showEditAccountDialog(accountName, accountBalance)
        }

        return rectangleLayout
    }




    private fun showEditAccountDialog(accountName: String, accountBalance: Double) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_account, null)
        val editTextNewName = dialogView.findViewById<EditText>(R.id.editTextNewName)
        val colorSpinner = dialogView.findViewById<Spinner>(R.id.colorSpinner)
        val buttonSave = dialogView.findViewById<Button>(R.id.buttonSave)
        val buttonDelete = dialogView.findViewById<Button>(R.id.buttonDelete)

        // Set default values
        editTextNewName.setText(accountName)

        val dialogBuilder = AlertDialog.Builder(requireContext(), R.style.CustomDialogStyle)
            .setView(dialogView)
            .setCancelable(false)
        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        buttonSave.setOnClickListener {
            val newName = editTextNewName.text.toString().trim()

            val newColor = when (colorSpinner.selectedItemPosition) {
                0 -> ContextCompat.getColor(requireContext(), R.color.account_green)
                1 -> ContextCompat.getColor(requireContext(), R.color.account_red)
                2 -> ContextCompat.getColor(requireContext(), R.color.account_yellow)
                3 -> ContextCompat.getColor(requireContext(), R.color.account_gray)
                4 -> ContextCompat.getColor(requireContext(), R.color.account_forest_green)
                5 -> ContextCompat.getColor(requireContext(), R.color.account_mighty_purple)
                6 -> ContextCompat.getColor(requireContext(), R.color.account_purple)
                7 -> ContextCompat.getColor(requireContext(), R.color.account_seal_blue)
                8 -> ContextCompat.getColor(requireContext(), R.color.account_royal_blue)
                else -> ContextCompat.getColor(requireContext(), R.color.account_gray)
            }

            if (newName.isNotEmpty()) {
                val accountView = findRectangleView(accountName)
                updateAccount(accountName, newName, newColor, accountView)
                alertDialog.dismiss()
            }
        }

        buttonDelete.setOnClickListener {
            deleteAccount(accountName)
            alertDialog.dismiss()
        }
    }

    private fun updateAccount(oldName: String, newName: String, newColor: Int, rectangleView: View) {
        // Update name text
        val nameTextView = rectangleView.findViewById<TextView>(R.id.accountNameText)
        nameTextView.text = newName

        // Change background color
        val backgroundDrawable = GradientDrawable()
        backgroundDrawable.cornerRadius = 20f * resources.displayMetrics.density // 20dp in px
        backgroundDrawable.setColor(newColor)
        rectangleView.background = backgroundDrawable

        // Update tag
        rectangleView.tag = newName

        // Update the list
        val index = accountList.indexOf(oldName)
        if (index != -1) {
            accountList[index] = newName
        }
    }

    private fun findRectangleView(accountName: String): View {
        // Iterate through the child views of the container layout to find the rectangle view with the specified account name
        for (i in 0 until binding.container.childCount) {
            val childView = binding.container.getChildAt(i)
            if (childView.tag == accountName) {
                return childView
            }
        }
        throw IllegalArgumentException("Rectangle view not found for account: $accountName")
    }

    private fun deleteAccount(accountName: String) {
        // Delete the account from the database
        databaseHandler.deleteAccount(accountName)

        // Remove from in-memory list
        accountList.remove(accountName)

        // Refresh the UI
        loadAccounts()

        // Save updated account list to SharedPreferences
        saveAccounts()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
