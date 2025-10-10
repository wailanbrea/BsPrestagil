package com.example.bsprestagil.screens.clients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bsprestagil.components.BottomNavigationBar
import com.example.bsprestagil.components.ClientCard
import com.example.bsprestagil.data.models.EstadoPagos
import com.example.bsprestagil.navigation.Screen
import com.example.bsprestagil.ui.theme.ErrorColor
import com.example.bsprestagil.ui.theme.SuccessColor
import com.example.bsprestagil.ui.theme.WarningColor
import com.example.bsprestagil.viewmodels.ClientsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    navController: NavController,
    clientsViewModel: ClientsViewModel = viewModel()
) {
    val clientes by clientsViewModel.clientes.collectAsState()
    val searchQuery by clientsViewModel.searchQuery.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Clientes",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${clientes.size} clientes registrados",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddEditClient.createRoute()) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar cliente")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Barra de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { clientsViewModel.searchClientes(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar cliente...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                singleLine = true
            )
            
            // Lista de clientes
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(clientes) { cliente ->
                    val estadoColor = when (cliente.historialPagos) {
                        EstadoPagos.AL_DIA -> SuccessColor
                        EstadoPagos.ATRASADO -> WarningColor
                        EstadoPagos.MOROSO -> ErrorColor
                    }
                    
                    val estadoTexto = when (cliente.historialPagos) {
                        EstadoPagos.AL_DIA -> "Al día"
                        EstadoPagos.ATRASADO -> "Atrasado"
                        EstadoPagos.MOROSO -> "Moroso"
                    }
                    
                    ClientCard(
                        nombre = cliente.nombre,
                        telefono = cliente.telefono,
                        prestamosActivos = cliente.prestamosActivos,
                        estado = estadoTexto,
                        estadoColor = estadoColor,
                        onClick = {
                            navController.navigate(Screen.ClientDetail.createRoute(cliente.id))
                        }
                    )
                }
                
                if (clientes.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                        ) {
                            Text(
                                text = if (searchQuery.isBlank()) 
                                    "No hay clientes registrados" 
                                else 
                                    "No se encontraron clientes",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

