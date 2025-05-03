package com.example.expensetracker

data class savingsGoal (
    val id: Int = 0,  // Primary key for DB use
    val title: String,
    val goal: Double,
    var funds: Double
)