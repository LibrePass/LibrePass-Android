package dev.medzik.librepass.android.ui.screens.vault

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.pullrefresh.PullRefreshIndicator
import androidx.compose.material3.pullrefresh.pullRefresh
import androidx.compose.material3.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.medzik.android.components.navigate
import dev.medzik.android.components.rememberMutableBoolean
import dev.medzik.librepass.android.ui.Argument
import dev.medzik.librepass.android.ui.LibrePassViewModel
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.ui.components.CipherCard
import dev.medzik.librepass.android.utils.SecretStore.getUserSecrets
import dev.medzik.librepass.android.utils.showErrorToast
import dev.medzik.librepass.client.Server
import dev.medzik.librepass.client.api.CipherClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun VaultScreen(
    navController: NavController,
    viewModel: LibrePassViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val userSecrets = context.getUserSecrets() ?: return

    val scope = rememberCoroutineScope()

    // states
    var refreshing by rememberMutableBoolean()
    var ciphers by remember { mutableStateOf(viewModel.vault.sortAlphabetically()) }

    val credentials = viewModel.credentialRepository.get() ?: return

    val cipherClient =
        CipherClient(
            apiKey = credentials.apiKey,
            apiUrl = credentials.apiUrl ?: Server.PRODUCTION
        )

    fun updateCiphers() {
        scope.launch(Dispatchers.IO) {
            refreshing = true

            try {
                val localCiphers = viewModel.cipherRepository.getAll(credentials.userId)
                val lastSync = viewModel.credentialRepository.get()!!.lastSync

                // send non-synchronized ciphers
                localCiphers.filter { it.needUpload }.forEach {
                    cipherClient.save(it.encryptedCipher)
                    viewModel.vault.save(it.encryptedCipher, needUpload = false)
                }

                if (lastSync != null) {
                    // update last sync date
                    viewModel.credentialRepository.update(credentials.copy(lastSync = Date().time / 1000))

                    // get ciphers from API
                    val syncResponse = cipherClient.sync(Date(lastSync * 1000))

                    viewModel.vault.sync(syncResponse)
                } else {
                    // update last sync date
                    viewModel.credentialRepository.update(credentials.copy(lastSync = Date().time / 1000))

                    // get all ciphers from API
                    val ciphersResponse = cipherClient.getAll()

                    // insert ciphers into the local database
                    for (cipher in ciphersResponse) {
                        viewModel.vault.save(cipher)
                    }
                }
            } catch (e: Exception) {
                e.showErrorToast(context)
            }

            // sort ciphers and update UI
            ciphers = viewModel.vault.sortAlphabetically()

            refreshing = false
        }
    }

    LaunchedEffect(scope) {
        // get local stored ciphers
        val dbCiphers = viewModel.cipherRepository.getAll(credentials.userId)

        // decrypt database if needed
        if (viewModel.vault.ciphers.isEmpty()) {
            viewModel.vault.decryptDatabase(userSecrets.secretKey, dbCiphers)
        }

        // sort ciphers and update UI
        ciphers = viewModel.vault.sortAlphabetically()
    }

    val pullRefreshState = rememberPullRefreshState(refreshing, ::updateCiphers)

    Box {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
        ) {
            items(ciphers.size) { index ->
                CipherCard(
                    cipher = ciphers[index],
                    onClick = { cipher ->
                        navController.navigate(
                            screen = Screen.CipherView,
                            args = arrayOf(Argument.CipherId to cipher.id.toString())
                        )
                    },
                    onEdit = { cipher ->
                        navController.navigate(
                            screen = Screen.CipherEdit,
                            args = arrayOf(Argument.CipherId to cipher.id.toString())
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
