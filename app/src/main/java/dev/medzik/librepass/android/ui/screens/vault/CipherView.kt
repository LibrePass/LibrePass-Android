package dev.medzik.librepass.android.ui.screens.vault

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.OpenInNew
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.medzik.android.components.BaseDialog
import dev.medzik.android.components.SecondaryText
import dev.medzik.android.components.getString
import dev.medzik.android.components.navigate
import dev.medzik.android.components.rememberDialogState
import dev.medzik.android.utils.showToast
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.data.getRepository
import dev.medzik.librepass.android.ui.Argument
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.ui.components.TopBar
import dev.medzik.librepass.android.ui.components.TopBarBackIcon
import dev.medzik.librepass.android.utils.SHORTEN_NAME_LENGTH
import dev.medzik.librepass.android.utils.SecretStore.getUserSecrets
import dev.medzik.librepass.android.utils.shorten
import dev.medzik.librepass.types.cipher.Cipher
import dev.medzik.librepass.types.cipher.CipherType
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

@Composable
fun CipherViewScreen(navController: NavController) {
    val cipherId =
        navController.getString(Argument.CipherId)
            ?: return

    val context = LocalContext.current

    val userSecrets =
        context.getUserSecrets()
            ?: return

    val encryptedCipher = context.getRepository().cipher.get(UUID.fromString(cipherId))!!.encryptedCipher
    val cipher =
        try {
            Cipher(encryptedCipher, userSecrets.secretKey)
        } catch (e: Exception) {
            // handle decryption error
            DecryptionError(navController, e)
            return
        }

    @Composable
    fun CipherViewLogin() {
        val cipherData = cipher.loginData!!

        CipherField(
            title = stringResource(R.string.Name),
            value = cipherData.name
        )

        if (!cipherData.username.isNullOrEmpty() || !cipherData.password.isNullOrEmpty()) {
            SecondaryText(
                stringResource(R.string.LoginDetails),
                modifier = Modifier.padding(top = 8.dp)
            )

            CipherField(
                title = stringResource(R.string.Username),
                value = cipherData.username,
                copy = true
            )

            val passwordHistoryDialog = rememberDialogState()

            CipherField(
                title = stringResource(R.string.Password),
                value = cipherData.password,
                copy = true,
                hidden = true,
                customIcon = {
                    if (cipherData.passwordHistory != null) {
                        IconButton(onClick = { passwordHistoryDialog.show() }) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null
                            )
                        }
                    }
                }
            )

            BaseDialog(state = passwordHistoryDialog) {
                val clipboardManager = LocalClipboardManager.current
                val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                val passwords =
                    (cipherData.passwordHistory ?: return@BaseDialog).asReversed()

                LazyColumn(modifier = Modifier.padding(horizontal = 24.dp)) {
                    for (i in passwords.indices) {
                        item {
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = parser.format(passwords[i].lastUsed),
                                        style = MaterialTheme.typography.titleSmall,
                                        color =
                                            MaterialTheme.colorScheme.onSurface.copy(
                                                alpha = 0.6f
                                            )
                                    )

                                    Text(
                                        text = passwords[i].password,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                IconButton(onClick = {
                                    clipboardManager.setText(
                                        AnnotatedString(passwords[i].password)
                                    )
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (!cipherData.uris.isNullOrEmpty()) {
                SecondaryText(
                    stringResource(R.string.WebsiteDetails),
                    modifier = Modifier.padding(top = 8.dp)
                )

                cipherData.uris?.forEachIndexed { index, it ->
                    CipherField(
                        title = stringResource(R.string.WebsiteAddress) + " ${index + 1}",
                        value = it,
                        openUri = true,
                        uri = it,
                        copy = true
                    )
                }
            }

            if (!cipherData.notes.isNullOrEmpty()) {
                SecondaryText(
                    stringResource(R.string.OtherDetails),
                    modifier = Modifier.padding(top = 8.dp)
                )

                CipherField(
                    title = stringResource(R.string.Notes),
                    value = cipherData.notes,
                    copy = true
                )
            }
        }
    }

    @Composable
    fun CipherViewSecureNote() {
        val cipherData = cipher.secureNoteData!!

        CipherField(
            title = stringResource(R.string.Title),
            value = cipherData.title,
            copy = true
        )

        CipherField(
            title = stringResource(R.string.Notes),
            value = cipherData.note,
            copy = true
        )
    }

    @Composable
    fun CipherViewCard() {
        val cipherData = cipher.cardData!!

        CipherField(
            title = stringResource(R.string.Name),
            value = cipherData.name,
            copy = true
        )

        CipherField(
            title = stringResource(R.string.CardholderName),
            value = cipherData.cardholderName,
            copy = true
        )

        CipherField(
            title = stringResource(R.string.CardNumber),
            value = cipherData.number,
            copy = true,
            hidden = true
        )

        if (cipherData.expMonth != null) {
            CipherField(
                title = stringResource(R.string.ExpirationMonth),
                value = cipherData.expMonth.toString(),
                copy = true
            )
        }

        if (cipherData.expYear != null) {
            CipherField(
                title = stringResource(R.string.ExpirationYear),
                value = cipherData.expYear.toString(),
                copy = true
            )
        }

        if (cipherData.code != null) {
            CipherField(
                title = stringResource(R.string.SecurityCode),
                value = cipherData.code,
                copy = true,
                hidden = true
            )
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title =
                    when (cipher.type) {
                        CipherType.Login -> cipher.loginData!!.name
                        CipherType.SecureNote -> cipher.secureNoteData!!.title
                        CipherType.Card -> cipher.cardData!!.cardholderName
                    }.shorten(SHORTEN_NAME_LENGTH),
                navigationIcon = { TopBarBackIcon(navController) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(
                    screen = Screen.CipherEdit,
                    args = arrayOf(Argument.CipherId to cipherId)
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
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
        ) {
            item {
                when (cipher.type) {
                    CipherType.Login -> CipherViewLogin()
                    CipherType.SecureNote -> CipherViewSecureNote()
                    CipherType.Card -> CipherViewCard()
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
    openUri: Boolean = false,
    uri: String? = null,
    copy: Boolean = false,
    customIcon: (@Composable () -> Unit)? = null
) {
    if (value.isNullOrEmpty()) return

    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
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
            if (customIcon != null) customIcon()

            if (hidden) {
                IconButton(onClick = { hiddenState = !hiddenState }) {
                    Icon(
                        imageVector = if (hiddenState) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null
                    )
                }
            }

            if (openUri) {
                IconButton(onClick = {
                    try {
                        var address = uri!!
                        if (!address.contains("http(s)?://".toRegex()))
                            address = "https://$uri"

                        uriHandler.openUri(address)
                    } catch (e: Exception) {
                        context.showToast("No application found for URI: $uri")
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
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

@Composable
fun DecryptionError(
    navController: NavController,
    e: Exception
) {
    Scaffold(
        topBar = {
            TopBar(
                "Error",
                navigationIcon = { TopBarBackIcon(navController) }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Text(e.message ?: "Unknown encryption error")
        }
    }
}
