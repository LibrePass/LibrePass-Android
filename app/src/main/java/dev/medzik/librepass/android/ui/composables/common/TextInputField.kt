package dev.medzik.librepass.android.ui.composables.common

import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import dev.medzik.librepass.android.R

@Composable
fun TextInputField(
    label: String,
    hidden: Boolean = false,
    state: MutableState<String>,
    isError: Boolean = false,
    errorMessage: String = "",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val hiddenState = remember { mutableStateOf(hidden) }

    OutlinedTextField(
        value = state.value,
        onValueChange = { state.value = it },
        label = { Text(label) },
        maxLines = 1,
        singleLine = true,
        visualTransformation = if (hidden && hiddenState.value) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        trailingIcon = {
            if (hidden) {
                IconButton(onClick = { hiddenState.value = !hiddenState.value }) {
                    Icon(
                        imageVector = if (hiddenState.value) {
                            Icons.Filled.Visibility
                        } else {
                            Icons.Filled.VisibilityOff
                        },
                        contentDescription = if (hiddenState.value) {
                            stringResource(id = R.string.show_password)
                        } else {
                            stringResource(id = R.string.show_password)
                        }
                    )
                }
            }
        },
        supportingText = {
            if (isError) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Text(text = "")
            }
        },
        isError = isError,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun TextInputFieldBase(
    label: String,
    modifier: Modifier = Modifier,
    hidden: Boolean = false,
    state: MutableState<String>? = null,
    value: String? = null,
    onValueChange: ((String) -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailingIcon: @Composable () -> Unit = {}
) {
    val hiddenState = remember { mutableStateOf(hidden) }

    if (state == null && onValueChange == null) {
        throw IllegalArgumentException("Either state or onValueChange must be provided")
    }

    OutlinedTextField(
        value = value ?: state?.value ?: "",
        onValueChange = onValueChange ?: { state!!.value = it },
        label = { Text(label) },
        maxLines = 1,
        singleLine = true,
        visualTransformation = if (hidden && hiddenState.value) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        trailingIcon = {
            Row {
                if (hidden) {
                    IconButton(onClick = { hiddenState.value = !hiddenState.value }) {
                        Icon(
                            imageVector = if (hiddenState.value) {
                                Icons.Filled.Visibility
                            } else {
                                Icons.Filled.VisibilityOff
                            },
                            contentDescription = if (hiddenState.value) {
                                stringResource(id = R.string.hide_password)
                            } else {
                                stringResource(id = R.string.show_password)
                            }
                        )
                    }
                }

                trailingIcon()
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        modifier = modifier
    )
}
