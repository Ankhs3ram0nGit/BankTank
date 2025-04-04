package com.example.expensetracker.ui.home

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.expensetracker.databinding.FragmentHomeBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.Spinner
import com.example.expensetracker.R


class HomeFragment : Fragment() {

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

        sharedPreferences = requireActivity().getSharedPreferences("AccountData", Context.MODE_PRIVATE)
        // Load accounts from SharedPreferences
        loadAccounts()

        val plusButton: FloatingActionButton = binding.plusButton
        plusButton.setOnClickListener {
            showAddAccountDialog()
        }

        return root
    }

    private fun loadAccounts() {
        val savedAccounts = sharedPreferences.getStringSet(ACCOUNTS_KEY, setOf()) ?: setOf()
        accountList.clear()
        accountList.addAll(savedAccounts)
        // Create rectangle views for each saved account
        accountList.forEach { accountName ->
            createRectangleView(accountName, 0.0) // You can set balance to 0 or retrieve from storage if available
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

    private fun addAccount(accountName: String, accountBalance: Double) {
        accountList.add(accountName)
        saveAccounts() // Save updated account list
        // Create and add a new rectangle for the account
        val rectangleView = createRectangleView(accountName, accountBalance)
        binding.container.addView(rectangleView)
    }

    private fun createRectangleView(accountName: String, accountBalance: Double): View {
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            250 // Set the height to 250 pixels
        )
        val rectangleLayout = LinearLayout(requireContext())
        layoutParams.setMargins(5, 10, 5, 0) // Set margins (left, top, right, bottom)
        rectangleLayout.layoutParams = layoutParams
        rectangleLayout.orientation = LinearLayout.VERTICAL
        rectangleLayout.background = requireContext().getDrawable(R.drawable.rectangle_background) // Set background drawable for the rectangle
        val nameTextView = TextView(requireContext())
        nameTextView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        nameTextView.text = accountName
        nameTextView.textSize = 20f // Set text size for account name
        nameTextView.setPadding(16, 16, 16, 0) // Add padding to the text
        rectangleLayout.addView(nameTextView)
        rectangleLayout.tag = accountName
        val balanceTextView = TextView(requireContext())
        balanceTextView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        balanceTextView.text = "$accountBalance"
        balanceTextView.textSize = 16f // Set text size for account balance
        balanceTextView.setPadding(16, 8, 16, 16) // Add padding to the text
        rectangleLayout.addView(balanceTextView)

        // Create edit button
        val editButton = Button(requireContext())
        editButton.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        editButton.text = "Edit"
        editButton.setOnClickListener {
            showEditAccountDialog(accountName, accountBalance)
        }
        rectangleLayout.addView(editButton)

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
                0 -> Color.RED
                1 -> Color.GREEN
                2 -> Color.BLUE
                3 -> Color.YELLOW
                else -> Color.WHITE // Default color
            }


            // Update account name and color
            if (newName.isNotEmpty()) {
                updateAccount(accountName, newName, newColor)
                alertDialog.dismiss()
            } else {
                // Show error or handle empty input
            }
        }

        buttonDelete.setOnClickListener {
            // Delete account
            deleteAccount(accountName)
            alertDialog.dismiss()
        }
    }

    private fun updateAccount(oldName: String, newName: String, newColor: Int) {
        // Find the rectangle view associated with the old account name
        val rectangleView = findRectangleView(oldName)

        // Update the account name text
        val nameTextView = rectangleView.findViewById<TextView>(R.id.editTextNewName)
        nameTextView.text = newName

        // Update the background color of the rectangle layout
        rectangleView.setBackgroundColor(newColor)

        // Update the account name in the account list
        val index = accountList.indexOf(oldName)
        if (index != -1) {
            accountList[index] = newName
        }

        // Save the updated account list
        saveAccounts()
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
        // Remove the account from the list
        accountList.remove(accountName)
        // Remove the rectangle view from the container layout
        val rectangleView = findRectangleView(accountName)
        binding.container.removeView(rectangleView)

        // Save the updated account list
        saveAccounts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
