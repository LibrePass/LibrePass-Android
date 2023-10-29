package dev.medzik.android.components

import org.junit.Assert.assertEquals
import org.junit.Test

enum class Argument : NavArgument {
    Test,
    Name
}

enum class Screen(override val args: Array<NavArgument>? = null) : NavScreen {
    Home,
    Example(arrayOf(Argument.Test, Argument.Name))
}

class NavigationTests {
    @Test
    fun testGetRoute() {
        assertEquals("home", Screen.Home.getRoute())
        assertEquals("example/{test}/{name}", Screen.Example.getRoute())
    }

    @Test
    fun testFillRoute() {
        assertEquals("home", Screen.Home.fill())

        val filledExampleScreen =
            Screen.Example.fill(
                Argument.Test to "test",
                Argument.Name to "example_name"
            )
        assertEquals("example/test/example_name", filledExampleScreen)
    }
}
