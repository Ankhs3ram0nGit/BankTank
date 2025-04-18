package com.example.expensetracker.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import com.example.expensetracker.databinding.ListItemTankBinding

class TanksAdapter(
    private val UI_Tanks: List<UI_Tank>,
    private val onEditClick: (UI_Tank) -> Unit
) : RecyclerView.Adapter<TanksAdapter.TankViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TankViewHolder {
        val binding = ListItemTankBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TankViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TankViewHolder, position: Int) {
        holder.bind(UI_Tanks[position])
    }

    override fun getItemCount(): Int = UI_Tanks.size

    inner class TankViewHolder(private val binding: ListItemTankBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(uiTank: UI_Tank) {
            binding.tankName.text = uiTank.name

            // Map your display names to actual color resources:
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

            val ctx = binding.root.context
            val colorResId = colorMap[uiTank.color] ?: R.color.white
            val resolvedColor = ContextCompat.getColor(ctx, colorResId)

            // Apply resolved color
            binding.tankName.setTextColor(resolvedColor)
            binding.tankAllocation.setTextColor(resolvedColor)

            // (Any other UI you have—e.g. a colored side bar—would also use resolvedColor)
            // For example:
            // binding.tankColorBar.setBackgroundColor(resolvedColor)

            binding.tankAllocation.text = "${uiTank.allocation} allocated"
            binding.editButton.setOnClickListener { onEditClick(uiTank) }
        }
    }
}
