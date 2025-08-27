package com.agilan.expensetrackerapp.presentation.addexpense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agilan.expensetrackerapp.data.local.entity.ExpenseEntity
import com.agilan.expensetrackerapp.domain.AddExpenseUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AddExpenseUiState(
    val date: Long = System.currentTimeMillis(),
    val description: String = "",
    val amount: String = "",
    val type: String = "debit"
)

sealed class AddExpenseUiEvent {
    data class DescriptionChanged(val value: String) : AddExpenseUiEvent()
    data class AmountChanged(val value: String) : AddExpenseUiEvent()
    data class TypeChanged(val value: String) : AddExpenseUiEvent()
    data class DateChanged(val value: Long) : AddExpenseUiEvent()
    object Submit : AddExpenseUiEvent()
}

sealed class AddExpenseSideEffect {
    data class ShowError(val message: String) : AddExpenseSideEffect()
    object ExpenseAdded : AddExpenseSideEffect() // ✅ Already exists
}


class AddExpenseViewModel(
    private val addExpenseUseCase: AddExpenseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<AddExpenseSideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    fun onEvent(event: AddExpenseUiEvent) {
        when (event) {
            is AddExpenseUiEvent.DescriptionChanged -> _uiState.update { it.copy(description = event.value) }
            is AddExpenseUiEvent.AmountChanged -> _uiState.update { it.copy(amount = event.value) }
            is AddExpenseUiEvent.TypeChanged -> _uiState.update { it.copy(type = event.value) }
            is AddExpenseUiEvent.DateChanged -> _uiState.update { it.copy(date = event.value) }
            is AddExpenseUiEvent.Submit -> submitExpense()
        }
    }

    private fun submitExpense() {
        val state = _uiState.value

        val amount = state.amount.trim().toDoubleOrNull()

        if (state.description.isBlank()) {
            sendError("Description is required")
            return
        }

        if (amount == null || amount <= 0.0) {
            sendError("Enter a valid amount")
            return
        }

        val expense = ExpenseEntity(
            date = state.date,
            description = state.description.trim(),
            amount = amount,
            type = state.type
        )

        viewModelScope.launch {
            addExpenseUseCase(expense)
            _sideEffect.send(AddExpenseSideEffect.ExpenseAdded)
            // ✅ Clear form after submit
            _uiState.value = AddExpenseUiState(
                date = System.currentTimeMillis(), // keep today
                type = "debit" // or previous type
            )
        }
    }

    private fun sendError(message: String) {
        viewModelScope.launch {
            _sideEffect.send(AddExpenseSideEffect.ShowError(message))
        }
    }

}