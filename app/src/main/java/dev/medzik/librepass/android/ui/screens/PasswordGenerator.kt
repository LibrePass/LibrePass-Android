package dev.medzik.librepass.android.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.ui.composables.common.TopBar
import dev.medzik.librepass.android.ui.composables.common.TopBarBackIcon
import dev.medzik.librepass.android.utils.DataStore.readKeyFromDataStore
import dev.medzik.librepass.android.utils.DataStore.writeKeyToDataStore
import dev.medzik.librepass.android.utils.DataStoreKey
import dev.medzik.librepass.android.utils.Remember.rememberStringData
import java.util.Random

enum class PasswordType(val literals: String) {
    NUMERIC("1234567890"),
    LOWERCASE("abcdefghijklmnopqrstuvwxyz"),
    UPPERCASE("ABCDEFGHIJKLMNOPQRSTUVWXYZ"),
    SYMBOLS("!@#\$%^&*()_+-=[]{}\\|;:'\",.<>/?")
}

@Composable
fun PasswordGenerator(navController: NavController) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var generatedPassword by rememberStringData()

    // generator options
    var passwordLength by remember { mutableIntStateOf(context.readKeyFromDataStore(DataStoreKey.PasswordLength)) }
    var withCapitalLetters by remember { mutableStateOf(context.readKeyFromDataStore(DataStoreKey.PasswordCapitalize)) }
    var withNumbers by remember { mutableStateOf(context.readKeyFromDataStore(DataStoreKey.PasswordIncludeNumbers)) }
    var withSymbols by remember { mutableStateOf(context.readKeyFromDataStore(DataStoreKey.PasswordIncludeSymbols)) }

    @Composable
    fun TypeSwitcher(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                modifier = Modifier.weight(1f)
            )

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }

    /**
     * Generates password based on selected options and returns it.
     */
    fun generatePassword(): String {
        var letters = PasswordType.LOWERCASE.literals

        if (withCapitalLetters) {
            letters += PasswordType.UPPERCASE.literals
        }

        if (withNumbers) {
            letters += PasswordType.NUMERIC.literals
        }

        if (withSymbols) {
            letters += PasswordType.SYMBOLS.literals
        }

        return (1..passwordLength)
            .map { Random().nextInt(letters.length) }
            .map(letters::get)
            .joinToString("")
    }

    // regenerate on options change
    LaunchedEffect(passwordLength, withCapitalLetters, withNumbers, withSymbols) {
        generatedPassword = generatePassword()
    }

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.TopBar_PasswordGenerator),
                navigationIcon = { TopBarBackIcon(navController) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
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
                                    imageVector = Icons.Default.Refresh,
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
                    value = passwordLength.toString(),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    label = { Text(text = stringResource(R.string.PasswordGenerator_Length)) },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    onValueChange = {
                        if (it.length in 1..3 && it.toInt() <= 256)
                            context.writeKeyToDataStore(DataStoreKey.PasswordLength, it.toInt())
                    }
                )

                // - and + buttons
                IconButton(
                    onClick = {
                        if (passwordLength > 1) {
                            passwordLength--
                            context.writeKeyToDataStore(DataStoreKey.PasswordLength, passwordLength)
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
                        if (passwordLength < 256) {
                            passwordLength++
                            context.writeKeyToDataStore(DataStoreKey.PasswordLength, passwordLength)
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
            TypeSwitcher(
                text = stringResource(R.string.PasswordGenerator_CapitalLetters),
                checked = withCapitalLetters,
                onCheckedChange = {
                    withCapitalLetters = it
                    context.writeKeyToDataStore(DataStoreKey.PasswordCapitalize, it)
                }
            )

            // Numeric switch
            TypeSwitcher(
                text = stringResource(R.string.PasswordGenerator_Numbers),
                checked = withNumbers,
                onCheckedChange = {
                    withNumbers = it
                    context.writeKeyToDataStore(DataStoreKey.PasswordIncludeNumbers, it)
                }
            )

            // Symbols switch
            TypeSwitcher(
                text = stringResource(R.string.PasswordGenerator_Symbols),
                checked = withSymbols,
                onCheckedChange = {
                    withSymbols = it
                    context.writeKeyToDataStore(DataStoreKey.PasswordIncludeSymbols, it)
                }
            )

            // Submit button
            Button(
                // center horizontally
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 90.dp)
                    .padding(top = 16.dp),
                onClick = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("password", generatedPassword)
                    navController.popBackStack()
                }
            ) {
                Text(text = stringResource(R.string.PasswordGenerator_Submit))
            }
        }
    }
}
