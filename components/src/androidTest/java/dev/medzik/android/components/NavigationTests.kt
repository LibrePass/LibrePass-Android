package dev.medzik.android.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.junit.Rule
import org.junit.Test

enum class Argument : NavArgument {
    ID,
    Name
}

enum class Screen(override val args: Array<NavArgument>? = null) : NavScreen {
    Home,
    Example(arrayOf(Argument.ID, Argument.Name))
}

class NavigationTests {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testNavigation() {
        composeTestRule.setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = Screen.Home.getRoute()) {
                composable(Screen.Home.getRoute()) {
                    Column {
                        Text("Home Screen")

                        Button(onClick = {
                            navController.navigate(
                                screen = Screen.Example,
                                args =
                                    arrayOf(
                                        Argument.ID to "test id",
                                        Argument.Name to "test name"
                                    )
                            )
                        }) {
                            Text("Click me to go to Example screen")
                        }
                    }
                }

                composable(Screen.Example.getRoute()) {
                    val id = navController.getString(Argument.ID)
                    val name = navController.getString(Argument.Name)

                    Column {
                        Text("Example Screen")
                        Text("ID: $id")
                        Text("Name: $name")
                    }
                }
            }
        }

        composeTestRule.onNodeWithText("Home Screen").assertExists()

        // go to example screen
        composeTestRule.onNodeWithText("Click me to go to Example screen").performClick()
        composeTestRule.onNodeWithText("Example Screen").assertExists()

        // check arguments
        composeTestRule.onNodeWithText("ID: test id").assertExists()
        composeTestRule.onNodeWithText("Name: test name").assertExists()
    }
}
