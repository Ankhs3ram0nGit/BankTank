package com.example.expensetracker.ui.dashboard

data class UI_Tank(
    val name: String,
    var allocation: Double,
    val color: String = "#FFFFFF",
    var currentAllocation: Double// Default to white if no color is assigned
)
