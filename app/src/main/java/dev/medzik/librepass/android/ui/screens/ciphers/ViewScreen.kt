package dev.medzik.librepass.android.ui.screens.ciphers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import dev.medzik.librepass.android.ui.composables.CipherGroup
import dev.medzik.librepass.android.ui.composables.common.TopBar
import dev.medzik.librepass.android.ui.composables.common.TopBarBackIcon
import dev.medzik.librepass.android.utils.navigation.getString
import dev.medzik.librepass.android.utils.navigation.navigate
import dev.medzik.librepass.types.cipher.Cipher
import java.util.UUID

@Composable
fun CipherViewScreen(navController: NavController) {
    // get encryption key from navController
    val secretKey = navController.getString(Argument.SecretKey)
        ?: return
    // get cipher id from navController
    val cipherId = navController.getString(Argument.CipherId)
        ?: return

    // get cipher from repository
    val repository = Repository(context = LocalContext.current)
    val cipher = repository.cipher.get(UUID.fromString(cipherId))!!.encryptedCipher
    val cipherData = Cipher(cipher, secretKey).loginData!!

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
                    screen = Screen.CipherEdit,
                    arguments = listOf(
                        Argument.SecretKey to secretKey,
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                CipherField(title = stringResource(id = R.string.CipherField_Name), value = cipherData.name)
            }

            if (!cipherData.username.isNullOrEmpty() || !cipherData.password.isNullOrEmpty()) {
                item {
                    CipherGroup(stringResource(id = R.string.CipherField_Group_Login)) {
                        CipherField(
                            title = stringResource(id = R.string.CipherField_Username),
                            value = cipherData.username,
                            copy = true
                        )
                        CipherField(
                            title = stringResource(id = R.string.CipherField_Password),
                            value = cipherData.password,
                            copy = true,
                            hidden = true
                        )
                    }
                }
            }

            item {
                if (!cipherData.uris.isNullOrEmpty()) {
                    CipherGroup(stringResource(id = R.string.CipherField_Group_Website)) {
                        cipherData.uris?.forEachIndexed { index, it ->
                            CipherField(
                                title = stringResource(id = R.string.CipherField_URL) + " ${index + 1}",
                                value = it,
                                copy = true
                            )
                        }
                    }
                }
            }

            if (!cipherData.notes.isNullOrEmpty()) {
                item {
                    CipherGroup(stringResource(id = R.string.CipherField_Group_Other)) {
                        CipherField(
                            title = stringResource(id = R.string.CipherField_Notes),
                            value = cipherData.notes,
                            copy = true
                        )
                    }
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

    var hiddenState by remember { mutableStateOf(hidden) }

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

            if (hiddenState) {
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
                IconButton(onClick = { hiddenState = !hiddenState }) {
                    Icon(
                        imageVector = if (hiddenState) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = null
                    )
                }
            }

            if (copy) {
                IconButton(onClick = { clipboardManager.setText(AnnotatedString(value)) }) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null
                    )
                }
            }
        }
    }
}
