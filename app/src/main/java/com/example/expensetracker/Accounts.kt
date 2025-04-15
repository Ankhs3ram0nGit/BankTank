package com.example.expensetracker

class Account(val id: Int, var name: String, var balance: Double, var color: String, var accountNumber: String, var currency: String) {
    // Constructor with id included
    constructor(name: String, balance: Double, color: String, accountNumber: String, currency: String) : this(0, name, balance, color, accountNumber, currency) // Default id as 0
}
