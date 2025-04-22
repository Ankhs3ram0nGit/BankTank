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
import com.example.expensetracker.R
import com.example.expensetracker.databinding.FragmentOtherBinding

class OtherFragment : Fragment() {

    private var _binding: FragmentOtherBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtherBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val sharedPrefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)

        // Access the switch via binding
        binding.switchInvertFill.isChecked = sharedPrefs.getBoolean("invert_fill", false)

        binding.switchInvertFill.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("invert_fill", isChecked).apply()
            Toast.makeText(requireContext(), "Tank fill inverted: $isChecked", Toast.LENGTH_SHORT).show()
        }

        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createSalaryRectangle() {

    }
}

