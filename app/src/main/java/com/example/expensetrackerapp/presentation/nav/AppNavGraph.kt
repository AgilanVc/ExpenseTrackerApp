package com.example.expensetrackerapp.presentation.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.expensetrackerapp.presentation.addexpense.AddExpenseScreen
import com.example.expensetrackerapp.presentation.home.MainScreen
import com.example.expensetrackerapp.presentation.viewexpenses.ViewExpensesScreen

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
    }


}