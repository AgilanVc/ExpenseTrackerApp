package com.example.expensetrackerapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.DeleteColumn
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.expensetrackerapp.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT SUM(amount) FROM expenses WHERE type = 'credit'")
    fun getTotalCredit(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM expenses WHERE type = 'debit'")
    fun getTotalDebit(): Flow<Double?>

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)


}