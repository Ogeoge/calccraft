package com.calccraft.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.calccraft.state.CalcViewModel
import com.calccraft.ui.screens.CalculatorScreen
import com.calccraft.ui.screens.HistoryScreen

sealed class Screen(val route: String) {
    object Calculator : Screen("calculator")
    object History : Screen("history")
}

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val calcViewModel: CalcViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Calculator.route
    ) {
        composable(Screen.Calculator.route) {
            CalculatorScreen(
                viewModel = calcViewModel,
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                }
            )
        }
        composable(Screen.History.route) {
            HistoryScreen(
                viewModel = calcViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
