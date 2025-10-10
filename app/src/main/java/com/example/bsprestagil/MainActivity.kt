package com.example.bsprestagil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.bsprestagil.navigation.NavGraph
import com.example.bsprestagil.ui.theme.BsPrestagilTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BsPrestagilTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}