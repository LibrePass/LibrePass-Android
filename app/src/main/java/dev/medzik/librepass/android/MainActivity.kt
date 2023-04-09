package dev.medzik.librepass.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.medzik.librepass.android.data.Repository
import dev.medzik.librepass.android.ui.screens.DashboardScreen
import dev.medzik.librepass.android.ui.screens.LoginScreen
import dev.medzik.librepass.android.ui.theme.LibrePassTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LibrePassTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxSize()
                ) {
                    LibrePassApp()
                }
            }
        }
    }
}

@Composable
fun LibrePassApp() {
    val navController = rememberNavController()

    val repository = Repository(context = LocalContext.current)

    NavHost(navController = navController, startDestination = repository.get()?.let { "dashboard" } ?: "login") {
        composable("login") {
            LoginScreen(navController = navController)
        }

        composable("dashboard") {
            DashboardScreen()
        }
    }
}
