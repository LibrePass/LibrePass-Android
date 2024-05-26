package dev.medzik.librepass.android.utils

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptionsBuilder

fun NavOptionsBuilder.popUpToDestination(navController: NavController) {
    popUpTo(navController.graph.findStartDestination().id) {
        saveState = false
        inclusive = true
    }
}
