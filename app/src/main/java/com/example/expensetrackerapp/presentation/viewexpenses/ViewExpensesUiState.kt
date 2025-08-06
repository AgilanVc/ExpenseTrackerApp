package com.example.expensetrackerapp.presentation.viewexpenses

import com.example.expensetrackerapp.data.local.entity.ExpenseEntity
import java.time.LocalDate

data class ViewExpensesUiState(
    val expenses: List<ExpenseEntity> = emptyList(),
    val totalCredit: Double = 0.0,
    val totalDebit: Double = 0.0,
    val balance: Double = 0.0,
    val isLoading: Boolean = false,
    val allExpenses: List<ExpenseEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: String = "all", // all, credit, debit, date
    val selectedDate: LocalDate? = null
)

sealed class ViewExpensesUiEvent {
    object LoadExpenses : ViewExpensesUiEvent()
}