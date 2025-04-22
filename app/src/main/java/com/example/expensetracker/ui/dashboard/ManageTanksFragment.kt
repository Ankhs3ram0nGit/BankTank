package com.example.expensetracker.ui.dashboard

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.DatabaseHandler
import com.example.expensetracker.R
import com.example.expensetracker.databinding.FragmentManageTanksBinding

class ManageTanksFragment : Fragment() {

    private lateinit var databaseHandler: DatabaseHandler
    private lateinit var maxAllocationInput: EditText
    private lateinit var tanksRecyclerView: RecyclerView
    private lateinit var manageButton: Button
    private lateinit var addTankButton: Button
    private lateinit var tanksAdapter: TanksAdapter
    private val tanksList = mutableListOf<UI_Tank>()
    private var maxAllocation: Double = 0.0 // Declare maxAllocation here

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentManageTanksBinding.inflate(inflater, container, false)
        databaseHandler = DatabaseHandler(requireContext())

        // Initialize views
        maxAllocationInput = binding.maxAllocationInput
        tanksRecyclerView = binding.tanksRecyclerView
        tanksAdapter = TanksAdapter(tanksList, ::onEditTank)

        tanksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        tanksRecyclerView.adapter = tanksAdapter

        // Set up the button to handle the manage button click
        manageButton = binding.manageButton
        manageButton.setOnClickListener { saveMaxAllocation() }

        // Set up the Add Tank button
        addTankButton = binding.addTankButton
        addTankButton.setOnClickListener { showAddTankDialog() }

        // Load existing tanks
        loadTanks()

        // Load and set the saved Max Allocation value
        val dbHandler = DatabaseHandler(requireContext())
        val savedMaxAllocation = dbHandler.getMaxAllocation()  // This method should return the saved max allocation value

        // Populate the max allocation field with the saved value
        if (savedMaxAllocation != null) {
            maxAllocationInput.setText(savedMaxAllocation.toString())
        }


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            }
        )



        return binding.root
    }


    // Save max allocation and update the maxAllocation variable
    private fun saveMaxAllocation() {
        val inputText = maxAllocationInput.text.toString()
        val allocation = inputText.toDoubleOrNull()

        if (allocation != null) {
            val dbHandler = DatabaseHandler(requireContext())

            // Save the max allocation value to the MaxAllocation table (upsert)
            dbHandler.updateMaxAllocation(allocation)


            // Show the saved allocation in the EditText
            maxAllocationInput.setText(allocation.toString())

            Toast.makeText(requireContext(), "Max Allocation saved: $allocation", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Invalid allocation value", Toast.LENGTH_SHORT).show()
        }
    }




    // Function to show the dialog to add a new tank
    private fun showAddTankDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_tank, null)
        val tankNameEditText: EditText = dialogView.findViewById(R.id.addTankName)
        val tankColorSpinner: Spinner = dialogView.findViewById(R.id.addTankColor)
        val tankCapacityEditText: EditText = dialogView.findViewById(R.id.addTankAllocation)
        val buttonSave: Button = dialogView.findViewById(R.id.buttonConfirm)
        val buttonCancel: Button = dialogView.findViewById(R.id.buttonDeleteTank)

        // Set the default capacity to remaining allocation
        tankCapacityEditText.setText(databaseHandler.getRemainingAllocation().toString())

        // Create and show dialog
        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomDialogStyle)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        buttonSave.setOnClickListener {
            val name = tankNameEditText.text.toString().trim()
            val color = tankColorSpinner.selectedItem.toString()
            val capacityString = tankCapacityEditText.text.toString().trim()

            val capacity = capacityString.toDoubleOrNull()

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Tank name is required", Toast.LENGTH_SHORT).show()
            } else if (capacity == null) {
                Toast.makeText(requireContext(), "Invalid capacity", Toast.LENGTH_SHORT).show()
            } else if (capacity > databaseHandler.getRemainingAllocation() || capacity < 1) {
                Toast.makeText(requireContext(), "Exceeded available allocation", Toast.LENGTH_SHORT).show()
            } else {
                // Save the tank data (Add to your data source or database)
                saveTank(name, capacity, color)
                dialog.dismiss() // Close the dialog when everything is valid
            }
        }

        buttonCancel.setOnClickListener {
            dialog.dismiss() // Close dialog if the user cancels
        }

        dialog.show()
    }

    private fun showEditTankDialog(tankName: String, tankAllocation: Double, tankList: List<UI_Tank>) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_tank, null)
        val tankNameEditText: EditText = dialogView.findViewById(R.id.editTankName)
        val tankColorSpinner: Spinner = dialogView.findViewById(R.id.editTankColor)
        val tankCapacityEditText: EditText = dialogView.findViewById(R.id.editTankAllocation)
        val buttonSave: Button = dialogView.findViewById(R.id.buttonConfirm)
        val buttonDelete: Button = dialogView.findViewById(R.id.buttonDeleteTank)

        // Set the default capacity to assigned allocation
        tankCapacityEditText.setText(tankAllocation.toString())
        tankNameEditText.setText(tankName)

        // Create and show dialog
        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomDialogStyle)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        buttonSave.setOnClickListener {
            val name = tankNameEditText.text.toString().trim()
            val color = tankColorSpinner.selectedItem.toString()
            val capacityString = tankCapacityEditText.text.toString().trim()
            val tank = tankList.find { it.name == tankName }
            val currentAllocation = tank!!.currentAllocation

            val capacity = capacityString.toDoubleOrNull()

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Tank name is required", Toast.LENGTH_SHORT).show()
            } else if (capacity == null) {
                Toast.makeText(requireContext(), "Invalid capacity", Toast.LENGTH_SHORT).show()
            } else if (capacity < 1) {
                Toast.makeText(requireContext(), "Allocation cannot be less than 1", Toast.LENGTH_SHORT).show()
            } else if (capacity > (databaseHandler.getRemainingAllocation() + tankAllocation)) {
                Toast.makeText(requireContext(), "Exceeded available allocation", Toast.LENGTH_SHORT).show()
            } else {
                updateTank(name, capacity, color, tankName, currentAllocation, tankAllocation)
                dialog.dismiss()
            }
        }
        val name = tankNameEditText.text.toString().trim()
        val color = tankColorSpinner.selectedItem.toString()
        val capacityString = tankCapacityEditText.text.toString().trim()
        val capacity = capacityString.toDouble()

        buttonDelete.setOnClickListener {
            deleteTank(name, capacity, color)
            dialog.dismiss() // Close dialog if the user cancels
        }

        dialog.show()
    }

    // Save the new tank (adjust to your own logic)
    private fun saveTank(name: String, capacity: Double, color: String) {
        val newTank = UI_Tank(name, capacity, color, capacity)

        // Save to the database
        val dbTank = DB_Tank(name = name, maxAllocation = capacity, color = color, currentAllocation = capacity)
        databaseHandler.insertTank(dbTank)

        // Update the UI list
        tanksList.add(newTank)
        tanksAdapter.notifyDataSetChanged()
    }

    private fun updateTank(name: String, capacity: Double, color: String, oldName: String, currentAllocation: Double, oldAllocation: Double) {
        val updatedTank = DB_Tank(name = name, maxAllocation = capacity, color = color, currentAllocation = currentAllocation)
        databaseHandler.updateTank(updatedTank, oldAllocation)
        tanksList.removeIf { it.name == oldName }
        tanksList.add(UI_Tank(name, capacity, color, currentAllocation))
        tanksAdapter.notifyDataSetChanged()

    }

    private fun deleteTank(name: String, capacity: Double, color: String) {
        val theTank = DB_Tank(name = name, maxAllocation = capacity, color = color, currentAllocation = 0.0)
        databaseHandler.deleteTank(theTank)
        tanksList.removeIf { it.name == name }
        tanksAdapter.notifyDataSetChanged()
    }

    // Load the tanks from the database or any data source
    private fun loadTanks() {
        tanksList.clear()
        val dbHandler = DatabaseHandler(requireContext())
        val loadedTanks = dbHandler.getAllTanksUI()
        tanksList.addAll(loadedTanks)
        tanksAdapter.notifyDataSetChanged()
    }

    private fun onEditTank(tank: UI_Tank) {
        showEditTankDialog(tank.name, tank.allocation, tanksList)
    }
}
