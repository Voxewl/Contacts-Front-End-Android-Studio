package com.example.examenrecu.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.examenrecu.screens.UserDetailScreen
import com.example.examenrecu.screens.UserFormScreen
import com.example.examenrecu.screens.UserListScreen
import com.example.examenrecu.viewmodel.UserViewModel

// Definición de las rutas de navegación
sealed class Screen(val route: String) {
    object UserList : Screen("user_list")
    object UserDetail : Screen("user_detail/{userId}") {
        fun createRoute(userId: Int) = "user_detail/$userId"
    }
    object UserCreate : Screen("user_create")
    object UserEdit : Screen("user_edit/{userId}") {
        fun createRoute(userId: Int) = "user_edit/$userId"
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    // Crear una única instancia del ViewModel compartida entre todas las pantallas
    val viewModel: UserViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.UserList.route
    ) {
        // Pantalla de lista de usuarios (INDEX)
        composable(Screen.UserList.route) {
            UserListScreen(
                viewModel = viewModel,
                onUserClick = { userId ->
                    navController.navigate(Screen.UserDetail.createRoute(userId))
                },
                onCreateUserClick = {
                    navController.navigate(Screen.UserCreate.route)
                }
            )
        }

        // Pantalla de detalle de usuario (SHOW)
        composable(
            route = Screen.UserDetail.route,
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            UserDetailScreen(
                userId = userId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEditClick = { id ->
                    navController.navigate(Screen.UserEdit.createRoute(id))
                }
            )
        }

        // Pantalla de crear usuario (STORE)
        composable(Screen.UserCreate.route) {
            UserFormScreen(
                userId = null,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla de editar usuario (UPDATE)
        composable(
            route = Screen.UserEdit.route,
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            UserFormScreen(
                userId = userId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}