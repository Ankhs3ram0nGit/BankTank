package com.example.expensetracker.ui.other

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
        val OtherViewModel =
                ViewModelProvider(this).get(OtherViewModel::class.java)

        _binding = FragmentOtherBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textOther
        OtherViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
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

