package dev.medzik.librepass.android.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.medzik.librepass.android.data.Repository
import dev.medzik.librepass.android.ui.Argument
import dev.medzik.librepass.android.ui.composable.common.TopBar
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
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null // stringResource(id = R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Item details",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )

            CipherField(title = "Name", value = cipherData.name)

            Group(name = "Login") {
                CipherField(title = "Username", value = cipherData.username, copy = true)
                CipherField(title = "Password", value = cipherData.password, copy = true, hidden = true)
            }

            Group(name = "Website") {
                CipherField(title = "URL", value = cipherData.uris?.get(0), copy = true)
            }

            Group(name = "Other") {
                CipherField(title = "Notes", value = cipherData.notes, copy = true)
            }
        }
    }
}

@Composable
fun CipherField(
    title: String,
    value: String?,
    hidden: Boolean = false,
    copy: Boolean = false,
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
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Row {
            if (hidden) {
                IconButton(onClick = { hiddenState.value = !hiddenState.value }) {
                    Icon(
                        imageVector = if (hiddenState.value) { Icons.Filled.Visibility } else { Icons.Filled.VisibilityOff },
                        contentDescription = if (hiddenState.value) { "Show password" } else { "Hide password" },
                    )
                }
            }

            if (copy) {
                IconButton(onClick = { clipboardManager.setText(AnnotatedString(value)) }) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy to clipboard",
                    )
                }
            }
        }
    }
}
