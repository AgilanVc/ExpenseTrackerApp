package com.example.expensetrackerapp.presentation.SortAndFilterExpense

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetrackerapp.data.local.ExpenseDatabase
import com.example.expensetrackerapp.data.local.entity.ExpenseEntity
import com.example.expensetrackerapp.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository =
        ExpenseRepository(ExpenseDatabase.getDatabase(application).expenseDao())
    private val _allExpenses = MutableStateFlow<List<ExpenseEntity>>(emptyList())
    private val _filteredExpenses = MutableStateFlow<List<ExpenseEntity>>(emptyList())
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
    }
}



