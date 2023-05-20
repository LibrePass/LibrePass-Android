package dev.medzik.librepass.android.utils.navigation

import androidx.navigation.NavController
import dev.medzik.librepass.android.ui.Argument
import dev.medzik.librepass.android.ui.Screen

/**
 * Get string argument from current screen in [NavController].
 */
fun NavController.getString(argument: Argument): String? {
    return currentBackStackEntry?.arguments?.getString(argument.get)
}

/**
 * Navigate to [Screen] with given [arguments].
 * @param screen [Screen] to navigate to.
 * @param arguments Pair of [Argument] and [String].
 */
fun NavController.navigate(screen: Screen, arguments: List<Pair<Argument, String>>) {
    val args = arguments.toTypedArray()
    navigate(screen.fill(*args))
}

/**
 * Navigate to [Screen] with given [argument].
 * @param screen [Screen] to navigate to.
 * @param argument Pair of [Argument] and [String].
 */
fun NavController.navigate(screen: Screen, argument: Pair<Argument, String>) {
    navigate(screen.fill(argument))
}
