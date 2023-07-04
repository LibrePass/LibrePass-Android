package dev.medzik.librepass.android.utils

import androidx.navigation.NavController
import dev.medzik.librepass.android.ui.Argument
import dev.medzik.librepass.android.ui.Screen

object Navigation {
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
     * @param disableBack [Boolean] to disable back navigation.
     */
    fun NavController.navigate(
        screen: Screen,
        arguments: List<Pair<Argument, String>>,
        disableBack: Boolean = false
    ) {
        val args = arguments.toTypedArray()
        navigate(
            route = screen.fill(*args),
            builder = {
                if (disableBack) {
                    popUpTo(graph.startDestinationId) {
                        inclusive = true
                    }
                }
            }
        )
    }

    /**
     * Navigate to [Screen] with given [argument].
     * @param screen [Screen] to navigate to.
     * @param argument Pair of [Argument] and [String].
     * @param disableBack [Boolean] to disable back navigation.
     */
    fun NavController.navigate(
        screen: Screen,
        argument: Pair<Argument, String>,
        disableBack: Boolean = false
    ) {
        navigate(
            route = screen.fill(argument),
            builder = {
                if (disableBack) {
                    popUpTo(graph.startDestinationId) {
                        inclusive = true
                    }
                }
            }
        )
    }

    /**
     * Navigate to [Screen] without arguments.
     * @param screen [Screen] to navigate to.
     * @param disableBack [Boolean] to disable back navigation.
     */
    fun NavController.navigate(
        screen: Screen,
        disableBack: Boolean = false
    ) {
        navigate(
            route = screen.fill(),
            builder = {
                if (disableBack) {
                    popUpTo(graph.startDestinationId) {
                        inclusive = true
                    }
                }
            }
        )
    }
}
