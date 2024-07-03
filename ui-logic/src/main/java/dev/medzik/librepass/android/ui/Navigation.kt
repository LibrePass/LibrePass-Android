package dev.medzik.librepass.android.ui

import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.medzik.android.compose.navigation.NavigationAnimations
import dev.medzik.librepass.android.ui.auth.authGraph

@Composable
fun LibrePassNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController,
        startDestination = Home,
        modifier = Modifier.imePadding(),
        enterTransition = {
            NavigationAnimations.enterTransition()
        },
        exitTransition = {
            NavigationAnimations.exitTransition()
        },
        popEnterTransition = {
            NavigationAnimations.popEnterTransition()
        },
        popExitTransition = {
            NavigationAnimations.popExitTransition()
        }
    ) {
        composable<Home> {
            HomeScreen(navController)
        }

        authGraph(navController)
    }
}
