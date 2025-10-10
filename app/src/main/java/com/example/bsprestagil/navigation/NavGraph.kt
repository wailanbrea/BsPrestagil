package com.example.bsprestagil.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.bsprestagil.screens.auth.LoginScreen
import com.example.bsprestagil.screens.auth.RegisterScreen
import com.example.bsprestagil.screens.clients.AddEditClientScreen
import com.example.bsprestagil.screens.clients.ClientDetailScreen
import com.example.bsprestagil.screens.clients.ClientsScreen
import com.example.bsprestagil.screens.collaterals.AddEditCollateralScreen
import com.example.bsprestagil.screens.collaterals.CollateralDetailScreen
import com.example.bsprestagil.screens.collaterals.CollateralsScreen
import com.example.bsprestagil.screens.collaterals.QRGarantiaScreen
import com.example.bsprestagil.screens.dashboard.DashboardScreen
import com.example.bsprestagil.screens.loans.AddLoanScreen
import com.example.bsprestagil.screens.loans.LoanDetailScreen
import com.example.bsprestagil.screens.loans.LoansScreen
import com.example.bsprestagil.screens.notifications.NotificationsScreen
import com.example.bsprestagil.screens.payments.PaymentDetailScreen
import com.example.bsprestagil.screens.payments.PaymentsScreen
import com.example.bsprestagil.screens.payments.RegisterPaymentScreen
import com.example.bsprestagil.screens.reports.ReportsScreen
import com.example.bsprestagil.screens.settings.SettingsScreen
import com.example.bsprestagil.screens.test.TestSyncScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
        
        // Main Tabs
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        
        composable(Screen.Clients.route) {
            ClientsScreen(navController = navController)
        }
        
        composable(Screen.Loans.route) {
            LoansScreen(navController = navController)
        }
        
        composable(Screen.Payments.route) {
            PaymentsScreen(navController = navController)
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        
        // Clients
        composable(
            route = Screen.ClientDetail.route,
            arguments = listOf(navArgument("clientId") { type = NavType.StringType })
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getString("clientId") ?: ""
            ClientDetailScreen(
                clientId = clientId,
                navController = navController
            )
        }
        
        composable(
            route = Screen.AddEditClient.route,
            arguments = listOf(navArgument("clientId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getString("clientId")
            AddEditClientScreen(
                clientId = clientId,
                navController = navController
            )
        }
        
        // Loans
        composable(
            route = Screen.LoanDetail.route,
            arguments = listOf(navArgument("loanId") { type = NavType.StringType })
        ) { backStackEntry ->
            val loanId = backStackEntry.arguments?.getString("loanId") ?: ""
            LoanDetailScreen(
                loanId = loanId,
                navController = navController
            )
        }
        
        composable(
            route = Screen.AddLoan.route,
            arguments = listOf(navArgument("clientId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getString("clientId")
            AddLoanScreen(
                clientId = clientId,
                navController = navController
            )
        }
        
        // Collaterals
        composable(Screen.Collaterals.route) {
            CollateralsScreen(navController = navController)
        }
        
        composable(
            route = Screen.CollateralDetail.route,
            arguments = listOf(navArgument("collateralId") { type = NavType.StringType })
        ) { backStackEntry ->
            val collateralId = backStackEntry.arguments?.getString("collateralId") ?: ""
            CollateralDetailScreen(
                collateralId = collateralId,
                navController = navController
            )
        }
        
        composable(
            route = Screen.AddEditCollateral.route,
            arguments = listOf(navArgument("collateralId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val collateralId = backStackEntry.arguments?.getString("collateralId")
            AddEditCollateralScreen(
                collateralId = collateralId,
                navController = navController
            )
        }
        
        composable(
            route = Screen.QRGarantia.route,
            arguments = listOf(
                navArgument("garantiaId") { type = NavType.StringType },
                navArgument("clienteNombre") { type = NavType.StringType },
                navArgument("descripcion") { type = NavType.StringType },
                navArgument("valorEstimado") { type = NavType.StringType },
                navArgument("tipo") { type = NavType.StringType },
                navArgument("fechaRegistro") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            QRGarantiaScreen(
                garantiaId = backStackEntry.arguments?.getString("garantiaId") ?: "",
                clienteNombre = backStackEntry.arguments?.getString("clienteNombre") ?: "",
                descripcion = backStackEntry.arguments?.getString("descripcion") ?: "",
                valorEstimado = backStackEntry.arguments?.getString("valorEstimado")?.toDoubleOrNull() ?: 0.0,
                tipo = backStackEntry.arguments?.getString("tipo") ?: "",
                fechaRegistro = backStackEntry.arguments?.getLong("fechaRegistro") ?: 0L,
                navController = navController
            )
        }
        
        // Payments
        composable(
            route = Screen.RegisterPayment.route,
            arguments = listOf(navArgument("loanId") { type = NavType.StringType })
        ) { backStackEntry ->
            val loanId = backStackEntry.arguments?.getString("loanId") ?: ""
            RegisterPaymentScreen(
                loanId = loanId,
                navController = navController
            )
        }
        
        composable(
            route = Screen.PaymentDetail.route,
            arguments = listOf(navArgument("paymentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val paymentId = backStackEntry.arguments?.getString("paymentId") ?: ""
            PaymentDetailScreen(
                paymentId = paymentId,
                navController = navController
            )
        }
        
        // Reports
        composable(Screen.Reports.route) {
            ReportsScreen(navController = navController)
        }
        
        // Notifications
        composable(Screen.Notifications.route) {
            NotificationsScreen(navController = navController)
        }
        
        // Test Sync
        composable(Screen.TestSync.route) {
            TestSyncScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}

