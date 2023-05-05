package dev.medzik.librepass.android.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.data.Repository
import dev.medzik.librepass.android.ui.Argument
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.ui.composable.CipherGroup
import dev.medzik.librepass.android.ui.composable.common.TopBar
import dev.medzik.librepass.android.ui.composable.common.TopBarBackIcon
import java.util.UUID

@Composable
fun CipherViewScreen(navController: NavController) {
    val encryptionKey = navController.currentBackStackEntry?.arguments?.getString(Argument.EncryptionKey.get)
        ?: return
    val cipherId = navController.currentBackStackEntry?.arguments?.getString(Argument.CipherId.get)
        ?: return

    val repository = Repository(context = LocalContext.current)

    val cipher = repository.cipher.get(UUID.fromString(cipherId))!!.encryptedCipher
    val cipherData = cipher.decrypt(encryptionKey)

    Scaffold(
        topBar = {
            TopBar(
                title = cipherData.name,
                navigationIcon = {
                    TopBarBackIcon(navController = navController)
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(
                    Screen.CipherEdit.fill(
                        Argument.EncryptionKey to encryptionKey,
                        Argument.CipherId to cipherId
                    )
                )
            }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.item_details),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )

            CipherField(title = stringResource(id = R.string.cipher_field_name), value = cipherData.name)

            if (!cipherData.username.isNullOrEmpty() || cipherData.password.isNullOrEmpty()) {
                CipherGroup(stringResource(id = R.string.cipher_group_login)) {
                    CipherField(
                        title = stringResource(id = R.string.cipher_field_username),
                        value = cipherData.username,
                        copy = true
                    )
                    CipherField(
                        title = stringResource(id = R.string.cipher_field_password),
                        value = cipherData.password,
                        copy = true,
                        hidden = true
                    )
                }
            }

            if (!cipherData.uris.isNullOrEmpty()) {
                CipherGroup(stringResource(id = R.string.cipher_group_website)) {
                    cipherData.uris?.forEachIndexed { index, it ->
                        CipherField(
                            title = stringResource(id = R.string.cipher_field_url) + " ${index + 1}",
                            value = it,
                            copy = true
                        )
                    }
                }
            }

            if (!cipherData.notes.isNullOrEmpty()) {
                CipherGroup(stringResource(id = R.string.cipher_group_other)) {
                    CipherField(
                        title = stringResource(id = R.string.cipher_field_notes),
                        value = cipherData.notes,
                        copy = true
                    )
                }
            }
        }
    }
}

@Composable
fun CipherField(
    title: String,
    value: String?,
    hidden: Boolean = false,
    copy: Boolean = false
) {
    if (value.isNullOrEmpty()) return

    val clipboardManager = LocalClipboardManager.current

    val hiddenState = remember { mutableStateOf(hidden) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            if (hiddenState.value) {
                Text(
                    text = "•".repeat(value.length),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Row {
            if (hidden) {
                IconButton(onClick = { hiddenState.value = !hiddenState.value }) {
                    Icon(
                        imageVector = if (hiddenState.value) { Icons.Filled.Visibility } else { Icons.Filled.VisibilityOff },
                        contentDescription = if (hiddenState.value) {
                            stringResource(id = R.string.show_password)
                        } else {
                            stringResource(id = R.string.show_password)
                        }
                    )
                }
            }

            if (copy) {
                IconButton(onClick = { clipboardManager.setText(AnnotatedString(value)) }) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = stringResource(id = R.string.copy_to_clipboard)
                    )
                }
            }
        }
    }
}
