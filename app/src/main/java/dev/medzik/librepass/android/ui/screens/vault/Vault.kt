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
import androidx.compose.material3.*
import androidx.compose.material3.pullrefresh.PullRefreshIndicator
import androidx.compose.material3.pullrefresh.pullRefresh
import androidx.compose.material3.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.medzik.android.components.rememberDialogState
import dev.medzik.android.components.rememberMutableBoolean
import dev.medzik.android.crypto.KeyStore
import dev.medzik.librepass.android.MainActivity
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.ui.LibrePassViewModel
import dev.medzik.librepass.android.ui.components.CipherCard
import dev.medzik.librepass.android.ui.components.CipherTypeDialog
import dev.medzik.librepass.android.ui.components.TopBar
import dev.medzik.librepass.android.ui.screens.settings.Settings
import dev.medzik.librepass.android.utils.*
import dev.medzik.librepass.client.Server
import dev.medzik.librepass.client.api.CipherClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.*
import java.util.concurrent.TimeUnit

@Serializable
object Vault

@Composable
fun VaultScreen(
    navController: NavController,
    viewModel: LibrePassViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    var refreshing by rememberMutableBoolean()
    var ciphers by remember { mutableStateOf(viewModel.vault.sortAlphabetically()) }

    val credentials = viewModel.credentialRepository.get() ?: return

    val cipherClient = CipherClient(
        apiKey = credentials.apiKey,
        apiUrl = credentials.apiUrl ?: Server.PRODUCTION
    )

    fun updateCiphers() {
        scope.launch(Dispatchers.IO) {
            refreshing = true

            try {
                val localCiphers = viewModel.cipherRepository.getAll(credentials.userId)
                val lastSync = viewModel.credentialRepository.get()!!.lastSync ?: 0
                val lastSyncDate = Date(TimeUnit.SECONDS.toMillis(lastSync))
                val newLastSync = TimeUnit.MILLISECONDS.toSeconds(Date().time)

                // filter ciphers that need upload
                val ciphersNeededUpload = localCiphers.filter { it.needUpload }.map { it.encryptedCipher }

                // TODO: delete ciphers using this method

                val syncResponse = cipherClient.sync(lastSyncDate, ciphersNeededUpload, emptyList())

                // if it is not a full sync (it isn't the first sync)
                if (lastSync != 0L) {
                    // synchronize the local database with the server database
                    viewModel.vault.sync(syncResponse)
                } else {
                    // save all ciphers
                    for (cipher in syncResponse.ciphers) {
                        viewModel.vault.save(cipher)
                    }
                }

                // update the last sync date
                viewModel.credentialRepository.update(credentials.copy(lastSync = newLastSync))
            } catch (e: Exception) {
                e.showErrorToast(context)
            }

            // sort ciphers and update UI
            ciphers = viewModel.vault.sortAlphabetically()

            refreshing = false
        }
    }

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
            viewModel.vault.decryptDatabase(dbCiphers)
        }

        // sort ciphers and update UI
        ciphers = viewModel.vault.sortAlphabetically()

        // sync remote ciphers
        if (context.haveNetworkConnection()) {
            updateCiphers()
        }
    }

    val pullRefreshState = rememberPullRefreshState(refreshing, ::updateCiphers)

    Box {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
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

            item {
                Spacer(
                    modifier = Modifier.size(72.dp)
                )
            }
        }

        PullRefreshIndicator(
            refreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun VaultScreenTopBar(navController: NavController) {
    TopBar(
        title = stringResource(R.string.Vault),
        actions = {
            val context = LocalContext.current
            var expanded by rememberMutableBoolean()
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
