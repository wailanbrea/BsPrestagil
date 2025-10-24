package com.example.bsprestagil.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.bsprestagil.screens.auth.LoginScreen
import com.example.bsprestagil.screens.auth.RegisterScreen
import com.example.bsprestagil.viewmodels.AuthViewModel
import com.example.bsprestagil.screens.clients.AddEditClientScreen
import com.example.bsprestagil.screens.clients.ClientDetailScreen
import com.example.bsprestagil.screens.clients.ClientsScreen
import com.example.bsprestagil.screens.collaterals.AddEditCollateralScreen
import com.example.bsprestagil.screens.collaterals.CollateralDetailScreen
import com.example.bsprestagil.screens.collaterals.CollateralsScreen
import com.example.bsprestagil.screens.collaterals.HistorialGarantiasScreen
import com.example.bsprestagil.screens.collaterals.QRGarantiaScreen
import com.example.bsprestagil.screens.collaterals.QRScannerScreen
import com.example.bsprestagil.screens.dashboard.DashboardScreen
import com.example.bsprestagil.screens.calculator.CalculadoraPrestamoScreen
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
        startDestination: String = Screen.Login.route,
    authViewModel: AuthViewModel = viewModel()
) {
    // Obtener el rol del usuario para protección de rutas
    val userRole by authViewModel.userRole.collectAsState()
    
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
        
        // Auth
        composable(Screen.Login.route) {
            LoginScreen(
                navController = navController,
                authViewModel = authViewModel
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
                },
                authViewModel = authViewModel
            )
        }
        
        composable(Screen.CambiarPassword.route) {
            com.example.bsprestagil.screens.auth.CambiarPasswordScreen(
                navController = navController
            )
        }
        
        // Main Tabs (con animaciones fade)
        composable(
            route = Screen.Dashboard.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            ProtectedRoute(
                navController = navController,
                currentRoute = Screen.Dashboard.route,
                userRole = userRole
            ) {
                DashboardScreen(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }
        }
        
        composable(
            route = Screen.Clients.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            ClientsScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
        
        composable(
            route = Screen.Loans.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            LoansScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
        
        composable(
            route = Screen.Payments.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            PaymentsScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
        
        composable(
            route = Screen.Settings.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            SettingsScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
        
        composable(Screen.PersonalizacionRecibo.route) {
            com.example.bsprestagil.screens.settings.PersonalizacionReciboScreen(
                navController = navController
            )
        }
        
        composable(Screen.ConfiguracionFactura.route) {
            com.example.bsprestagil.screens.settings.ConfiguracionFacturaScreen(
                navController = navController
            )
        }
        
        composable(Screen.LanguageSettings.route) {
            com.example.bsprestagil.screens.settings.LanguageSettingsScreen(
                navController = navController
            )
        }
        
        // Extensiones de préstamo
        composable(Screen.ExtensionPrestamo.route) { backStackEntry ->
            val prestamoId = backStackEntry.arguments?.getString("prestamoId") ?: ""
            com.example.bsprestagil.screens.prestamos.ExtensionPrestamoScreen(
                navController = navController,
                prestamoId = prestamoId
            )
        }
        
        composable(Screen.HistorialExtensiones.route) { backStackEntry ->
            val prestamoId = backStackEntry.arguments?.getString("prestamoId") ?: ""
            com.example.bsprestagil.screens.prestamos.HistorialExtensionesScreen(
                navController = navController,
                prestamoId = prestamoId
            )
        }
        
        composable(Screen.GestionCobradores.route) {
            com.example.bsprestagil.screens.settings.GestionCobradoresScreen(
                navController = navController
            )
        }
        
        composable(Screen.Perfil.route) {
            com.example.bsprestagil.screens.settings.PerfilScreen(
                navController = navController
            )
        }
        
        composable(Screen.CobradorDashboard.route) {
            com.example.bsprestagil.screens.cobradores.CobradorDashboardScreen(
                navController = navController,
                authViewModel = authViewModel,
                usersViewModel = viewModel()
            )
        }
        
        composable(Screen.ReporteCobradores.route) {
            com.example.bsprestagil.screens.reports.ReporteCobradoresScreen(
                navController = navController
            )
        }
        
        composable(Screen.Comisiones.route) {
            com.example.bsprestagil.screens.comisiones.ComisionesScreen(
                navController = navController
            )
        }
        
        // Clients (con animaciones slide)
        composable(
            route = Screen.ClientDetail.route,
            arguments = listOf(navArgument("clientId") { type = NavType.StringType }),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
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
            }),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(400)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(400)
                )
            }
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getString("clientId")
            AddEditClientScreen(
                clientId = clientId,
                navController = navController
            )
        }
        
        // Loans (con animaciones slide)
        composable(
            route = Screen.LoanDetail.route,
            arguments = listOf(navArgument("loanId") { type = NavType.StringType }),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
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
        
        composable(Screen.CalculadoraPrestamo.route) {
            CalculadoraPrestamoScreen(navController = navController)
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
        
        composable(Screen.QRScanner.route) {
            QRScannerScreen(navController = navController)
        }
        
        composable(Screen.HistorialGarantias.route) {
            HistorialGarantiasScreen(navController = navController)
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

