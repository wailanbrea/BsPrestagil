package com.example.bsprestagil.screens.collaterals

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bsprestagil.components.TopAppBarComponent

@Composable
fun AddEditCollateralScreen(
    collateralId: String?,
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBarComponent(
                title = if (collateralId != null) "Editar garantía" else "Nueva garantía",
                onNavigateBack = { navController.navigateUp() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Formulario de garantía en desarrollo...",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

