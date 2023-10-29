package dev.medzik.android.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class RememberTests {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testRememberMutable() {
        composeTestRule.setContent {
            var clicked by rememberMutable(0)

            Column {
                Text(text = "Clicked: $clicked")

                Button(onClick = { clicked++ }) {
                    Text(text = "Click me")
                }
            }
        }

        // click the button two times
        repeat(2) {
            composeTestRule.onNodeWithText("Click me").performClick()
        }

        // check if the text changed
        composeTestRule.onNodeWithText("Clicked: 2").assertExists()
    }

    @Test
    fun testRememberMutableString() {
        composeTestRule.setContent {
            var value by rememberMutableString()

            Column {
                Text(text = "Current Value: $value")

                Button(onClick = { value = "test" }) {
                    Text(text = "Click me")
                }
            }
        }

        // click button
        composeTestRule.onNodeWithText("Click me").performClick()
        // check if the text changed
        composeTestRule.onNodeWithText("Current Value: test").assertExists()
    }

    @Test
    fun testRememberMutableBoolean() {
        composeTestRule.setContent {
            var value by rememberMutableBoolean()

            Column {
                Text(text = "Current Value: $value")

                Button(onClick = { value = true }) {
                    Text(text = "Click me")
                }
            }
        }

        // click button
        composeTestRule.onNodeWithText("Click me").performClick()
        // check if the text changed
        composeTestRule.onNodeWithText("Current Value: true").assertExists()
    }
}
