package com.example.expensetrackerapp.presentation.SortAndFilterExpense

import android.app.Application
import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.expensetrackerapp.data.local.entity.ExpenseEntity
import java.text.SimpleDateFormat

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SortAndFilterExpenseScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: ExpenseViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as Application)
    )
    val expenses by viewModel.filteredExpenses.collectAsState()

    var selectedFilterType by remember { mutableStateOf("Date Range") }
    var amountCondition by remember { mutableStateOf(">") }
    var amountValue by remember { mutableStateOf("") }
    var fromDate by remember { mutableStateOf<LocalDate?>(null) }
    var toDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedSortOption by remember { mutableStateOf("Date Ascending") }

    val filterTypes = listOf("Date Range", "Amount")
    val sortOptions = listOf("Date Ascending", "Date Descending", "Amount Ascending", "Amount Descending")
    val amountConditions = listOf("=", ">", "<")

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Sort And Filter Expense", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(16.dp))

        // Filter Type Dropdown
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
                viewModel.applyFilterAndSort(
                    filterType = selectedFilterType,
                    fromDate = fromDate,
                    toDate = toDate,
                    amountCondition = amountCondition,
                    amountValue = amountValue.toDoubleOrNull(),
                    sortOption = selectedSortOption
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Apply")
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn {
            items(expenses) { expense ->
                ExpenseItem(expense)
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
        Column(Modifier.padding(12.dp)) {
            Text("Date: ${SimpleDateFormat("ddMMMyy,hh:mma", Locale.getDefault()).format(expense.date)}")
            Text("Description: ${expense.description}")
            Text("Amount: ₹${expense.amount}", color = color, style = MaterialTheme.typography.bodyLarge)
            Text("Type: ${expense.type}", color = color)
        }
    }
}
