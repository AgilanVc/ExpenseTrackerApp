package com.example.expensetrackerapp.presentation.viewexpenses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.expensetrackerapp.data.local.ExpenseDatabase
import com.example.expensetrackerapp.data.local.entity.ExpenseEntity
import com.example.expensetrackerapp.data.repository.ExpenseRepository
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewExpensesScreen(navController: NavController) {
    val context = LocalContext.current
    val db = ExpenseDatabase.getDatabase(context)
    val dao = db.expenseDao()
    val repository = remember { ExpenseRepository(dao) }
    val viewModel = remember { ViewExpensesViewModel(repository) }

    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    LaunchedEffect(Unit) {
        viewModel.onEvent(ViewExpensesUiEvent.LoadExpenses)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("View Expenses") }, // or View Expenses, etc.
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
    )

    { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        )
        {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Description") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )

                DropdownMenuFilter(selectedFilter) { selected ->
                    selectedFilter = selected
                }

            }
            // 🧾 Summary Totals
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Total Credit: ₹${uiState.totalCredit}")
                    Text("Total Debit: ₹${uiState.totalDebit}")
                    Text("Balance: ₹${uiState.balance}")
                }
            }

            // 🧾 Expense List
            if (uiState.expenses.isEmpty()) {
                Text("No expenses found.")
            } else {
                val filteredExpenses = uiState.expenses
                    .filter {
                        (selectedFilter == "All" || it.type.equals(
                            selectedFilter,
                            ignoreCase = true
                        )) &&
                                it.description.contains(searchQuery, ignoreCase = true)
                    }

                if (filteredExpenses.isEmpty()) {
                    Text("No matching expenses found.")
                } else {
                    // Put these remembers just above the LazyColumn (so they are in the same composable scope)
                    var showDeleteDialog by remember { mutableStateOf(false) }
                    var pendingDeleteExpense by remember { mutableStateOf<ExpenseEntity?>(null) }

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        itemsIndexed(filteredExpenses) { index, expense ->

                            // intercept swipe -> ask for confirmation instead of deleting immediately
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    if (value == SwipeToDismissBoxValue.StartToEnd || value == SwipeToDismissBoxValue.EndToStart) {
                                        // store the swiped expense and open confirmation dialog
                                        pendingDeleteExpense = expense
                                        showDeleteDialog = true
                                        false // prevent auto-dismiss until user confirms
                                    } else {
                                        false
                                    }
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Red)
                                            .padding(start = 16.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color.White
                                        )
                                    }
                                },
                                content = {
                                    ExpenseItem(serialNo = index + 1, expense = expense)
                                }
                            )
                        }
                    }

// Confirmation dialog outside the LazyColumn (show once)
                    if (showDeleteDialog && pendingDeleteExpense != null) {
                        val e = pendingDeleteExpense!!
                        AlertDialog(
                            onDismissRequest = {
                                // close dialog and clear pending item; nothing deleted
                                showDeleteDialog = false
                                pendingDeleteExpense = null
                            },
                            title = { Text("Delete expense") },
                            text = {
                                Text("Are you sure you want to delete \"${e.description}\" for ₹${e.amount}?")
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    // Perform deletion and close dialog
                                    viewModel.deleteExpense(e)
                                    showDeleteDialog = false
                                    pendingDeleteExpense = null
                                }) {
                                    Text("Yes")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    // Cancel deletion and close dialog
                                    showDeleteDialog = false
                                    pendingDeleteExpense = null
                                }) {
                                    Text("No")
                                }
                            }
                        )
                    }

                }
            }
        }
    }

}

@Composable
fun DropdownMenuFilter(
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selected)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf("All", "Credit", "Debit").forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = {
                        onSelect(it)
                        expanded = false
                    }
                )
            }
        }
    }
}


@Composable
fun ExpenseItem(serialNo: Int, expense: ExpenseEntity) {
    val color = if (expense.type == "credit") Color(0xFF2E7D32) else Color(0xFFC62828)
    val date = remember(expense.date) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(expense.date))
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("S.No: $serialNo", style = MaterialTheme.typography.labelSmall)
            Text("Date: $date", style = MaterialTheme.typography.labelSmall)
            Text(expense.description, style = MaterialTheme.typography.titleMedium)
            Text("₹${expense.amount}", color = color, style = MaterialTheme.typography.bodyLarge)
            Text("Type: ${expense.type}", color = color)
        }
    }
}
