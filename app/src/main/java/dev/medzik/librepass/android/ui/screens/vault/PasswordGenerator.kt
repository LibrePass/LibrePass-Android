package dev.medzik.librepass.android.ui.screens.vault

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.medzik.android.components.rememberMutable
import dev.medzik.android.components.rememberMutableString
import dev.medzik.android.components.ui.SwitcherPreference
import dev.medzik.android.utils.runOnIOThread
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.database.datastore.PasswordGeneratorPreference
import dev.medzik.librepass.android.database.datastore.readPasswordGeneratorPreference
import dev.medzik.librepass.android.database.datastore.writePasswordGeneratorPreference
import kotlinx.serialization.Serializable
import java.util.Random

enum class PasswordType(val literals: String) {
    NUMERIC("1234567890"),
    LOWERCASE("abcdefghijklmnopqrstuvwxyz"),
    UPPERCASE("ABCDEFGHIJKLMNOPQRSTUVWXYZ"),
    SYMBOLS("!@#\$%^&*()_+-=[]{}\\|;:'\",.<>/?")
}

@Serializable
object PasswordGenerator

@Composable
fun PasswordGeneratorScreen(navController: NavController) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var generatedPassword by rememberMutableString()

    var passwordGeneratorPreference by rememberMutable(PasswordGeneratorPreference())
    LaunchedEffect(Unit) {
        passwordGeneratorPreference = readPasswordGeneratorPreference(context)
    }

    fun generatePassword(): String {
        var letters = PasswordType.LOWERCASE.literals

        if (passwordGeneratorPreference.capitalize) {
            letters += PasswordType.UPPERCASE.literals
        }

        if (passwordGeneratorPreference.includeNumbers) {
            letters += PasswordType.NUMERIC.literals
        }

        if (passwordGeneratorPreference.includeSymbols) {
            letters += PasswordType.SYMBOLS.literals
        }

        return (1..passwordGeneratorPreference.length)
            .map { Random().nextInt(letters.length) }
            .map(letters::get)
            .joinToString("")
    }

    // regenerate on options change
    LaunchedEffect(passwordGeneratorPreference) {
        generatedPassword = generatePassword()
    }

    Row {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = generatedPassword,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                Row {
                    IconButton(
                        onClick = { clipboardManager.setText(AnnotatedString(generatedPassword)) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null
                        )
                    }

                    IconButton(
                        onClick = { generatedPassword = generatePassword() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null
                        )
                    }
                }
            },
        )
    }

    Spacer(modifier = Modifier.padding(top = 8.dp))

    // Password length options
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = passwordGeneratorPreference.length.toString(),
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            label = { Text(stringResource(R.string.PasswordGenerator_Length)) },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            onValueChange = {
                try {
                    if (it.length in 1..3 && it.toInt() <= 256) {
                        passwordGeneratorPreference = passwordGeneratorPreference.copy(length = it.toInt())
                        runOnIOThread { writePasswordGeneratorPreference(context, passwordGeneratorPreference) }
                    }
                } catch (e: NumberFormatException) {
                    // ignore, just do not update input value
                }
            }
        )

        // - and + buttons
        IconButton(
            onClick = {
                if (passwordGeneratorPreference.length > 1) {
                    passwordGeneratorPreference = passwordGeneratorPreference.copy(
                        length = passwordGeneratorPreference.length - 1
                    )
                    runOnIOThread { writePasswordGeneratorPreference(context, passwordGeneratorPreference) }
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = null
            )
        }
        IconButton(
            onClick = {
                if (passwordGeneratorPreference.length < 256) {
                    passwordGeneratorPreference = passwordGeneratorPreference.copy(
                        length = passwordGeneratorPreference.length + 1
                    )
                    runOnIOThread { writePasswordGeneratorPreference(context, passwordGeneratorPreference) }
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
        }
    }

    // Capital letters switch
    SwitcherPreference(
        title = stringResource(R.string.PasswordGenerator_CapitalLetters),
        checked = passwordGeneratorPreference.capitalize,
        onCheckedChange = {
            passwordGeneratorPreference = passwordGeneratorPreference.copy(capitalize = it)
            runOnIOThread { writePasswordGeneratorPreference(context, passwordGeneratorPreference) }
        }
    )

    // Numeric switch
    SwitcherPreference(
        title = stringResource(R.string.PasswordGenerator_Numbers),
        checked = passwordGeneratorPreference.includeNumbers,
        onCheckedChange = {
            passwordGeneratorPreference = passwordGeneratorPreference.copy(includeNumbers = it)
            runOnIOThread { writePasswordGeneratorPreference(context, passwordGeneratorPreference) }
        }
    )

    // Symbols switch
    SwitcherPreference(
        title = stringResource(R.string.PasswordGenerator_Symbols),
        checked = passwordGeneratorPreference.includeSymbols,
        onCheckedChange = {
            passwordGeneratorPreference = passwordGeneratorPreference.copy(includeSymbols = it)
            runOnIOThread { writePasswordGeneratorPreference(context, passwordGeneratorPreference) }
        }
    )

    Button(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 90.dp)
            .padding(top = 16.dp),
        onClick = {
            navController.previousBackStackEntry!!.savedStateHandle["password"] = generatedPassword
            navController.popBackStack()
        }
    ) {
        Text(stringResource(R.string.Submit))
    }
}
