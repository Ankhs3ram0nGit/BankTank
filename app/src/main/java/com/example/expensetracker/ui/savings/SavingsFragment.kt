package com.example.expensetracker.ui.savings

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.example.expensetracker.DB_Tank
import com.example.expensetracker.savingsGoal
import com.example.expensetracker.DatabaseHandler
import com.example.expensetracker.R
import com.example.expensetracker.databinding.FragmentSavingsBinding

class SavingsFragment : Fragment() {

    private lateinit var databaseHandler: DatabaseHandler

    private var _binding: FragmentSavingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        databaseHandler = DatabaseHandler(requireContext())
        _binding = FragmentSavingsBinding.inflate(inflater, container, false)
        // For Systems Dev Module
        //binding.plusButton.setOnClickListener {
        //    showAddSavingsGoalDialog()
        //}
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val container = binding.container
        // loadSavingsGoals()
        val savingsTank = databaseHandler.getAllTanks()
            .firstOrNull { it.name.equals("Savings", true) }
        if (savingsTank != null) {
            addSavingsRectangle(container, savingsTank)
        }    }

    private fun showAddSavingsGoalDialog() {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.add_savings_goal, null)

        val editTextGoalName = dialogView.findViewById<EditText>(R.id.GoalTitle)
        val editTextTargetAmount = dialogView.findViewById<EditText>(R.id.GoalAmount)
        val buttonCancel = dialogView.findViewById<Button>(R.id.buttonCancel)
        val buttonConfirm = dialogView.findViewById<Button>(R.id.buttonConfirm)

        val dialogBuilder = AlertDialog.Builder(requireContext(), R.style.CustomDialogStyle)
            .setView(dialogView)
            .setCancelable(true)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        buttonConfirm.setOnClickListener {
            val goalName = editTextGoalName.text.toString().trim()
            val targetAmountStr = editTextTargetAmount.text.toString().trim()

            val goal = targetAmountStr.toDouble()

            if (goalName.isNotEmpty() && targetAmountStr.isNotEmpty()) {
                try {
                    val targetAmount = targetAmountStr.toDouble()
                    alertDialog.dismiss()
                } catch (e: NumberFormatException) {
                    Toast.makeText(
                        requireContext(),
                        "Invalid target amount",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT)
                    .show()
            }

            addGoal(goalName, goal)
        }
    }

    private fun loadSavingsGoals() {
        val container = binding.container
        // remove everything *after* index 0
        while (container.childCount > 1) {
            container.removeViewAt(1)
        }
        // now add goals below your header
        val savingsList = databaseHandler.getAllSavingsGoals()
        for (goal in savingsList) {
            createRectangleViewGoals(goal)
        }
    }


    private fun addSavingsRectangle(
        container: LinearLayout,
        savingsTank: DB_Tank?
    ) {
        val view = LayoutInflater.from(container.context)
            .inflate(R.layout.savings_tank_rectangle, container, false)

        val balanceText = view.findViewById<TextView>(R.id.savingsBalance)
        balanceText.text = "${savingsTank?.maxAllocation?.toDouble() ?: 0.00}"

        // Make the rectangle clickable

        val clickableRectangle = view.findViewById<ConstraintLayout>(R.id.savings_goal_rectangle)
        clickableRectangle.setOnClickListener {
            Log.d("DialogCheck", "Clicked savings rectangle")
            showAddTransactionDialog(requireContext())
        }

        container.addView(view, 0)
    }


    private fun addGoal(title: String, goal: Double) {
        val existingGoal = databaseHandler.getSavingsGoalByTitle(title)

        if (existingGoal != null) {
            Toast.makeText(
                requireContext(),
                "Goal with this title already exists.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val newGoal = savingsGoal(title = title, goal = goal, funds = 0.0)
        databaseHandler.insertNewSavingsGoal(newGoal)

        val inflater = LayoutInflater.from(requireContext())
        val savingsCardView = inflater.inflate(R.layout.savings_goal_rectangle, null)

        val goalTitleText = savingsCardView.findViewById<TextView>(R.id.transactionDescription)
        val goalAmountText = savingsCardView.findViewById<TextView>(R.id.transactionAmount)

        goalTitleText.text = title
        goalAmountText.text = "0/${goal.toInt()}"

        val container = view?.findViewById<LinearLayout>(R.id.container)
        container?.addView(savingsCardView)
    }

    private fun createRectangleViewGoals(newGoal: savingsGoal) {
        val inflater = LayoutInflater.from(requireContext())
        val savingsCardView = inflater.inflate(R.layout.savings_goal_rectangle, null)

        val goalTitleText = savingsCardView.findViewById<TextView>(R.id.transactionDescription)
        val goalAmountText = savingsCardView.findViewById<TextView>(R.id.transactionAmount)

        goalTitleText.text = newGoal.title
        goalAmountText.text = "${newGoal.funds.toInt()}/${newGoal.goal.toInt()}"

        val container = view?.findViewById<LinearLayout>(R.id.container)
        container?.addView(savingsCardView)
    }
    private fun refreshSavingsUI() {
        val container = binding.container
        container.removeAllViews()

        val updatedSavingsTank = databaseHandler.getAllTanks()
            .firstOrNull { it.name.equals("Savings", true) }
        if (updatedSavingsTank != null) {
            addSavingsRectangle(container, updatedSavingsTank)
        }

        loadSavingsGoals()
    }

    fun showAddTransactionDialog(context: Context) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.savings_transfer, null)

        val dialogBuilder = AlertDialog.Builder(context, R.style.CustomDialogStyle)
            .setView(dialogView)
            .setCancelable(true)

        val alertDialog = dialogBuilder.create()
        Log.d("DialogCheck", "About to show dialog")
        alertDialog.show()

        val accountSpinner = dialogView.findViewById<Spinner>(R.id.Account)
        val databaseHandler = DatabaseHandler(context)
        val accountList = databaseHandler.getAllAccounts()
        val accountNames = accountList.map { it.name }
        val amountEditText = dialogView.findViewById<EditText>(R.id.Amount)
        val accountAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, accountNames)
        accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        accountSpinner.adapter = accountAdapter

        // Create Transfer button
        val addButton = Button(context).apply {
            text = "Add"
            setTextColor(Color.GREEN)
            background = null
            backgroundTintList = null
            background = GradientDrawable().apply {
                setStroke(4, Color.GREEN)
                cornerRadius = 50f
                setColor(Color.TRANSPARENT)
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = 32
            }
        }



        // Create Withdraw button
        val withdrawButton = Button(context).apply {
            text = "Withdraw"
            setTextColor(Color.RED)
            background = null
            background = null
            backgroundTintList = null
            background = GradientDrawable().apply {
                setStroke(4, Color.RED)
                cornerRadius = 50f
                setColor(Color.TRANSPARENT)
            }
        }

        // Add buttons to dialog layout
        val radiusLayout = dialogView.findViewById<LinearLayout>(R.id.radius)
        radiusLayout.orientation = LinearLayout.HORIZONTAL
        radiusLayout.addView(addButton)
        radiusLayout.addView(withdrawButton)

        // Button listeners
        addButton.setOnClickListener {
            val selectedAccount = accountSpinner.selectedItem as String
            val amount = amountEditText.text.toString().toDoubleOrNull()

            if (amount != null && amount > 0) {
                databaseHandler.processSavingsTransaction("add", selectedAccount, amount)
            }
            refreshSavingsUI()
            alertDialog.dismiss()
        }

        withdrawButton.setOnClickListener {
            val selectedAccount = accountSpinner.selectedItem as String
            val amount = amountEditText.text.toString().toDoubleOrNull()

            if (amount != null && amount > 0) {
                databaseHandler.processSavingsTransaction("withdraw", selectedAccount, amount)
            }
            refreshSavingsUI()
            alertDialog.dismiss()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
