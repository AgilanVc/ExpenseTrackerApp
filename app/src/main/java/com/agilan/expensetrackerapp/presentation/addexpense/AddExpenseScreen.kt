package com.agilan.expensetrackerapp.presentation.addexpense

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.items
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
import com.agilan.expensetrackerapp.data.local.ExpenseDatabase
import com.agilan.expensetrackerapp.data.repository.ExpenseRepository
import com.agilan.expensetrackerapp.domain.AddExpenseUseCase
import com.agilan.expensetrackerapp.presentation.nav.NavRoutes
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
                title = { Text("Add Expense") },
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
            // Date Picker
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

            // ✅ Description with AutoComplete
            ExpenseDescriptionAutoCompleteField(
                allSubcategories = defaultSubcategoryList(),
                value = uiState.description,
                onValueChange = { viewModel.onEvent(AddExpenseUiEvent.DescriptionChanged(it)) }
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

            // Submit
            Button(
                onClick = { viewModel.onEvent(AddExpenseUiEvent.Submit) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Add Expense")
            }
        }
    }
}

fun defaultSubcategoryList(): List<String> {
    return listOf(
        "Breakfast", "Lunch", "Dinner", "Snacks", "Groceries", "Restaurant", "Café / Coffee",
        "Rent", "Mortgage", "Home Maintenance", "Property Tax", "Home Insurance", "Furnishing", "Utilities",
        "Fuel", "Public Transport", "Taxi / Ride Share", "Vehicle Maintenance", "Vehicle Insurance", "Parking Fees", "Toll Charges",
        "Electricity", "Water", "Internet", "Mobile Bill", "Gas", "Cable", "TV Subscription", "OTT",
        "Clothing", "Electronics", "Accessories", "Gifts", "Home Items",
        "Medicines", "Doctor Visits", "Health Insurance", "Lab Tests", "GYM", "Yoga", "Sports",
        "Tuition Fees", "Books & Supplies", "Online Courses", "Coaching/Training",
        "Office Supplies", "Business Travel", "Subscriptions", "Client Meeting Expenses",
        "Movies", "Streaming Services", "Games", "Events & Shows", "Hobbies",
        "Flights", "Accommodation", "Local Transport", "Food (While Travel)", "Sightseeing",
        "Stocks", "Mutual Funds", "Crypto", "Fixed Deposits", "Real Estate",
        "Salon / Haircut", "Cosmetics", "Spa / Massage", "Toiletries",
        "Pet Food", "Veterinary", "Grooming", "Accessories",
        "Childcare", "Elder Care", "Gifts", "Anniversary", "Birthday",
        "Loan EMI", "Credit Card Payment", "Interest Paid",
        "Religious Offering", "NGO Donation", "Crowdfunding",
        "Uncategorized", "Lost Money", "Others"
    )
}


@Composable
fun ExpenseDescriptionAutoCompleteField(
    allSubcategories: List<String>,
    value: String,
    onValueChange: (String) -> Unit
) {
    var filteredList by remember { mutableStateOf(emptyList<String>()) }
    var job by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()

    Column {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
                job?.cancel()
                job = scope.launch {
                    delay(150)
                    filteredList = if (newValue.length >= 3) {
                        allSubcategories.filter {
                            it.contains(newValue, ignoreCase = true)
                        }
                    } else {
                        emptyList()
                    }
                }
            },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (filteredList.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
            ) {
                items(filteredList) { suggestion ->
                    Text(
                        text = suggestion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onValueChange(suggestion)
                                filteredList = emptyList()
                            }
                            .padding(12.dp)
                    )
                }
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



