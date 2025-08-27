package com.agilan.expensetrackerapp.presentation.SortAndFilterExpense

import com.agilan.expensetrackerapp.data.local.entity.Expense
import com.agilan.expensetrackerapp.data.local.entity.ExpenseEntity

data class SortAndFilterUiState(
    val expenses: List<ExpenseEntity> = emptyList(),
    val filteredExpenses: List<Expense> = emptyList(),

    // ✅ New fields for totals
    val totalCredit: Double = 0.0,
    val totalDebit: Double = 0.0,
    val balance: Double = 0.0,
    val isLoading: Boolean = false,
    // existing filter/sort fields
    val startDate: Long? = null,
    val endDate: Long? = null,
    val amountValue: Double? = null

)
sealed class SortAndFilterUiEvent {
    object LoadFilterExpenses : SortAndFilterUiEvent()
}