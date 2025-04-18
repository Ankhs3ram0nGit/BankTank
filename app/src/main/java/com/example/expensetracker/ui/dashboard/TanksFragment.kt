package com.example.expensetracker.ui.dashboard

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.expensetracker.DatabaseHandler
import com.example.expensetracker.R
import com.example.expensetracker.databinding.FragmentTanksBinding


class TanksFragment : Fragment() {
    private var _binding: FragmentTanksBinding? = null
    private val binding get() = _binding!!
    private lateinit var databaseHandler: DatabaseHandler

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTanksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        databaseHandler = DatabaseHandler(requireContext())

        // Manage button navigation
        binding.manageButton.setOnClickListener {
            findNavController().navigate(R.id.action_tanks_to_manage)
        }

        // Load data from DB
        val tanks = databaseHandler.getAllTanks()
        val maxAllocation = databaseHandler.getMaxAllocation().coerceAtLeast(1.0)

        // Populate the UI with your vertical tubes
        populateTanksView(tanks, maxAllocation)
    }

    private fun populateTanksView(tanksList: List<UI_Tank>, maxAllocation: Double) {
        val container = binding.tanksFragment
        container.removeAllViews()

        val d = resources.displayMetrics.density
        fun Int.dp() = (this * d).toInt()

        // increased canvas height to create more space above the tube
        val maxCanvasHeight = 500.dp()  // Increased height for more space above the tube
        val tubeWidth = 100.dp()
        val tubeMargin = 32.dp()

        // find the largest current allocation
        val maxAssigned = tanksList.maxOfOrNull { it.allocation }?.coerceAtLeast(1.0) ?: 1.0

        val colorNameMap = mapOf(
            "Green" to R.color.account_green,
            "Red" to R.color.account_red,
            "Yellow" to R.color.account_yellow,
            "Gray" to R.color.account_gray,
            "Forest Green" to R.color.account_forest_green,
            "Mighty Purple" to R.color.account_mighty_purple,
            "Purple" to R.color.account_purple,
            "Seal Blue" to R.color.account_seal_blue,
            "Royal Blue" to R.color.account_royal_blue
        )

        for (tank in tanksList) {
            // now scale each tank relative to the largest one
            val ratio = (tank.allocation / maxAssigned).coerceIn(0.0, 1.0)
            val tubeHeight = (maxCanvasHeight * ratio).toInt()

            // fill color
            val fillColor = ContextCompat.getColor(
                requireContext(),
                colorNameMap[tank.color] ?: R.color.account_gray
            )

            // Create a container for the tube and its text (this will stack them vertically)
            val tankLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    tubeWidth, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(tubeMargin, 16.dp(), tubeMargin, 16.dp())  // Adjusted space between tube and text
                }
            }

            // container for this tube (tube is placed at the top)
            val tubeCanvas = FrameLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(tubeWidth, maxCanvasHeight).apply {
                    gravity = Gravity.BOTTOM
                }
            }

            // colored fill with rounded corners
            val fillDrawable = GradientDrawable().apply {
                cornerRadius = 12.dp().toFloat()
                setColor(fillColor)
            }
            val fillView = View(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    tubeHeight,
                    Gravity.BOTTOM
                )
                background = fillDrawable
            }
            tubeCanvas.addView(fillView)

            // outline overlay, same height
            val outlineView = View(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    tubeHeight,
                    Gravity.BOTTOM
                )
                setBackgroundResource(R.drawable.tank_outline)
            }
            tubeCanvas.addView(outlineView)

            // Add the tube to the tank layout (tube is the first element)
            tankLayout.addView(tubeCanvas)

            // Create text views for the tank name and allocation values
            val nameView = TextView(requireContext()).apply {
                text = tank.name
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white)) // Changed to white
                setTypeface(null, android.graphics.Typeface.BOLD)
                gravity = Gravity.CENTER
            }

            val allocationView = TextView(requireContext()).apply {
                text = "${tank.currentAllocation}/${tank.allocation}"
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
                gravity = Gravity.CENTER
            }

            // Add name and allocation text views to the tank layout (text is below the tube)
            tankLayout.addView(nameView)
            tankLayout.addView(allocationView)

            // Now add the entire tank layout (tube + text) to the container
            container.addView(tankLayout)
        }
    }












    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}







