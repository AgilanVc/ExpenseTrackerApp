package com.agilan.expensetrackerapp.presentation.SortAndFilterExpense

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agilan.expensetrackerapp.data.local.entity.ExpenseEntity
import com.agilan.expensetrackerapp.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class ExpenseViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {
    private val _allExpenses = MutableStateFlow<List<ExpenseEntity>>(emptyList())
    private val _filteredExpenses = MutableStateFlow<List<ExpenseEntity>>(emptyList())
    private val _uiState = MutableStateFlow(SortAndFilterUiState(isLoading = true))
    val uiState: StateFlow<SortAndFilterUiState> = _uiState
    fun onEvent(event: SortAndFilterUiEvent) {
        when (event) {
            is SortAndFilterUiEvent.LoadFilterExpenses -> loadExpenses()
        }
    }

    private fun loadExpenses() {
        viewModelScope.launch {
            repository.getAllExpenses().collect { expenseList ->
                val creditTotal = expenseList.filter { it.type == "credit" }.sumOf { it.amount }
                val debitTotal = expenseList.filter { it.type == "debit" }.sumOf { it.amount }
                val balance = creditTotal - debitTotal

                _uiState.value = SortAndFilterUiState(
                    expenses = expenseList,
                    totalCredit = creditTotal,
                    totalDebit = debitTotal,
                    balance = balance,
                    isLoading = false
                )
            }
        }
    }
    val filteredExpenses: StateFlow<List<ExpenseEntity>> = _filteredExpenses

    init {
        viewModelScope.launch {
            repository.getAllExpenses().collect {
                _allExpenses.value = it
                _filteredExpenses.value = it
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun applyDateFilter(
        fromDate: LocalDate?,
        toDate: LocalDate?
    ) {
        var result = _allExpenses.value
        result = result.filter {
            val expenseDate = Instant.ofEpochMilli(it.date.toString().toLong())
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            val isAfterFrom = fromDate?.let { fd -> expenseDate >= fd } ?: true
            val isBeforeTo = toDate?.let { td -> expenseDate <= td } ?: true
            isAfterFrom && isBeforeTo
        }
        _filteredExpenses.value = result
        updateTotalsFromList(result)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun applyAmountFilter(
        amountCondition: String,
        amountValue: Double?
    ) {
        var result = _allExpenses.value
        result = result.filter {
            when (amountCondition) {
                "=" -> it.amount == amountValue
                ">" -> it.amount > amountValue!!
                "<" -> it.amount < amountValue!!
                else -> true
            }
        }
        _filteredExpenses.value = result
        updateTotalsFromList(result)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun applySort(
        sortOption: String
    ) {
        var result = _allExpenses.value
        result = when (sortOption) {
            "Date Ascending" -> result.sortedBy {
                Instant.ofEpochMilli(it.date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }

            "Date Descending" -> result.sortedByDescending {
                Instant.ofEpochMilli(it.date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }

            "Amount Ascending" -> result.sortedBy { it.amount }
            "Amount Descending" -> result.sortedByDescending { it.amount }
            else -> result
        }
        _filteredExpenses.value = result
    }

    fun clearFilter() {
        _filteredExpenses.value = _allExpenses.value
        updateTotalsFromList(_allExpenses.value)
    }
    private fun updateTotalsFromList(list: List<ExpenseEntity>) {
        val credit = list.filter { it.type.equals("credit", ignoreCase = true) }
            .sumOf { it.amount }

        val debit = list.filter { it.type.equals("debit", ignoreCase = true) }
            .sumOf { it.amount }

        _uiState.update {
            it.copy(
                totalCredit = credit,
                totalDebit = debit,
                balance = credit - debit
            )
        }
    }
}



