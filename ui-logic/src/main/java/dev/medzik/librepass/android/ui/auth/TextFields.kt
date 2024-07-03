package dev.medzik.librepass.android.ui.auth

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import dev.medzik.android.compose.ui.IconBox
import dev.medzik.android.compose.ui.textfield.AnimatedTextField
import dev.medzik.android.compose.ui.textfield.PasswordAnimatedTextField
import dev.medzik.android.compose.ui.textfield.TextFieldValue
import dev.medzik.librepass.android.ui.R

@Composable
fun EmailTextField(value: TextFieldValue) {
    AnimatedTextField(
        label = stringResource(R.string.Email),
        value = value,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email
        ),
        leading = { IconBox(Icons.Default.Email) },
        singleLine = true
    )
}

@Composable
fun PasswordTextField(value: TextFieldValue) {
    PasswordAnimatedTextField(
        label = stringResource(R.string.Password),
        value = value,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email
        )
    )
}
