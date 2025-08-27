package com.agilan.expensetrackerapp.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.agilan.expensetrackerapp.presentation.nav.NavRoutes
import androidx.compose.ui.Modifier

// MainScreen.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Expense Tracker") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { navController.navigate(NavRoutes.AddExpense) }) {
                Text("Add Expense")
            }

            Button(onClick = { navController.navigate(NavRoutes.ViewExpenses) }) {
                Text("View Expenses")
            }
            Button(onClick = { navController.navigate(NavRoutes.SortAndFilterExpense) }) {
                Text("Sort And Filter Expenses")
            }

        }
    }
}
