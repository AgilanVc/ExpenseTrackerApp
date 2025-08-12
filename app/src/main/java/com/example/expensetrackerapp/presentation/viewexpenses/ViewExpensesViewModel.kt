package com.example.expensetrackerapp.presentation.viewexpenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetrackerapp.data.local.entity.ExpenseEntity
import com.example.expensetrackerapp.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

class ViewExpensesViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ViewExpensesUiState(isLoading = true))
    val uiState: StateFlow<ViewExpensesUiState> = _uiState

    fun onEvent(event: ViewExpensesUiEvent) {
        when (event) {
            is ViewExpensesUiEvent.LoadExpenses -> loadExpenses()
        }
    }

    private fun loadExpenses() {
        viewModelScope.launch {
            repository.getAllExpenses().collect { expenseList ->
                val creditTotal = expenseList.filter { it.type == "credit" }.sumOf { it.amount }
                val debitTotal = expenseList.filter { it.type == "debit" }.sumOf { it.amount }
                val balance = creditTotal - debitTotal

                _uiState.value = ViewExpensesUiState(
                    expenses = expenseList,
                    totalCredit = creditTotal,
                    totalDebit = debitTotal,
                    balance = balance,
                    isLoading = false
                )
            }
        }
    }
    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            // Optionally reload list or rely on Room Flow auto-update
        }
    }
    fun updateExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.updateExpense(expense)
        }
    }

}