package dev.medzik.librepass.android.ui.composable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun TextInputField(
    label: String,
    hidden: Boolean = false,
    state: MutableState<String>,
    isError: Boolean = false,
    errorMessage: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    val hiddenState = remember { mutableStateOf(hidden) }

    OutlinedTextField(
        value = state.value,
        onValueChange = { state.value = it },
        label = { Text(label) },
        visualTransformation =
        if (hidden && hiddenState.value) PasswordVisualTransformation()
        else VisualTransformation.None,
        trailingIcon = {
            if (hidden) {
                // add icon
                val icon = if (hiddenState.value) {
                    Icons.Filled.Visibility
                } else {
                    Icons.Filled.VisibilityOff
                }

                // localize description for accessibility
                val description = if (hiddenState.value) {
                    "Show password"
                } else {
                    "Hide password"
                }

                // add icon button
                IconButton(onClick = { hiddenState.value = !hiddenState.value }) {
                    Icon(
                        imageVector = icon,
                        contentDescription = description,
                    )
                }
            }
        },
        supportingText = {
            if (isError) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                )
            } else {
                Text(text = "")
            }
        },
        isError = isError,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
