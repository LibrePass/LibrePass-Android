package dev.medzik.librepass.android.ui.screens.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.InvertColors
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.jakewharton.processphoenix.ProcessPhoenix
import dev.medzik.android.cryptoutils.KeyStoreUtils
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.data.getRepository
import dev.medzik.librepass.android.ui.composables.Group
import dev.medzik.librepass.android.utils.Biometric
import dev.medzik.librepass.android.utils.DataStoreKey
import dev.medzik.librepass.android.utils.getUserSecretsSync
import dev.medzik.librepass.android.utils.readKeyFromDataStore
import dev.medzik.librepass.android.utils.writeKeyToDataStore
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    // context must be FragmentActivity to show biometric prompt
    val context = LocalContext.current as FragmentActivity

    val userSecrets = context.getUserSecretsSync() ?: return

    val repository = context.getRepository()
    val credentials = repository.credentials.get()!!

    val scope = rememberCoroutineScope()

    var biometricEnabled by remember { mutableStateOf(credentials.biometricEnabled) }
    val theme = context.readKeyFromDataStore(DataStoreKey.Theme)
    val dynamicColor = context.readKeyFromDataStore(DataStoreKey.DynamicColor)

    // Biometric checked event handler (enable/disable biometric authentication)
    fun biometricChecked() {
        if (biometricEnabled) {
            biometricEnabled = false

            scope.launch {
                repository.credentials.update(
                    credentials.copy(
                        biometricEnabled = false
                    )
                )
            }

            return
        }

        Biometric.showBiometricPrompt(
            context = context,
            cipher = KeyStoreUtils.initCipherForEncryption(
                Biometric.PrivateKeyAlias,
                true
            ),
            onAuthenticationSucceeded = { cipher ->
                val encryptedData = KeyStoreUtils.encrypt(
                    cipher = cipher,
                    data = userSecrets.privateKey
                )

                biometricEnabled = true

                scope.launch {
                    repository.credentials.update(
                        credentials.copy(
                            biometricEnabled = true,
                            biometricProtectedPrivateKey = encryptedData.cipherText,
                            biometricProtectedPrivateKeyIV = encryptedData.initializationVector
                        )
                    )
                }
            },
            onAuthenticationFailed = { }
        )
    }

    @Composable
    fun SettingsSwitcher(
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
                onCheckedChange = onCheckedChange,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }

    var themeSelectorExpanded by remember { mutableStateOf(false) }

    fun changeTheme(id: Int) {
        context.writeKeyToDataStore(DataStoreKey.Theme, id)

        // restart application to apply changes
        ProcessPhoenix.triggerRebirth(context)
    }

    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
        item {
            Group(
                name = stringResource(R.string.Settings_Group_Appearance)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.TopStart)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 16.dp)
                        )

                        Text(
                            text = stringResource(R.string.Settings_Theme),
                            modifier = Modifier.weight(1f)
                        )

                        TextButton(
                            onClick = { themeSelectorExpanded = true },
                            modifier = Modifier.padding(start = 16.dp)
                        ) {
                            Text(
                                when (theme) {
                                    0 -> stringResource(R.string.Settings_SystemDefault)
                                    1 -> stringResource(R.string.Settings_Light)
                                    2 -> stringResource(R.string.Settings_Dark)
                                    // never happens
                                    else -> ""
                                }
                            )
                        }

                        DropdownMenu(
                            expanded = themeSelectorExpanded,
                            onDismissRequest = { themeSelectorExpanded = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.Settings_SystemDefault)) },
                                onClick = { changeTheme(0) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.InvertColors,
                                        contentDescription = null
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.Settings_Light)) },
                                onClick = { changeTheme(1) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.LightMode,
                                        contentDescription = null
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.Settings_Dark)) },
                                onClick = { changeTheme(2) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.DarkMode,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }

                SettingsSwitcher(
                    icon = Icons.Default.ColorLens,
                    text = stringResource(R.string.Settings_MaterialYou),
                    checked = dynamicColor,
                    onCheckedChange = {
                        context.writeKeyToDataStore(DataStoreKey.DynamicColor, it)

                        // restart application to apply changes
                        ProcessPhoenix.triggerRebirth(context)
                    }
                )
            }
        }

        item {
            Group(
                name = stringResource(R.string.Settings_Group_Security)
            ) {
                SettingsSwitcher(
                    icon = Icons.Default.Fingerprint,
                    text = stringResource(R.string.Settings_BiometricUnlock),
                    checked = biometricEnabled,
                    onCheckedChange = { biometricChecked() }
                )
            }
        }
    }
}
