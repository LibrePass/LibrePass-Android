package dev.medzik.librepass.android.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.ui.composables.common.TopBar
import dev.medzik.librepass.android.ui.composables.common.TopBarBackIcon
import java.util.Random

@Composable
fun PasswordGenerator(navController: NavController) {
    var password by remember { mutableStateOf("") }

    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(id = R.string.password_generator_topbar),
                navigationIcon = {
                    TopBarBackIcon(navController = navController)
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Show password text with, next to it, a button to copy it to clipboard with fixed width
            Row {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    trailingIcon = {
                        Row {
                            IconButton(
                                onClick = { clipboardManager.setText(AnnotatedString(password)) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = stringResource(id = R.string.copy_to_clipboard)
                                )
                            }

                            IconButton(
                                onClick = {
                                    val leftLimit = 48 // numeral '0'

                                    val rightLimit = 122 // letter 'z'

                                    val size = 10L

                                    val random = Random()

                                    password =
                                        random.ints(leftLimit, rightLimit + 1)
                                            .filter { i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97) }
                                            .limit(size)
                                            .collect(
                                                { StringBuilder() },
                                                java.lang.StringBuilder::appendCodePoint,
                                                java.lang.StringBuilder::append
                                            )
                                            .toString()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = stringResource(id = R.string.generate_password)
                                )
                            }
                        }
                    },
                    readOnly = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // submit button
            Button(
                // center horizontally
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 90.dp)
                    .padding(top = 16.dp),
                onClick = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("password", password)
                    navController.popBackStack()
                }
            ) {
                Text(text = stringResource(id = R.string.submit))
            }
        }
    }
}
