package dev.medzik.librepass.android.common

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptionsBuilder

fun NavOptionsBuilder.popUpToDestination(navController: NavController) {
    popUpTo(navController.graph.findStartDestination().id) {
        saveState = false
        inclusive = true
    }
}
