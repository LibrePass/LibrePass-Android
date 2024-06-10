package dev.medzik.librepass.android.ui.screens.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.medzik.android.components.rememberMutable
import dev.medzik.android.components.ui.IconBox
import dev.medzik.android.components.ui.PickerDialog
import dev.medzik.android.components.ui.preference.PropertyPreference
import dev.medzik.android.components.ui.preference.SwitcherPreference
import dev.medzik.android.components.ui.rememberDialogState
import dev.medzik.android.crypto.KeyStore
import dev.medzik.android.utils.runOnIOThread
import dev.medzik.librepass.android.MainActivity
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.common.LibrePassViewModel
import dev.medzik.librepass.android.database.datastore.VaultTimeoutValue
import dev.medzik.librepass.android.database.datastore.readVaultTimeout
import dev.medzik.librepass.android.database.datastore.writeVaultTimeout
import dev.medzik.librepass.android.utils.KeyAlias
import dev.medzik.librepass.android.utils.checkIfBiometricAvailable
import dev.medzik.librepass.android.utils.showBiometricPromptForSetup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object SettingsSecurity

@Composable
fun SettingsSecurityScreen(viewModel: LibrePassViewModel = hiltViewModel()) {
    val context = LocalContext.current

    val credentials = viewModel.credentialRepository.get() ?: return

    val scope = rememberCoroutineScope()
    var biometricEnabled by remember { mutableStateOf(credentials.biometricAesKey != null) }
    val timerDialogState = rememberDialogState()
    var vaultTimeout by rememberMutable(readVaultTimeout(context))

    // Biometric checked event handler (enable/disable biometric authentication)
    fun biometricHandler() {
        if (biometricEnabled) {
            biometricEnabled = false

            scope.launch(Dispatchers.IO) {
                viewModel.credentialRepository.update(
                    credentials.copy(
                        biometricReSetup = false,
                        biometricAesKey = null,
                        biometricAesKeyIV = null
                    )
                )
            }

            return
        }

        showBiometricPromptForSetup(
            context as MainActivity,
            cipher = KeyStore.initForEncryption(
                KeyAlias.BiometricAesKey,
                deviceAuthentication = false
            ),
            onAuthenticationSucceeded = { cipher ->
                val encryptedData = KeyStore.encrypt(
                    cipher = cipher,
                    clearBytes = viewModel.vault.aesKey
                )

                biometricEnabled = true

                scope.launch {
                    viewModel.credentialRepository.update(
                        credentials.copy(
                            biometricAesKey = encryptedData.cipherText,
                            biometricAesKeyIV = encryptedData.initializationVector
                        )
                    )
                }
            },
            onAuthenticationFailed = {}
        )
    }

    @Composable
    fun getVaultTimeoutTranslation(value: VaultTimeoutValue): String {
        return when (value) {
            VaultTimeoutValue.INSTANT -> stringResource(R.string.Timeout_Instant)

            VaultTimeoutValue.ONE_MINUTE -> pluralStringResource(R.plurals.minutes, 1, 1)

            VaultTimeoutValue.FIVE_MINUTES -> pluralStringResource(R.plurals.minutes, 5, 5)

            VaultTimeoutValue.FIFTEEN_MINUTES -> pluralStringResource(R.plurals.minutes, 15, 15)

            VaultTimeoutValue.THIRTY_MINUTES -> pluralStringResource(R.plurals.minutes, 30, 30)

            VaultTimeoutValue.ONE_HOUR -> pluralStringResource(R.plurals.hours, 1, 1)

            VaultTimeoutValue.NEVER -> stringResource(R.string.Timeout_Never)
        }
    }

    if (checkIfBiometricAvailable(context)) {
        SwitcherPreference(
            title = stringResource(R.string.UnlockWithBiometrics),
            leading = { IconBox(Icons.Default.Fingerprint) },
            checked = biometricEnabled,
            onCheckedChange = { biometricHandler() }
        )
    }

    PropertyPreference(
        title = stringResource(R.string.VaultTimeout),
        leading = { IconBox(Icons.Default.Timer) },
        currentValue = getVaultTimeoutTranslation(vaultTimeout.timeout),
        onClick = { timerDialogState.show() },
    )

    PickerDialog(
        state = timerDialogState,
        title = stringResource(R.string.VaultTimeout),
        items = VaultTimeoutValue.entries,
        onSelected = {
            vaultTimeout = vaultTimeout.copy(timeout = it)
            runOnIOThread { writeVaultTimeout(context, vaultTimeout) }
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
