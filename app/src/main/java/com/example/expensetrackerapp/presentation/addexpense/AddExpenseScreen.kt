package com.example.expensetrackerapp.presentation.addexpense

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.expensetrackerapp.data.local.ExpenseDatabase
import com.example.expensetrackerapp.data.repository.ExpenseRepository
import com.example.expensetrackerapp.domain.AddExpenseUseCase
import com.example.expensetrackerapp.presentation.nav.NavRoutes
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(navController: NavController) {
    val context = LocalContext.current

    // ViewModel setup (manual DI for now)
    val db = ExpenseDatabase.getDatabase(context)
    val dao = db.expenseDao()
    val repository = remember { ExpenseRepository(dao) }
    val viewModel: AddExpenseViewModel = remember {
        AddExpenseViewModel(AddExpenseUseCase(repository))
    }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val formattedDate = remember(uiState.date) {
        SimpleDateFormat("ddMMMyy,hh:mma", Locale.getDefault()).format(Date(uiState.date))
    }


    // Handle side effects
    LaunchedEffect(true) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is AddExpenseSideEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }

                is AddExpenseSideEffect.ExpenseAdded -> {
                    Toast.makeText(context, "Expense Added", Toast.LENGTH_SHORT).show()
                    Log.d(
                        "NAVIGATION",
                        "Navigating from ${NavRoutes.AddExpense} to ${NavRoutes.ViewExpenses}"
                    )

                    // ✅ Navigate to View Expenses screen
                    navController.navigate(NavRoutes.ViewExpenses) {
                        popUpTo(NavRoutes.AddExpense) { inclusive = true }
                    }
                }
            }
        }
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Expense") }, // or View Expenses, etc.
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = formattedDate,
                onValueChange = {},
                readOnly = true,
                label = { Text("Date & Time") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        showDatePickerAndTimePicker(context) { selectedMillis ->
                            viewModel.onEvent(AddExpenseUiEvent.DateChanged(selectedMillis))
                        }
                    },
                enabled = false
            )


            // Description
            OutlinedTextField(
                value = uiState.description,
                onValueChange = {
                    viewModel.onEvent(AddExpenseUiEvent.DescriptionChanged(it))
                },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            // Amount
            OutlinedTextField(
                value = uiState.amount,
                onValueChange = {
                    viewModel.onEvent(AddExpenseUiEvent.AmountChanged(it))
                },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Type: Credit / Debit
            Text("Type:")
            Row(Modifier.fillMaxWidth()) {
                listOf("credit", "debit").forEach { type ->
                    Row(
                        Modifier
                            .weight(1f)
                            .selectable(
                                selected = (uiState.type == type),
                                onClick = { viewModel.onEvent(AddExpenseUiEvent.TypeChanged(type)) }
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (uiState.type == type),
                            onClick = { viewModel.onEvent(AddExpenseUiEvent.TypeChanged(type)) }
                        )
                        Text(type.replaceFirstChar { it.uppercase() })
                    }
                }
            }

            // Submit Button
            Button(
                onClick = {
                    viewModel.onEvent(AddExpenseUiEvent.Submit)
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Add Expense")
            }
        }
    }

}

fun showDatePickerAndTimePicker(
    context: Context,
    onDateTimeSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance()

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            TimePickerDialog(
                context,
                { _, hour, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)
                    onDateTimeSelected(calendar.timeInMillis)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}



