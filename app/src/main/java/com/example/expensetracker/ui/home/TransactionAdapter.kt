package com.example.expensetracker.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import com.example.expensetracker.TransactionData
import com.google.android.material.card.MaterialCardView

class TransactionAdapter(
    private val transactions: List<TransactionData>,
    private val onItemClick: (TransactionData) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    // Reverse the list to show the newest first
    private val reversedTransactions = transactions.asReversed()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.transaction_item, parent, false)
        return TransactionViewHolder(view)
    }

    override fun getItemCount(): Int = reversedTransactions.size

    override fun onBindViewHolder(
        holder: TransactionViewHolder,
        position: Int
    ) {
        val tx = reversedTransactions[position]

        holder.dateText.text = tx.date
        val desc = tx.description ?: ""
        holder.descriptionText.text = desc.ifBlank { "No description" }

        // Format and color the amount text
        holder.amountText.apply {
            text = String.format("%.2f", tx.amount)
            setTextColor(ContextCompat.getColor(context, android.R.color.white))
        }

        // Change the card outline color based on transactionType
        val strokeColor = if (tx.transactionType == "expense") {
            ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_dark)
        } else {
            ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_dark)
        }
        holder.cardView.setStrokeColor(strokeColor)

        // Set click listener for item
        holder.itemView.setOnClickListener {
            onItemClick(tx)
        }
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateText: TextView = itemView.findViewById(R.id.transactionDate)
        val descriptionText: TextView = itemView.findViewById(R.id.transactionDescription)
        val amountText: TextView = itemView.findViewById(R.id.transactionAmount)
        val cardView: MaterialCardView = itemView.findViewById(R.id.transactionCard)
    }
}
