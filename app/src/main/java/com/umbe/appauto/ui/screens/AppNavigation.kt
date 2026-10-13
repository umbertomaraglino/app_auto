package com.umbe.appauto.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.umbe.appauto.data.CarViewModel

@Composable
fun AppNavigation(viewModel: CarViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                viewModel = viewModel,
                onCarClick = { carId -> navController.navigate("detail/$carId") }
            )
        }
        composable(
            route = "detail/{carId}",
            arguments = listOf(navArgument("carId") { type = NavType.IntType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getInt("carId") ?: return@composable
            CarDetailScreen(
                carId = carId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
