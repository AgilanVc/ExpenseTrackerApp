package com.agilan.expensetrackerapp.data.local.entity

import java.time.LocalDate

data class Expense(
    val description: String,
    val date: LocalDate,
    val amount: Double,
    val type: String // "credit" or "debit"
)