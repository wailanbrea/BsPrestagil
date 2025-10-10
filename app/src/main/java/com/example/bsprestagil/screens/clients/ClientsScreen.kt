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
import androidx.navigation.NavController
import com.example.bsprestagil.components.BottomNavigationBar
import com.example.bsprestagil.components.ClientCard
import com.example.bsprestagil.data.models.Cliente
import com.example.bsprestagil.data.models.EstadoPagos
import com.example.bsprestagil.navigation.Screen
import com.example.bsprestagil.ui.theme.ErrorColor
import com.example.bsprestagil.ui.theme.SuccessColor
import com.example.bsprestagil.ui.theme.WarningColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    navController: NavController
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val clientes = remember {
        listOf(
            Cliente(
                id = "1",
                nombre = "Juan Pérez González",
                telefono = "+52 999 123 4567",
                direccion = "Calle Principal #123",
                prestamosActivos = 2,
                historialPagos = EstadoPagos.AL_DIA
            ),
            Cliente(
                id = "2",
                nombre = "María González López",
                telefono = "+52 999 234 5678",
                direccion = "Av. Reforma #456",
                prestamosActivos = 1,
                historialPagos = EstadoPagos.ATRASADO
            ),
            Cliente(
                id = "3",
                nombre = "Carlos Ramírez Sánchez",
                telefono = "+52 999 345 6789",
                direccion = "Col. Centro #789",
                prestamosActivos = 3,
                historialPagos = EstadoPagos.AL_DIA
            ),
            Cliente(
                id = "4",
                nombre = "Ana Martínez García",
                telefono = "+52 999 456 7890",
                direccion = "Fracc. Las Flores #321",
                prestamosActivos = 1,
                historialPagos = EstadoPagos.MOROSO
            ),
            Cliente(
                id = "5",
                nombre = "Roberto Silva Torres",
                telefono = "+52 999 567 8901",
                direccion = "Col. Juárez #654",
                prestamosActivos = 0,
                historialPagos = EstadoPagos.AL_DIA
            )
        )
    }
    
    val filteredClientes = if (searchQuery.isBlank()) {
        clientes
    } else {
        clientes.filter {
            it.nombre.contains(searchQuery, ignoreCase = true) ||
            it.telefono.contains(searchQuery, ignoreCase = true)
        }
    }
    
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
                onValueChange = { searchQuery = it },
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
                items(filteredClientes) { cliente ->
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
                
                if (filteredClientes.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                        ) {
                            Text(
                                text = "No se encontraron clientes",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

