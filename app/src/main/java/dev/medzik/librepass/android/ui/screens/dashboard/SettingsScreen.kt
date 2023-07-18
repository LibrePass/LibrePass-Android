package dev.medzik.librepass.android.ui.screens.dashboard

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.InvertColors
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.jakewharton.processphoenix.ProcessPhoenix
import dev.medzik.android.composables.res.Text
import dev.medzik.android.cryptoutils.KeyStoreUtils
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.data.getRepository
import dev.medzik.librepass.android.ui.composables.Group
import dev.medzik.librepass.android.utils.Biometric
import dev.medzik.librepass.android.utils.DataStore.getUserSecrets
import dev.medzik.librepass.android.utils.DataStore.readKeyFromDataStore
import dev.medzik.librepass.android.utils.DataStore.writeKeyToDataStore
import dev.medzik.librepass.android.utils.DataStoreKey
import dev.medzik.librepass.android.utils.ThemeValues
import dev.medzik.librepass.android.utils.VaultTimeoutValues
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    // context must be FragmentActivity to show biometric prompt
    val context = LocalContext.current as FragmentActivity

    val userSecrets = context.getUserSecrets() ?: return

    val repository = context.getRepository()
    val credentials = repository.credentials.get()!!

    val scope = rememberCoroutineScope()

    var biometricEnabled by remember { mutableStateOf(credentials.biometricEnabled) }
    val dynamicColor = context.readKeyFromDataStore(DataStoreKey.DynamicColor)

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
        @StringRes text: Int,
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

    var showThemeSwitcherDialog by remember { mutableStateOf(false) }

    @Composable
    fun ThemeSwitcherDialog() {
        AlertDialog(
            onDismissRequest = { showThemeSwitcherDialog = false }
        ) {
            Surface(
                shape = AlertDialogDefaults.shape,
                tonalElevation = AlertDialogDefaults.TonalElevation,
            ) {
                Box(
                    modifier = Modifier.padding(all = 24.dp)
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.Settings_Theme),
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        for (theme in listOf(0, 1, 2)) {
                            Box(
                                modifier = Modifier.clickable {
                                    context.writeKeyToDataStore(DataStoreKey.Theme, theme)

                                    // restart application to apply changes
                                    ProcessPhoenix.triggerRebirth(context)
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(vertical = 12.dp)
                                        .fillMaxWidth(),
                                ) {
                                    Icon(
                                        when (theme) {
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
                                        text = when (theme) {
                                            0 -> R.string.Settings_SystemDefault
                                            1 -> R.string.Settings_Light
                                            2 -> R.string.Settings_Dark
                                            // never happens
                                            else -> throw UnsupportedOperationException()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun getTranslatedTimeoutValue(value: VaultTimeoutValues): String {
        return when (value) {
            VaultTimeoutValues.INSTANT -> stringResource(R.string.Settings_Vault_Timeout_Instant)
            VaultTimeoutValues.ONE_MINUTE -> pluralStringResource(R.plurals.Time_Minutes, 1, 1)
            VaultTimeoutValues.FIVE_MINUTES -> pluralStringResource(R.plurals.Time_Minutes, 5, 5)
            VaultTimeoutValues.FIFTEEN_MINUTES -> pluralStringResource(R.plurals.Time_Minutes, 15, 15)
            VaultTimeoutValues.THIRTY_MINUTES -> pluralStringResource(R.plurals.Time_Minutes, 30, 30)
            VaultTimeoutValues.ONE_HOUR -> pluralStringResource(R.plurals.Time_Hours, 1, 1)
            VaultTimeoutValues.NEVER -> stringResource(R.string.Settings_Vault_Timeout_Never)
        }
    }

    var showVaultTimeoutDialog by remember { mutableStateOf(false) }
    var vaultTimeout by remember { mutableIntStateOf(context.readKeyFromDataStore(DataStoreKey.VaultTimeout)) }

    @Composable
    fun VaultTimeoutDialog() {
        AlertDialog(
            onDismissRequest = { showVaultTimeoutDialog = false }
        ) {
            Surface(
                shape = AlertDialogDefaults.shape,
                tonalElevation = AlertDialogDefaults.TonalElevation,
            ) {
                Box(
                    modifier = Modifier.padding(all = 24.dp)
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.Settings_Vault_Timeout_Modal_Title),
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        for (value in VaultTimeoutValues.values()) {
                            Box(
                                modifier = Modifier.clickable {
                                    vaultTimeout = value.seconds
                                    context.writeKeyToDataStore(DataStoreKey.VaultTimeout, value.seconds)
                                    showVaultTimeoutDialog = false
                                }
                            ) {
                                Text(
                                    text = getTranslatedTimeoutValue(value),
                                    modifier = Modifier
                                        .padding(vertical = 12.dp)
                                        .fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        item {
            Group(R.string.Settings_Group_Appearance) {
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

                        val theme = context.readKeyFromDataStore(DataStoreKey.Theme)

                        Text(
                            text = R.string.Settings_Theme,
                            modifier = Modifier.weight(1f)
                        )

                        TextButton(
                            onClick = { showThemeSwitcherDialog = true },
                            modifier = Modifier.padding(start = 16.dp)
                        ) {
                            Text(
                                when (theme) {
                                    ThemeValues.SYSTEM.ordinal -> R.string.Settings_SystemDefault
                                    ThemeValues.LIGHT.ordinal -> R.string.Settings_Light
                                    ThemeValues.DARK.ordinal -> R.string.Settings_Dark
                                    // never happens
                                    else -> throw UnsupportedOperationException()
                                }
                            )
                        }

                        if (showThemeSwitcherDialog) {
                            ThemeSwitcherDialog()
                        }
                    }
                }

                SettingsSwitcher(
                    icon = Icons.Default.ColorLens,
                    text = R.string.Settings_MaterialYou,
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
            Group(R.string.Settings_Group_Security) {
                SettingsSwitcher(
                    icon = Icons.Default.Fingerprint,
                    text = R.string.Settings_BiometricUnlock,
                    checked = biometricEnabled,
                    onCheckedChange = { showBiometricPrompt() }
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { showVaultTimeoutDialog = true }
                        .padding(vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 16.dp)
                    )

                    Text(
                        text = R.string.Settings_Vault_Timeout_Modal_Title,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        modifier = Modifier.padding(start = 16.dp),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                        text = getTranslatedTimeoutValue(VaultTimeoutValues.fromSeconds(vaultTimeout))
                    )

                    if (showVaultTimeoutDialog) {
                        VaultTimeoutDialog()
                    }
                }
            }
        }
    }
}
