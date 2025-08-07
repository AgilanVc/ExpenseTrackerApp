package com.example.expensetrackerapp.presentation.nav

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.expensetrackerapp.presentation.SortAndFilterExpense.SortAndFilterExpenseScreen
import com.example.expensetrackerapp.presentation.addexpense.AddExpenseScreen
import com.example.expensetrackerapp.presentation.home.MainScreen
import com.example.expensetrackerapp.presentation.viewexpenses.ViewExpensesScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Main
    ) {
        composable(NavRoutes.Main) {
            MainScreen(navController)
        }

        composable(NavRoutes.AddExpense) {
            AddExpenseScreen(navController)
        }

        composable(NavRoutes.ViewExpenses) {
            ViewExpensesScreen(navController)
        }

        composable(NavRoutes.SortAndFilterExpense) {
            SortAndFilterExpenseScreen(navController)
        }
    }


}