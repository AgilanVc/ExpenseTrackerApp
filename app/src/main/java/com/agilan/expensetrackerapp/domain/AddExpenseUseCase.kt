package com.agilan.expensetrackerapp.domain

import com.agilan.expensetrackerapp.data.local.entity.ExpenseEntity
import com.agilan.expensetrackerapp.data.repository.ExpenseRepository

class AddExpenseUseCase(private val repository: ExpenseRepository) {
    suspend operator fun invoke(expense: ExpenseEntity) {
        repository.insertExpense(expense)
    }
}