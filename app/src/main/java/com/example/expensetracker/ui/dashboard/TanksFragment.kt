package com.example.expensetracker.ui.dashboard

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.expensetracker.DatabaseHandler
import com.example.expensetracker.R
import com.example.expensetracker.UI_Tank
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

        binding.manageButton.setOnClickListener {
            findNavController().navigate(R.id.action_tanks_to_manage)
        }

        val tanks       = databaseHandler.getAllTanksUI()
        val maxAlloc    = databaseHandler.getMaxAllocation().coerceAtLeast(1.0)

        binding.tanksScrollView.post {
            // total height available for the tubes
            val totalHeightPx = binding.tanksScrollView.height

            val reservedDp = 70 + 16 + 16 // baseline bottom + top/bottom padding
            val reservedPx = (reservedDp * resources.displayMetrics.density).toInt()

            val maxCanvasHeight = totalHeightPx - reservedPx

            populateTanksView(tanks, maxAlloc, maxCanvasHeight)
        }
        refreshTanks()
    }

    private fun populateTanksView(
        tanksList: List<UI_Tank>,
        maxAllocation: Double,
        maxCanvasHeight: Int
    ) {
        val container = binding.tanksFragment
        container.removeAllViews()

        val d = resources.displayMetrics.density
        fun Int.dp() = (this * d).toInt()

        val tubeWidth  = 100.dp()
        val tubeMargin = 32.dp()

        // Filter out "Savings"
        val filtered = tanksList.filter { it.name.lowercase() != "savings" }
        val maxAssigned = filtered.maxOfOrNull { it.allocation }?.coerceAtLeast(1.0) ?: 1.0

        val colorMap = mapOf(
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

        for (tank in filtered) {
            val ratio     = (tank.allocation / maxAssigned).coerceIn(0.0, 1.0)
            val tubeHeight = (maxCanvasHeight * ratio).toInt()

            val fillColor = ContextCompat.getColor(
                requireContext(),
                colorMap[tank.color] ?: R.color.account_gray
            )

            val tankLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    tubeWidth, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(tubeMargin, 16.dp(), tubeMargin, 16.dp())
                }
            }

            val tubeCanvas = FrameLayout(requireContext()).apply {
                layoutParams = LinearLayout
                    .LayoutParams(tubeWidth, maxCanvasHeight)
                    .apply { gravity = Gravity.BOTTOM }
            }

            val invert = requireContext()
                .getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                .getBoolean("invert_fill", false)

            val rawRatio  = (tank.currentAllocation / tank.allocation).coerceIn(0.0, 1.0)
            val fillRatio = if (invert) 1.0 - rawRatio else rawRatio
            val fillHeight = (tubeHeight * fillRatio).toInt()

            val fillDrawable = GradientDrawable().apply {
                cornerRadius = 12.dp().toFloat()
                setColor(fillColor)
            }

            tubeCanvas.addView(View(requireContext()).apply {
                layoutParams = FrameLayout
                    .LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, fillHeight, Gravity.BOTTOM)
                background = fillDrawable
            })

            tubeCanvas.addView(View(requireContext()).apply {
                layoutParams = FrameLayout
                    .LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, tubeHeight, Gravity.BOTTOM)
                setBackgroundResource(R.drawable.tank_outline)
            })

            tankLayout.addView(tubeCanvas)

            tankLayout.addView(TextView(requireContext()).apply {
                text    = tank.name
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                setTypeface(null, android.graphics.Typeface.BOLD)
                gravity = Gravity.CENTER
            })

            tankLayout.addView(TextView(requireContext()).apply {
                text    = "${tank.currentAllocation}/${tank.allocation}"
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
                gravity = Gravity.CENTER
            })

            tankLayout.setOnClickListener {
                findNavController().navigate(
                    R.id.action_tanks_to_tankDetails,
                    Bundle().apply {
                        putString("name", tank.name)
                        putString("color", tank.color)
                        putDouble("allocation", tank.allocation)
                        putDouble("currentAllocation", tank.currentAllocation)
                    }
                )
            }

            container.addView(tankLayout)
        }
    }
    private fun refreshTanks() {
        val tanks = databaseHandler.getAllTanksUI()
        val maxAlloc = databaseHandler.getMaxAllocation().coerceAtLeast(1.0)

        binding.tanksScrollView.post {
            val totalHeightPx = binding.tanksScrollView.height
            val reservedDp = 70 + 16 + 16
            val reservedPx = (reservedDp * resources.displayMetrics.density).toInt()
            val maxCanvasHeight = totalHeightPx - reservedPx

            populateTanksView(tanks, maxAlloc, maxCanvasHeight)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
