package dev.medzik.librepass.android.ui.screens.vault

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.medzik.android.compose.rememberMutable
import dev.medzik.android.compose.ui.dialog.rememberDialogState
import dev.medzik.android.crypto.KeyStore
import dev.medzik.librepass.android.MainActivity
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.business.syncCiphers
import dev.medzik.librepass.android.common.LibrePassViewModel
import dev.medzik.librepass.android.common.haveNetworkConnection
import dev.medzik.librepass.android.ui.components.CipherCard
import dev.medzik.librepass.android.ui.components.CipherTypeDialog
import dev.medzik.librepass.android.ui.components.TopBar
import dev.medzik.librepass.android.ui.screens.settings.Settings
import dev.medzik.librepass.android.utils.KeyAlias
import dev.medzik.librepass.android.utils.checkIfBiometricAvailable
import dev.medzik.librepass.android.utils.showBiometricPromptForSetup
import dev.medzik.librepass.android.utils.showErrorToast
import dev.medzik.librepass.client.Server
import dev.medzik.librepass.client.api.CipherClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object Vault

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    navController: NavController,
    viewModel: LibrePassViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val pullToRefreshState = rememberPullToRefreshState()

    var ciphers by remember { mutableStateOf(viewModel.vault.getSortedCiphers()) }
    val credentials = remember { viewModel.credentialRepository.get() } ?: return

    val cipherClient = CipherClient(
        apiKey = credentials.apiKey,
        apiUrl = credentials.apiUrl ?: Server.PRODUCTION
    )

    fun reSetupBiometrics() {
        // enable biometric authentication if possible
        if (checkIfBiometricAvailable(context)) {
            showBiometricPromptForSetup(
                context as MainActivity,
                cipher = KeyStore.initForEncryption(
                    KeyAlias.BiometricAesKey,
                    deviceAuthentication = false
                ),
                onAuthenticationSucceeded = { cipher ->
                    val encryptedData = KeyStore.encrypt(cipher, viewModel.vault.aesKey)

                    scope.launch(Dispatchers.IO) {
                        viewModel.credentialRepository.update(
                            credentials.copy(
                                biometricReSetup = false,
                                biometricAesKey = encryptedData.cipherText,
                                biometricAesKeyIV = encryptedData.initializationVector
                            )
                        )
                    }
                },
                onAuthenticationFailed = {
                    scope.launch(Dispatchers.IO) {
                        viewModel.credentialRepository.update(
                            credentials.copy(
                                biometricReSetup = false
                            )
                        )
                    }
                }
            )
        } else {
            scope.launch(Dispatchers.IO) {
                viewModel.credentialRepository.update(
                    credentials.copy(
                        biometricReSetup = false
                    )
                )
            }
        }
    }

    LaunchedEffect(scope) {
        pullToRefreshState.startRefresh()

        if (credentials.biometricReSetup) {
            try {
                reSetupBiometrics()
            } catch (e: Exception) {
                e.showErrorToast(context)
            }
        }

        // get local stored ciphers
        val dbCiphers = viewModel.cipherRepository.getAll(credentials.userId)

        // decrypt database if needed
        if (viewModel.vault.ciphers.isEmpty()) {
            viewModel.vault.decrypt(dbCiphers)
        }

        // sort ciphers and update UI
        ciphers = viewModel.vault.getSortedCiphers()

        // sync remote ciphers
        if (context.haveNetworkConnection()) {
            scope.launch(Dispatchers.IO) {
                try {
                    syncCiphers(
                        context,
                        credentials,
                        client = cipherClient,
                        vault = viewModel.vault
                    )

                    // sort ciphers and update UI
                    ciphers = viewModel.vault.getSortedCiphers()
                } catch (e: Exception) {
                    e.showErrorToast(context)
                } finally {
                    pullToRefreshState.endRefresh()
                }
            }
        } else {
            pullToRefreshState.endRefresh()
        }
    }

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(Unit) {
            scope.launch(Dispatchers.IO) {
                try {
                    syncCiphers(
                        context,
                        credentials,
                        client = cipherClient,
                        vault = viewModel.vault
                    )
                } catch (e: Exception) {
                    e.showErrorToast(context)
                } finally {
                    pullToRefreshState.endRefresh()
                }
            }
        }
    }

    Box {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(connection = pullToRefreshState.nestedScrollConnection)
        ) {
            items(ciphers.size) { index ->
                CipherCard(
                    cipher = ciphers[index],
                    onClick = { cipher ->
                        navController.navigate(
                            CipherView(
                                cipher.id.toString()
                            )
                        )
                    },
                    onEdit = { cipher ->
                        navController.navigate(
                            CipherEdit(
                                cipher.id.toString()
                            )
                        )
                    },
                    onDelete = { cipher ->
                        scope.launch(Dispatchers.IO) {
                            try {
                                cipherClient.delete(cipher.id)
                                viewModel.cipherRepository.delete(cipher.id)

                                ciphers = ciphers.filter { it.id != cipher.id }
                            } catch (e: Exception) {
                                e.showErrorToast(context)
                            }
                        }
                    }
                )
            }

            // Prevent covering ciphers with floating action button
            item {
                Spacer(
                    modifier = Modifier.size(72.dp)
                )
            }
        }

        PullToRefreshContainer(
            modifier = Modifier.align(alignment = Alignment.TopCenter),
            state = pullToRefreshState,
        )
    }
}

@Composable
fun VaultScreenTopBar(navController: NavController) {
    TopBar(
        title = stringResource(R.string.Vault),
        actions = {
            val context = LocalContext.current
            var expanded by rememberMutable(false)
            IconButton(onClick = { navController.navigate(Search) }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            }

            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.Settings)) },
                    onClick = {
                        expanded = false
                        navController.navigate(Settings)
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.LockVault)) },
                    onClick = {
                        (context as MainActivity).vault.deleteSecrets(context)

                        // close application
                        (context as Activity).finish()
                    }
                )
            }
        }
    )
}

@Composable
fun VaultScreenFloatingActionButton(navController: NavController) {
    val dialogState = rememberDialogState()

    FloatingActionButton(
        onClick = { dialogState.show() }
    ) {
        Icon(Icons.Default.Add, contentDescription = null)
    }

    CipherTypeDialog(
        dialogState,
        onSelected = { cipherType ->
            navController.navigate(
                CipherAdd(
                    cipherType
                )
            )
        }
    )
}
