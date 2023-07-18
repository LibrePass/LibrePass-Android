package dev.medzik.librepass.android.utils

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import dev.medzik.librepass.android.ui.Argument
import dev.medzik.librepass.android.ui.Screen

object Navigation {
    /**
     * Get string argument from current screen in [NavController].
     */
    fun NavController.getString(argument: Argument): String? {
        return currentBackStackEntry?.arguments?.getString(argument.name)
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
        disableBack: Boolean = false,
        options: (NavOptionsBuilder.() -> Unit)? = null
    ) {
        navigate(
            route = screen.fill(argument),
            builder = {
                if (disableBack)
                    popUpTo(graph.startDestinationId) { inclusive = true }

                if (options != null)
                    options()
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
        disableBack: Boolean = false,
        options: (NavOptionsBuilder.() -> Unit)? = null
    ) {
        navigate(
            route = screen.fill(),
            builder = {
                if (disableBack)
                    popUpTo(graph.startDestinationId) { inclusive = true }

                if (options != null)
                    options()
            }
        )
    }
}
