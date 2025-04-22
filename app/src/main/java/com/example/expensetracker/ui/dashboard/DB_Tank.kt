package com.example.expensetracker.ui.dashboard

data class DB_Tank(
    val id: Int = 0,  // Primary key for DB use
    val name: String,
    var maxAllocation: Double,
    val color: String? = null,
    var currentAllocation: Double// Color is optional for DB
)
