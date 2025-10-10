package com.example.bsprestagil.screens.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bsprestagil.components.InfoCard
import com.example.bsprestagil.components.TopAppBarComponent
import com.example.bsprestagil.ui.theme.ErrorColor
import com.example.bsprestagil.ui.theme.SuccessColor
import com.example.bsprestagil.ui.theme.WarningColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    navController: NavController
) {
    var periodoSeleccionado by remember { mutableStateOf("Mes actual") }
    var showMenu by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBarComponent(
                title = "Reportes",
                onNavigateBack = { navController.navigateUp() },
                actions = {
                    IconButton(onClick = { /* TODO: Exportar */ }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Exportar")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selector de período
            item {
                Card(
                    onClick = { showMenu = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Período",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = periodoSeleccionado,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                    }
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    listOf("Hoy", "Semana actual", "Mes actual", "Año actual").forEach { periodo ->
                        DropdownMenuItem(
                            text = { Text(periodo) },
                            onClick = {
                                periodoSeleccionado = periodo
                                showMenu = false
                            }
                        )
                    }
                }
            }
            
            // Estadísticas de cobros
            item {
                Text(
                    text = "Resumen de cobros",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoCard(
                        title = "Total cobrado",
                        value = "$45,600",
                        subtitle = "24 pagos",
                        color = SuccessColor,
                        modifier = Modifier.weight(1f)
                    )
                    InfoCard(
                        title = "Intereses",
                        value = "$8,420",
                        subtitle = "18.4%",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Préstamos activos
            item {
                Text(
                    text = "Estado de préstamos",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ReportRow("Préstamos activos", "12", SuccessColor)
                        ReportRow("Préstamos atrasados", "3", WarningColor)
                        ReportRow("Préstamos completados", "8", MaterialTheme.colorScheme.primary)
                        ReportRow("Tasa de morosidad", "25%", ErrorColor)
                    }
                }
            }
            
            // Clientes
            item {
                Text(
                    text = "Clientes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ReportRow("Total clientes", "28", MaterialTheme.colorScheme.primary)
                        ReportRow("Clientes al día", "20", SuccessColor)
                        ReportRow("Clientes atrasados", "5", WarningColor)
                        ReportRow("Clientes morosos", "3", ErrorColor)
                    }
                }
            }
            
            item {
                Button(
                    onClick = { /* TODO: Exportar */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Exportar reporte",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ReportRow(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

