package com.example.expensetracker.ui.dashboard

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
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.Account
import com.example.expensetracker.DatabaseHandler
import com.example.expensetracker.R
import com.example.expensetracker.TransactionData
import com.example.expensetracker.ui.home.TransactionAdapter
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TankDetailsFragment : Fragment() {
    private lateinit var databaseHandler: DatabaseHandler

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.tank_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) Pull your arguments
        val name = arguments?.getString("name") ?: "Unnamed Tank"
        val colorDisplayName = arguments?.getString("color") ?: "Gray"
        val allocation = arguments?.getDouble("allocation") ?: 0.0
        val currentAllocation = arguments?.getDouble("currentAllocation") ?: 0.0


        val dbHandler = DatabaseHandler(requireContext())
        val transactions = dbHandler.getAllTransactionsForTank(name)


        // 2) Wire up your text fields
        view.findViewById<TextView>(R.id.tankNameText).text = name
        view.findViewById<TextView>(R.id.tankBalanceText).text =
            "${currentAllocation.toInt()} / ${allocation.toInt()}"

        // 3) Map display names → actual color resource names
        val colorNameMap = mapOf(
            "Green"         to "account_green",
            "Red"           to "account_red",
            "Yellow"        to "account_yellow",
            "Gray"          to "account_gray",
            "Forest Green"  to "account_forest_green",
            "Mighty Purple" to "account_mighty_purple",
            "Purple"        to "account_purple",
            "Seal Blue"     to "account_seal_blue",
            "Royal Blue"    to "account_royal_blue"
        )

        // 4) Resolve the R.color ID
        val resourceName = colorNameMap[colorDisplayName] ?: "account_gray"
        val colorResId = resources.getIdentifier(
            resourceName, "color", requireContext().packageName
        )

        // 5) If found, get the actual int color; else fallback gray
        val tankColorInt = if (colorResId != 0)
            ContextCompat.getColor(requireContext(), colorResId)
        else
            ContextCompat.getColor(requireContext(), R.color.account_gray)

        // 6) Apply outline color to your MaterialCardView
        val tankCard = view.findViewById<MaterialCardView>(R.id.accountCard)
        tankCard.setStrokeColor(tankColorInt)

        // 7) Convert that int color into a hex string for updateTankFill
        val hexColor = String.format("#%06X", 0xFFFFFF and tankColorInt)

        // 8) Now update your horizontal fill bar
        updateTankFill(
            view        = view,
            current     = currentAllocation.toInt(),
            max         = allocation.toInt(),
            colorHex    = hexColor
        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.tanksTransactionsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = TransactionAdapter(transactions) { transaction ->
            showTransactionDetailsDialog(transaction)
        }

        val addTransactionButton = view.findViewById<Button>(R.id.addTransactionButton)
        addTransactionButton.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(requireContext(), android.R.color.white)
        )

        addTransactionButton.setOnClickListener {
            showAddTransactionDialogForTank(requireContext(), name) { transaction ->
                val updatedTransactions = dbHandler.getAllTransactionsForAccount(name)
                recyclerView.adapter = TransactionAdapter(updatedTransactions) { updatedTransaction ->
                    showTransactionDetailsDialog(updatedTransaction)
                }
            }
        }
    }

    fun showAddTransactionDialogForTank(
        context: Context,
        selectedTankName: String?,
        onTransactionAdded: (TransactionData) -> Unit
    ) {
        // 1) inflate your shared "add_transaction" layout
        val dialogView = LayoutInflater.from(context)
            .inflate(R.layout.add_transaction, null)

        val descriptionEditText = dialogView.findViewById<EditText>(R.id.Description)
        val tagsEditText        = dialogView.findViewById<EditText>(R.id.Tags)
        val tankSpinner         = dialogView.findViewById<Spinner>(R.id.Tank)
        val accountSpinner      = dialogView.findViewById<Spinner>(R.id.Account)
        val amountEditText      = dialogView.findViewById<EditText>(R.id.Amount)
        val bmlRefEditText      = dialogView.findViewById<EditText>(R.id.BML_Reference)
        val bmlDateEditText     = dialogView.findViewById<EditText>(R.id.BML_Date)

        val databaseHandler = DatabaseHandler(context)

        // 2) Populate TANK spinner and auto‐select this fragment's tank
        val tankList   = databaseHandler.getAllTanksUI()
        val tankNames  = tankList.map { it.name }
        val tankAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, tankNames)
        tankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        tankSpinner.adapter = tankAdapter

        selectedTankName?.let { name ->
            val idx = tankNames.indexOf(name)
            if (idx >= 0) tankSpinner.setSelection(idx)
        }

        // 3) Populate ACCOUNT spinner normally
        val accountList  = databaseHandler.getAllAccounts()
        val accountNames = accountList.map { it.name }
        val accountAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, accountNames)
        accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        accountSpinner.adapter = accountAdapter

        // 4) Build & show the dialog
        val alertDialog = AlertDialog.Builder(context, R.style.CustomDialogStyle)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        alertDialog.show()

        // 5) Expense / Income buttons
        val expenseButton = Button(context).apply {
            text = "Expense"
            setTextColor(Color.RED)
            background = null
            val border = GradientDrawable().apply {
                setStroke(4, Color.RED); cornerRadius = 50f; setColor(Color.TRANSPARENT)
            }
            background = border
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = 32 }
        }
        val incomeButton = Button(context).apply {
            text = "Income"
            setTextColor(Color.GREEN)
            background = null
            val border = GradientDrawable().apply {
                setStroke(4, Color.GREEN); cornerRadius = 50f; setColor(Color.TRANSPARENT)
            }
            background = border
        }
        val radiusLayout = dialogView.findViewById<LinearLayout>(R.id.radius)
        radiusLayout.orientation = LinearLayout.HORIZONTAL
        radiusLayout.addView(expenseButton)
        radiusLayout.addView(incomeButton)

        // 6) Wire up clicks
        expenseButton.setOnClickListener {
            handleTransaction(
                context           = context,
                db                = databaseHandler,
                tankList          = tankList,
                accountList       = accountList,
                tankSpinner       = tankSpinner,
                accountSpinner    = accountSpinner,
                descriptionEditText = descriptionEditText,
                tagsEditText      = tagsEditText,
                amountEditText    = amountEditText,
                bmlRefEditText    = bmlRefEditText,
                bmlDateEditText   = bmlDateEditText,
                transactionType   = "expense",
                onTransactionAdded = onTransactionAdded
            )
            alertDialog.dismiss()
        }
        incomeButton.setOnClickListener {
            handleTransaction(
                context           = context,
                db                = databaseHandler,
                tankList          = tankList,
                accountList       = accountList,
                tankSpinner       = tankSpinner,
                accountSpinner    = accountSpinner,
                descriptionEditText = descriptionEditText,
                tagsEditText      = tagsEditText,
                amountEditText    = amountEditText,
                bmlRefEditText    = bmlRefEditText,
                bmlDateEditText   = bmlDateEditText,
                transactionType   = "income",
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
    private fun updateTankFill(
        view: View,
        current: Int,
        max: Int,
        colorHex: String
    ) {
        val sharedPrefs = requireContext()
            .getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val invertFill = sharedPrefs.getBoolean("invert_fill", false)

        val rawRatio = if (max > 0) current.toFloat() / max else 0f
        val fillRatio = if (invertFill) 1f - rawRatio else rawRatio

        val container = view.findViewById<FrameLayout>(R.id.tankFillContainer)
        val fillView  = view.findViewById<View>(R.id.tankFill)

        container.post {
            val totalWidth = container.width
            val fillWidth  = (totalWidth * fillRatio).toInt()

            // 1) Set container background (light gray, rounded corners)
            val containerDrawable = GradientDrawable().apply {
                shape        = GradientDrawable.RECTANGLE
                setColor(Color.parseColor("#D3D3D3")) // light gray
                cornerRadius = 24f
            }
            container.background = containerDrawable

            // 2) Resize the fill view
            fillView.layoutParams.width = fillWidth
            fillView.requestLayout()

            // 3) Set fill background (tank color, rounded corners)
            val fillDrawable = GradientDrawable().apply {
                shape        = GradientDrawable.RECTANGLE
                setColor(Color.parseColor(colorHex))
                cornerRadius = 24f
            }
            fillView.background = fillDrawable
        }
    }
}
