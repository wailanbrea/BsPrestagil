package com.example.bsprestagil.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.bsprestagil.navigation.Screen

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

@Composable
fun BottomNavigationBar(
    navController: NavController,
    modifier: Modifier = Modifier,
    userRole: String? // Rol del usuario (desde Firestore)
) {
    // Debug: Log para verificar el rol
    android.util.Log.d("BottomNav", "📊 BottomNav recomponiendo con rol: $userRole")
    
    // Si no hay rol, no mostrar nada (aún cargando)
    if (userRole == null) return
    
    // Items diferentes según el rol
    val items = if (userRole == "COBRADOR") {
        // Cobradores: Solo dashboard, clientes, préstamos y pagos (4 items, SIN Settings)
        listOf(
            BottomNavItem(Screen.CobradorDashboard.route, Icons.Default.Dashboard, "Mi Dashboard"),
            BottomNavItem(Screen.Clients.route, Icons.Default.Person, "Clientes"),
            BottomNavItem(Screen.Loans.route, Icons.Default.AccountBalance, "Préstamos"),
            BottomNavItem(Screen.Payments.route, Icons.Default.Payment, "Pagos")
        )
    } else {
        // Prestamistas/Admin: Menú completo (5 items, CON Settings)
        listOf(
            BottomNavItem(Screen.Dashboard.route, Icons.Default.Home, "Dashboard"),
            BottomNavItem(Screen.Clients.route, Icons.Default.Person, "Clientes"),
            BottomNavItem(Screen.Loans.route, Icons.Default.AccountBalance, "Préstamos"),
            BottomNavItem(Screen.Payments.route, Icons.Default.Payment, "Pagos"),
            BottomNavItem(Screen.Settings.route, Icons.Default.Settings, "Ajustes")
        )
    }
    
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                
                // Animación de escala para el icono seleccionado
                val iconScale by animateFloatAsState(
                    targetValue = if (selected) 1.2f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "iconScale"
                )
                
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                // popUpTo según el rol
                                val startRoute = if (userRole == "COBRADOR") {
                                    Screen.CobradorDashboard.route
                                } else {
                                    Screen.Dashboard.route
                                }
                                popUpTo(startRoute) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            modifier = Modifier
                                .size(24.dp)
                                .scale(iconScale)
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )
            }
        }
    }
}

