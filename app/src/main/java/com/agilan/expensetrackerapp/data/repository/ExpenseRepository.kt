package com.agilan.expensetrackerapp.data.repository

import com.agilan.expensetrackerapp.data.local.dao.ExpenseDao
import com.agilan.expensetrackerapp.data.local.entity.ExpenseEntity

class ExpenseRepository(private val dao: ExpenseDao) {
    suspend fun insertExpense(expense: ExpenseEntity) = dao.insertExpense(expense)
    fun getAllExpenses() = dao.getAllExpenses()
    fun getTotalCredit() = dao.getTotalCredit()
    fun getTotalDebit() = dao.getTotalDebit()
    suspend fun deleteExpense(expense: ExpenseEntity) = dao.deleteExpense(expense)
    suspend fun updateExpense(expense: ExpenseEntity) = dao.updateExpense(expense)

}