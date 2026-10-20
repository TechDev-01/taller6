package com.ucaldas.taller6

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                // Un solo ViewModel, creado AQUÍ y compartido por lista y detalle.
                // (Si se llamara viewModel() dentro de cada composable{} del NavHost,
                // cada pantalla tendría su propia instancia y no compartiría estado.)
                val vm: CursosViewModel = viewModel()

                NavHost(navController, startDestination = "lista") {
                    composable("lista") {
                        ListaScreen(vm) { id -> navController.navigate("detalle/$id") }
                    }
                    composable(
                        "detalle/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.IntType })
                    ) { entry ->
                        val id = entry.arguments?.getInt("id") ?: return@composable
                        DetalleScreen(id, vm) { navController.popBackStack() }
                    }
                }
            }
        }
    }
}
