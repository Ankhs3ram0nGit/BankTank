package com.example.expensetracker.ui.other

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.expensetracker.DatabaseHandler
import com.example.expensetracker.R
import com.example.expensetracker.databinding.FragmentOtherBinding

class OtherFragment : Fragment() {
    private lateinit var databaseHandler: DatabaseHandler
    private var _binding: FragmentOtherBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtherBinding.inflate(inflater, container, false)
        val root: View = binding.root

        databaseHandler = DatabaseHandler(requireContext())
        val sharedPrefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)

        // Set initial state for invert switch
        binding.switchInvertFill.isChecked = sharedPrefs.getBoolean("invert_fill", false)
        binding.switchInvertFill.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("invert_fill", isChecked).apply()
            Toast.makeText(requireContext(), "Tank fill inverted: $isChecked", Toast.LENGTH_SHORT).show()
        }

        // Load saved salary if exists
        val savedSalary = sharedPrefs.getFloat("monthly_salary", 0f)
        if (savedSalary != 0f) {
            binding.SalaryInput.setText(savedSalary.toString())
        }

        // Cycle button click
        binding.buttonStartCycle.setOnClickListener {
            startNewCycle()
        }

        // Save button click
        binding.saveButton.setOnClickListener {
            saveSalary()
        }

        return root
    }

    private fun startNewCycle() {
        databaseHandler.incrementCurrentCycle()
        Toast.makeText(requireContext(), "New cycle started!", Toast.LENGTH_SHORT).show()
    }

    private fun saveSalary() {
        val inputText = binding.SalaryInput.text.toString()
        if (inputText.isNotEmpty()) {
            val salary = inputText.toFloatOrNull()
            if (salary != null) {
                val sharedPrefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                sharedPrefs.edit().putFloat("monthly_salary", salary).apply()
                Toast.makeText(requireContext(), "Salary saved: $salary", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Invalid salary input", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Please enter a salary", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


