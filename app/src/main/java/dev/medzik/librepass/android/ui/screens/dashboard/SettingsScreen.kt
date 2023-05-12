package dev.medzik.librepass.android.ui.screens.dashboard

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.data.Repository
import dev.medzik.librepass.android.ui.Argument
import dev.medzik.librepass.android.ui.composables.common.TopBar
import dev.medzik.librepass.android.utils.KeyStoreAlias
import dev.medzik.librepass.android.utils.KeyStoreUtils
import dev.medzik.librepass.android.utils.navController.getString
import dev.medzik.librepass.android.utils.showBiometricPrompt
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(navController: NavController) {
    // get encryption key from navigation arguments
    val encryptionKey = navController.getString(Argument.EncryptionKey)!!

    // get FragmentActivity from LocalContext
    val context = LocalContext.current as FragmentActivity

    // get credentials from local database
    val repository = Repository(context = context)
    val credentials = repository.credentials.get()!!

    var biometricEnabled by remember { mutableStateOf(credentials.biometricEnabled) }

    // coroutine scope
    val scope = rememberCoroutineScope()

    @Composable
    fun TypeSwitcher(
        icon: ImageVector,
        text: String,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.padding(end = 16.dp)
            )

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

    Scaffold(
        topBar = {
            TopBar(title = stringResource(id = R.string.settings))
        }
    ) { it ->
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .padding(horizontal = 24.dp)
        ) {
            item {
                TypeSwitcher(
                    icon = Icons.Default.Fingerprint,
                    text = stringResource(id = R.string.biometric_unlock),
                    checked = biometricEnabled,
                    onCheckedChange = {
                        // TODO: save to settings
                        if (biometricEnabled) {
                            biometricEnabled = false

                            scope.launch {
                                repository.credentials.update(
                                    credentials.copy(
                                        biometricEnabled = false
                                    )
                                )
                            }

                            return@TypeSwitcher
                        }

                        showBiometricPrompt(
                            context = context,
                            cipher = KeyStoreUtils.getCipherForEncryption(KeyStoreAlias.ENCRYPTION_KEY.name),
                            onAuthenticationSucceeded = { cipher ->
                                val encryptedData = KeyStoreUtils.encrypt(
                                    cipher = cipher,
                                    data = encryptionKey
                                )

                                biometricEnabled = true

                                scope.launch {
                                    repository.credentials.update(
                                        credentials.copy(
                                            biometricEnabled = true,
                                            biometricEncryptionKey = encryptedData.cipherText,
                                            biometricEncryptionKeyIV = encryptedData.initializationVector
                                        )
                                    )
                                }
                            },
                            onAuthenticationFailed = { }
                        )
                    }
                )
            }
        }
    }
}
