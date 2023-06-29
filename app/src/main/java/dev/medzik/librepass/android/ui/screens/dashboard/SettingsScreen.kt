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
import androidx.compose.runtime.mutableIntStateOf
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
import dev.medzik.android.cryptoutils.KeyStoreUtils
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.data.Repository
import dev.medzik.librepass.android.data.Settings
import dev.medzik.librepass.android.ui.composables.Group
import dev.medzik.librepass.android.utils.KeyStoreAlias
import dev.medzik.librepass.android.utils.getPrivateKeyFromDataStore
import dev.medzik.librepass.android.utils.showBiometricPrompt
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    // context must be FragmentActivity to show biometric prompt
    val context = LocalContext.current as FragmentActivity

    val privateKey = context.getPrivateKeyFromDataStore()
        ?: return

    // get credentials and settings from database
    val repository = Repository(context = context)
    val credentials = repository.credentials.get()!!
    val settings = repository.settings.get() ?: Settings()

    // insert default settings if not exists
    if (repository.settings.get() == null) {
        repository.settings.insert(settings)
    }

    val scope = rememberCoroutineScope()

    // states
    var biometricEnabled by remember { mutableStateOf(credentials.biometricEnabled) }
    var theme by remember { mutableIntStateOf(settings.theme) }
    var dynamicColor by remember { mutableStateOf(settings.dynamicColor) }

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

        showBiometricPrompt(
            context = context,
            cipher = KeyStoreUtils.initCipherForEncryption(KeyStoreAlias.PRIVATE_KEY.name, true),
            onAuthenticationSucceeded = { cipher ->
                val encryptedData = KeyStoreUtils.encrypt(
                    cipher = cipher,
                    data = privateKey
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
        // close theme selector
        themeSelectorExpanded = false

        // save theme to database
        theme = id
        repository.settings.update(settings.copy(theme = id))

        // reload activity
        context.recreate()
    }

    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
        item {
            Group(
                name = stringResource(id = R.string.Settings_Group_Appearance)
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
                            text = stringResource(id = R.string.Settings_Theme),
                            modifier = Modifier.weight(1f)
                        )

                        TextButton(
                            onClick = { themeSelectorExpanded = true },
                            modifier = Modifier.padding(start = 16.dp)
                        ) {
                            Text(
                                when (theme) {
                                    0 -> stringResource(id = R.string.Settings_SystemDefault)
                                    1 -> stringResource(id = R.string.Settings_Light)
                                    2 -> stringResource(id = R.string.Settings_Dark)
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
                                text = { Text(stringResource(id = R.string.Settings_SystemDefault)) },
                                onClick = { changeTheme(0) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.InvertColors,
                                        contentDescription = null
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.Settings_Light)) },
                                onClick = { changeTheme(1) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.LightMode,
                                        contentDescription = null
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.Settings_Dark)) },
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
                    text = stringResource(id = R.string.Settings_MaterialYou),
                    checked = dynamicColor,
                    onCheckedChange = {
                        dynamicColor = it

                        repository.settings.update(
                            settings.copy(
                                dynamicColor = it
                            )
                        )

                        // restart activity to apply changes
                        context.recreate()
                    }
                )
            }
        }

        item {
            Group(
                name = stringResource(id = R.string.Settings_Group_Security)
            ) {
                SettingsSwitcher(
                    icon = Icons.Default.Fingerprint,
                    text = stringResource(id = R.string.Settings_BiometricUnlock),
                    checked = biometricEnabled,
                    onCheckedChange = { biometricChecked() }
                )
            }
        }
    }
}
