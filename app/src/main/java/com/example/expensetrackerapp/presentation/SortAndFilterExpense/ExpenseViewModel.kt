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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ExpenseViewModel (application: Application) : AndroidViewModel(application) {

    private val repository = ExpenseRepository(ExpenseDatabase.getDatabase(application).expenseDao())

    private val _allExpenses = MutableStateFlow<List<ExpenseEntity>>(emptyList())
    val allExpenses: StateFlow<List<ExpenseEntity>> = _allExpenses

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
    fun applyFilterAndSort(
        filterType: String,
        fromDate: LocalDate?,
        toDate: LocalDate?,
        amountCondition: String,
        amountValue: Double?,
        sortOption: String
    ) {
        var result = _allExpenses.value

        if (filterType == "Date Range") {
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE
            result = result.filter {
                val expenseDate = LocalDate.parse(it.date.toString(), formatter)
                val isAfterFrom = fromDate?.let { fd -> expenseDate >= fd } ?: true
                val isBeforeTo = toDate?.let { td -> expenseDate <= td } ?: true
                isAfterFrom && isBeforeTo
            }


        }
        else if (filterType == "Amount" && amountValue != null) {
            result = result.filter {
                when (amountCondition) {
                    "=" -> it.amount == amountValue
                    ">" -> it.amount > amountValue
                    "<" -> it.amount < amountValue
                    else -> true
                }
            }
        }

        result = when (sortOption) {
            "Date Ascending" -> result.sortedBy { it.date }
            "Date Descending" -> result.sortedByDescending { it.date }
            "Amount Ascending" -> result.sortedBy { it.amount }
            "Amount Descending" -> result.sortedByDescending { it.amount }
            else -> result
        }

        _filteredExpenses.value = result
    }
}

