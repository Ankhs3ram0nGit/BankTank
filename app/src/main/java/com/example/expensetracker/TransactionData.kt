package com.example.expensetracker

data class TransactionData(
    val transactionId: Int,
    val accountId: String,
    val tankId: String,
    val amount: Double,
    val date: String,
    val description: String?,
    val tag: String?,
    val bmlReference: String?,
    val bmlDate: String?,
    val transactionType: String
)
