package dev.medzik.librepass.android.common

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptionsBuilder

fun NavOptionsBuilder.popUpToStartDestination(navController: NavController) {
    popUpTo(navController.graph.findStartDestination().id) {
        saveState = false
        inclusive = true
    }
}

inline fun <reified T : Any> NavOptionsBuilder.popUpToDestination(destination: T) {
    popUpTo(destination) {
        saveState = false
        inclusive = true
    }
}
