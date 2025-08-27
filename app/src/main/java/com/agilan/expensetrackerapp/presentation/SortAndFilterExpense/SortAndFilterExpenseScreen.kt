package com.agilan.expensetrackerapp.presentation.SortAndFilterExpense

import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.agilan.expensetrackerapp.data.local.ExpenseDatabase
import com.agilan.expensetrackerapp.data.local.entity.ExpenseEntity
import com.agilan.expensetrackerapp.data.repository.ExpenseRepository
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SortAndFilterExpenseScreen(navController: NavController) {

    /*val viewModel: ExpenseViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as Application)
    )*/
    val context = LocalContext.current
    val db = ExpenseDatabase.getDatabase(context)
    val dao = db.expenseDao()
    val repository = remember { ExpenseRepository(dao) }
    val viewModel = remember { ExpenseViewModel(repository) }
    val expenses by viewModel.filteredExpenses.collectAsState()

    val uiState by viewModel.uiState.collectAsState()
    var selectedFilterType by remember { mutableStateOf("Date Range") }
    var amountCondition by remember { mutableStateOf(">") }
    var amountValue by remember { mutableStateOf("") }
    var fromDate by remember { mutableStateOf<LocalDate?>(null) }
    var toDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedSortOption by remember { mutableStateOf("Date Ascending") }

    val filterTypes = listOf("Date Range", "Amount")
    val sortOptions =
        listOf("Date Ascending", "Date Descending", "Amount Ascending", "Amount Descending")
    val amountConditions = listOf("=", ">", "<")
// Mode: "", "Sort", "Filter"
    var mode by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        viewModel.onEvent(SortAndFilterUiEvent.LoadFilterExpenses)
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Sort And Filter Expense") }, // or View Expenses, etc.
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth() // Makes the Row take the full available width
                    .padding(4.dp), // Adds padding around the row
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceAround // Distributes space evenly around items
            ) {
                Button(onClick = {
                    mode = "Sort"
                }) {
                    Text("Sort By")
                }
                Button(onClick = {
                    mode = "Filter"
                }) {
                    Text("Filter")
                }
                Button(onClick = {
                    viewModel.clearFilter()
                    mode = ""
                }) {
                    Text("Clear")
                }
            }

            // Filter Type Dropdown
            if (mode == "Filter") {
                DropdownSelector(
                    label = "Filter Type",
                    options = filterTypes,
                    selected = selectedFilterType,
                    onSelectedChange = { selectedFilterType = it }
                )

                Spacer(Modifier.height(8.dp))

                if (selectedFilterType == "Date Range") {
                    DatePickerField("From Date", fromDate) { fromDate = it }
                    DatePickerField("To Date", toDate) { toDate = it }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DropdownSelector(
                            label = "Condition",
                            options = amountConditions,
                            selected = amountCondition,
                            onSelectedChange = { amountCondition = it },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(
                            value = amountValue,
                            onValueChange = { amountValue = it },
                            label = { Text("Amount") },
                            modifier = Modifier.weight(2f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        mode = ""
                        if (selectedFilterType == "Date Range") {
                            viewModel.applyDateFilter(
                                fromDate = fromDate,
                                toDate = toDate
                            )
                        } else {
                            viewModel.applyAmountFilter(
                                amountCondition = amountCondition,
                                amountValue = amountValue.toDoubleOrNull()
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply")
                }
            }
            Spacer(Modifier.height(12.dp))
            if (mode == "Sort") {
                // Sort Option
                DropdownSelector(
                    label = "Sort By",
                    options = sortOptions,
                    selected = selectedSortOption,
                    onSelectedChange = { selectedSortOption = it }
                )

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        mode = ""
                        viewModel.applySort(
                            sortOption = selectedSortOption
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply")
                }
            }
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
            Spacer(Modifier.height(16.dp))

            LazyColumn {
                items(expenses) { expense ->
                    ExpenseItem(expense)
                }
            }

        }
    }
}

@Composable
fun DropdownSelector(
    label: String,
    options: List<String>,
    selected: String,
    onSelectedChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier) {
        Text(label)
        Box {
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                enabled = false,
                trailingIcon = {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                }
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(onClick = {
                        onSelectedChange(option)
                        expanded = false
                    }, text = { Text(option) })
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DatePickerField(label: String, date: LocalDate?, onDateSelected: (LocalDate) -> Unit) {
    val context = LocalContext.current
    val dateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy")
    val datePickerDialog = remember {
        DatePickerDialog(context).apply {
            setOnDateSetListener { _, year, month, day ->
                onDateSelected(LocalDate.of(year, month + 1, day))
            }
        }
    }

    OutlinedTextField(
        value = date?.format(dateFormat) ?: "",
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        enabled = false,
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .clickable {
                datePickerDialog.show()
            }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExpenseItem(expense: ExpenseEntity) {
    val color = if (expense.type == "credit") Color(0xFF2E7D32) else Color(0xFFC62828)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Date (left aligned)
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    SimpleDateFormat(
                        "dd/MM/yy",
                        Locale.getDefault()
                    ).format(expense.date)
                )
            }

            // Description (center)
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(expense.description)
            }

            // Amount (right aligned)
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    "₹${expense.amount}",
                    color = color
                )
            }
        }

    }
}
