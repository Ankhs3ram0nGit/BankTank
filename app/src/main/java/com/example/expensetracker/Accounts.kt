package com.example.expensetracker

class Account(val id: Int, var name: String, var balance: Double, var color: String) {
    // Constructor with id included
    constructor(name: String, balance: Double, color: String) : this(0, name, balance, color) // Default id as 0
}
