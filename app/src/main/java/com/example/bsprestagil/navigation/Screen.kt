package com.example.bsprestagil.navigation

sealed class Screen(val route: String) {
    // Autenticación
    object Login : Screen("login")
    object Register : Screen("register")
    
    // Main Tabs
    object Dashboard : Screen("dashboard")
    object Clients : Screen("clients")
    object Loans : Screen("loans")
    object Payments : Screen("payments")
    object Settings : Screen("settings")
    
    // Clientes
    object ClientDetail : Screen("client_detail/{clientId}") {
        fun createRoute(clientId: String) = "client_detail/$clientId"
    }
    object AddEditClient : Screen("add_edit_client?clientId={clientId}") {
        fun createRoute(clientId: String? = null) = 
            if (clientId != null) "add_edit_client?clientId=$clientId"
            else "add_edit_client"
    }
    
    // Préstamos
    object LoanDetail : Screen("loan_detail/{loanId}") {
        fun createRoute(loanId: String) = "loan_detail/$loanId"
    }
    object AddLoan : Screen("add_loan?clientId={clientId}") {
        fun createRoute(clientId: String? = null) =
            if (clientId != null) "add_loan?clientId=$clientId"
            else "add_loan"
    }
    object CalculadoraPrestamo : Screen("calculadora_prestamo")
    
    // Garantías
    object Collaterals : Screen("collaterals")
    object CollateralDetail : Screen("collateral_detail/{collateralId}") {
        fun createRoute(collateralId: String) = "collateral_detail/$collateralId"
    }
    object AddEditCollateral : Screen("add_edit_collateral?collateralId={collateralId}") {
        fun createRoute(collateralId: String? = null) =
            if (collateralId != null) "add_edit_collateral?collateralId=$collateralId"
            else "add_edit_collateral"
    }
    object QRGarantia : Screen("qr_garantia/{garantiaId}/{clienteNombre}/{descripcion}/{valorEstimado}/{tipo}/{fechaRegistro}") {
        fun createRoute(
            garantiaId: String,
            clienteNombre: String,
            descripcion: String,
            valorEstimado: Double,
            tipo: String,
            fechaRegistro: Long
        ) = "qr_garantia/$garantiaId/${android.net.Uri.encode(clienteNombre)}/${android.net.Uri.encode(descripcion)}/$valorEstimado/$tipo/$fechaRegistro"
    }
    object QRScanner : Screen("qr_scanner")
    object HistorialGarantias : Screen("historial_garantias")
    
    // Pagos
    object RegisterPayment : Screen("register_payment/{loanId}") {
        fun createRoute(loanId: String) = "register_payment/$loanId"
    }
    object PaymentDetail : Screen("payment_detail/{paymentId}") {
        fun createRoute(paymentId: String) = "payment_detail/$paymentId"
    }
    
    // Reportes
    object Reports : Screen("reports")
    
    // Notificaciones
    object Notifications : Screen("notifications")
    
    // Configuración
    object PersonalizacionRecibo : Screen("personalizacion_recibo")
    
    // Pantalla de prueba de sincronización
    object TestSync : Screen("test_sync")
}

