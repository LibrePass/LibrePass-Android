package dev.medzik.android.components

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

/** A navigation argument interface that represents an argument for a navigation screens. */
interface NavArgument {
    val name: String
}

/**
 * A navigation screen interface that represents a destination within a navigation graph.
 * @property args An optional array of [NavArgument] objects representing the arguments required for this screen.
 */
interface NavScreen {
    val name: String
    val args: Array<NavArgument>?

    /**
     * Returns the route destination for the screen without filling arguments.
     * @return The route destination as a string.
     */
    fun getRoute(): String {
        return if (args != null) {
            "${name.lowercase()}/${args!!.joinToString("/") { "{${it.name.lowercase()}}" }}"
        } else {
            name.lowercase() // if no arguments, return route without arguments
        }
    }

    /**
     * Returns the route destination for the screen with filled arguments.
     * @param args Pairs of [NavArgument] and their corresponding argument values.
     * @return The route destination as a string with filled arguments.
     * @throws IllegalArgumentException if the number of provided arguments does not match the expected count.
     */
    fun fill(vararg args: Pair<NavArgument, String>): String {
        if (args.size != (this.args?.size ?: 0))
            throw IllegalArgumentException("Invalid number of arguments. Expected ${this.args?.size}, got ${args.size}")

        var route = getRoute()
        for (arg in args)
            route = route.replace("{${arg.first.name.lowercase()}}", arg.second)

        return route
    }
}

/** Gets the value of argument from navigation controller */
fun NavController.getString(argument: NavArgument): String? {
    return currentBackStackEntry?.arguments?.getString(argument.name.lowercase())
}

/**
 * Navigates to the specified [NavScreen] with the provided [NavArgument] values.
 * @param screen the target screen to navigate to
 * @param args a list of argument pairs in the form of [NavArgument] and [String] values
 * @param disableBack determines whether to disable the "back" navigation from this screen
 * @param builderOptions a function that allows customization of custom navigation options
 */
fun NavController.navigate(
    screen: NavScreen,
    args: Array<Pair<NavArgument, String>>? = null,
    disableBack: Boolean = false,
    builderOptions: (NavOptionsBuilder.() -> Unit)? = null
) {
    val route = if (args != null) screen.fill(*args) else screen.fill()

    navigate(
        route = route,
        builder = {
            // Disable back navigation
            if (disableBack) popUpTo(graph.startDestinationId) { inclusive = true }

            if (builderOptions != null) builderOptions()
        }
    )
}
