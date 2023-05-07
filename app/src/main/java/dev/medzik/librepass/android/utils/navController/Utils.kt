package dev.medzik.librepass.android.utils.navController

import androidx.navigation.NavController
import dev.medzik.librepass.android.ui.Argument

/**
 * Get string argument from current screen in [NavController].
 */
fun NavController.getString(argument: Argument): String? {
    return currentBackStackEntry?.arguments?.getString(argument.get)
}
