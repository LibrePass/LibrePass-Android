package dev.medzik.librepass.android.ui.screens.dashboard

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.InvertColors
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.jakewharton.processphoenix.ProcessPhoenix
import dev.medzik.android.composables.dialog.PickerDialog
import dev.medzik.android.composables.dialog.rememberDialogState
import dev.medzik.android.composables.settings.SettingsGroup
import dev.medzik.android.composables.settings.SettingsProperty
import dev.medzik.android.composables.settings.SettingsSwitcher
import dev.medzik.android.cryptoutils.KeyStore
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.data.getRepository
import dev.medzik.librepass.android.utils.Biometric
import dev.medzik.librepass.android.utils.SecretStore.getUserSecrets
import dev.medzik.librepass.android.utils.SecretStore.readKey
import dev.medzik.librepass.android.utils.SecretStore.writeKey
import dev.medzik.librepass.android.utils.StoreKey
import dev.medzik.librepass.android.utils.ThemeValues
import dev.medzik.librepass.android.utils.VaultTimeoutValues
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    // context must be FragmentActivity to show biometric prompt
    val context = LocalContext.current as FragmentActivity

    val userSecrets = context.getUserSecrets() ?: return

    val repository = context.getRepository()
    val credentials = repository.credentials.get()!!

    val scope = rememberCoroutineScope()

    var biometricEnabled by remember { mutableStateOf(credentials.biometricEnabled) }
    val dynamicColor = context.readKey(StoreKey.DynamicColor)

    // Biometric checked event handler (enable/disable biometric authentication)
    fun showBiometricPrompt() {
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
            cipher = KeyStore.initCipherForEncryption(
                Biometric.PrivateKeyAlias,
                true
            ),
            onAuthenticationSucceeded = { cipher ->
                val encryptedData = KeyStore.encrypt(
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
            onAuthenticationFailed = {}
        )
    }

    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        item {
            SettingsGroup(R.string.Settings_Group_Appearance) {
                @Composable
                fun getThemeTranslation(theme: Int): String {
                    val themeRes = when (theme) {
                        ThemeValues.SYSTEM.ordinal -> R.string.Settings_SystemDefault
                        ThemeValues.LIGHT.ordinal -> R.string.Settings_Light
                        ThemeValues.DARK.ordinal -> R.string.Settings_Dark
                        // never happens
                        else -> throw UnsupportedOperationException()
                    }

                    return stringResource(themeRes)
                }

                val theme = context.readKey(StoreKey.Theme)
                val themeDialogState = rememberDialogState()

                SettingsProperty(
                    icon = Icons.Default.DarkMode,
                    text = R.string.Settings_Theme,
                    currentValue = getThemeTranslation(theme),
                    onClick = { themeDialogState.show() },
                )

                PickerDialog(
                    state = themeDialogState,
                    title = R.string.Settings_Theme,
                    items = listOf(0, 1, 2),
                    onSelected = {
                        context.writeKey(StoreKey.Theme, it)

                        // restart application to apply changes
                        ProcessPhoenix.triggerRebirth(context)
                    },
                ) {
                    Row(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .fillMaxWidth()
                    ) {
                        Icon(
                            when (it) {
                                ThemeValues.SYSTEM.ordinal -> Icons.Outlined.InvertColors
                                ThemeValues.LIGHT.ordinal -> Icons.Outlined.LightMode
                                ThemeValues.DARK.ordinal -> Icons.Outlined.DarkMode
                                // never happens
                                else -> throw UnsupportedOperationException()
                            },
                            contentDescription = null
                        )

                        Text(
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .fillMaxWidth(),
                            text = getThemeTranslation(it)
                        )
                    }
                }

                SettingsSwitcher(
                    icon = Icons.Default.ColorLens,
                    text = R.string.Settings_MaterialYou,
                    checked = dynamicColor,
                    onCheckedChange = {
                        context.writeKey(StoreKey.DynamicColor, it)

                        // restart application to apply changes
                        ProcessPhoenix.triggerRebirth(context)
                    }
                )
            }
        }

        item {
            SettingsGroup(R.string.Settings_Group_Security) {
                SettingsSwitcher(
                    icon = Icons.Default.Fingerprint,
                    text = R.string.Settings_BiometricUnlock,
                    checked = biometricEnabled,
                    onCheckedChange = { showBiometricPrompt() }
                )

                val timerDialogState = rememberDialogState()
                var vaultTimeout by remember {
                    mutableIntStateOf(context.readKey(StoreKey.VaultTimeout))
                }

                @Composable
                fun getVaultTimeoutTranslation(value: VaultTimeoutValues): String {
                    return when (value) {
                        VaultTimeoutValues.INSTANT -> stringResource(R.string.Settings_Vault_Timeout_Instant)
                        VaultTimeoutValues.ONE_MINUTE -> pluralStringResource(
                            R.plurals.Time_Minutes,
                            1,
                            1
                        )

                        VaultTimeoutValues.FIVE_MINUTES -> pluralStringResource(
                            R.plurals.Time_Minutes,
                            5,
                            5
                        )

                        VaultTimeoutValues.FIFTEEN_MINUTES -> pluralStringResource(
                            R.plurals.Time_Minutes,
                            15,
                            15
                        )

                        VaultTimeoutValues.THIRTY_MINUTES -> pluralStringResource(
                            R.plurals.Time_Minutes,
                            30,
                            30
                        )

                        VaultTimeoutValues.ONE_HOUR -> pluralStringResource(
                            R.plurals.Time_Hours,
                            1,
                            1
                        )

                        VaultTimeoutValues.NEVER -> stringResource(R.string.Settings_Vault_Timeout_Never)
                    }
                }

                SettingsProperty(
                    icon = Icons.Default.Timer,
                    text = R.string.Settings_Vault_Timeout_Modal_Title,
                    currentValue = getVaultTimeoutTranslation(
                        VaultTimeoutValues.fromSeconds(
                            vaultTimeout
                        )
                    ),
                    onClick = { timerDialogState.show() },
                )

                PickerDialog(
                    state = timerDialogState,
                    title = R.string.Settings_Vault_Timeout_Modal_Title,
                    items = VaultTimeoutValues.values().asList(),
                    onSelected = {
                        vaultTimeout = it.seconds
                        context.writeKey(StoreKey.VaultTimeout, it.seconds)
                    }
                ) {
                    Text(
                        text = getVaultTimeoutTranslation(it),
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}
