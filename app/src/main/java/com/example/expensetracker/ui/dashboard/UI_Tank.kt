package com.example.expensetracker.ui.dashboard

data class UI_Tank(
    val name: String,
    val allocation: Double,
    val color: String = "#FFFFFF",
    val currentAllocation: Double// Default to white if no color is assigned
)
