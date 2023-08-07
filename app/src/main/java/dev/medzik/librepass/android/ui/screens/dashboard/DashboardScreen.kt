package dev.medzik.librepass.android.ui.screens.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
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
import androidx.navigation.NavController
import dev.medzik.libcrypto.EncryptException
import dev.medzik.librepass.android.data.CipherTable
import dev.medzik.librepass.android.data.getRepository
import dev.medzik.librepass.android.ui.Argument
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.ui.composables.CipherCard
import dev.medzik.librepass.android.utils.Navigation.navigate
import dev.medzik.librepass.android.utils.Remember.rememberLoadingState
import dev.medzik.librepass.android.utils.SecretStore.getUserSecrets
import dev.medzik.librepass.android.utils.exception.handle
import dev.medzik.librepass.client.api.CipherClient
import dev.medzik.librepass.types.cipher.Cipher
import dev.medzik.librepass.types.cipher.CipherType
import dev.medzik.librepass.types.cipher.data.CipherLoginData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current

    val userSecrets = context.getUserSecrets()
        ?: return

    val scope = rememberCoroutineScope()

    // states
    var refreshing by rememberLoadingState()
    var ciphers by remember { mutableStateOf(listOf<Cipher>()) }

    // database repository
    val repository = context.getRepository()
    val credentials = repository.credentials.get()!!

    // get ciphers from local repository and update UI
    fun updateLocalCiphers() {
        val dbCiphers = repository.cipher.getAll(credentials.userId)

        // decrypt ciphers
        val decryptedCiphers = dbCiphers.map {
            try {
                Cipher(it.encryptedCipher, userSecrets.secretKey)
            } catch (e: EncryptException) {
                Cipher(
                    id = it.encryptedCipher.id,
                    owner = it.encryptedCipher.owner,
                    type = CipherType.Login,
                    loginData = CipherLoginData(
                        name = "Encryption error"
                    )
                )
            }
        }

        // sort ciphers by name and update UI
        ciphers = decryptedCiphers.sortedBy { it.loginData!!.name }
    }

    // Update ciphers from API and local database and update UI
    fun updateCiphers() = scope.launch(Dispatchers.IO) {
        // set loading state to true
        refreshing = true

        try {
            // caching
            val cachedCiphers = repository.cipher.getAllIDs(credentials.userId)
            val lastSync = repository.credentials.get()!!.lastSync

            if (lastSync != null) {
                // update last sync date
                repository.credentials.update(credentials.copy(lastSync = Date().time / 1000))

                // get ciphers from API
                val syncResponse = CipherClient(credentials.apiKey).sync(Date(lastSync * 1000))

                // delete ciphers from the local database that are not in API response
                for (cipher in cachedCiphers) {
                    if (cipher !in syncResponse.ids) {
                        repository.cipher.delete(cipher)
                    }
                }

                // update ciphers in the local database
                for (cipher in syncResponse.ciphers) {
                    repository.cipher.insert(
                        CipherTable(
                            id = cipher.id,
                            owner = cipher.owner,
                            encryptedCipher = cipher
                        )
                    )
                }
            } else {
                // update last sync date
                repository.credentials.update(credentials.copy(lastSync = Date().time / 1000))

                // get all ciphers from API
                val ciphersResponse = CipherClient(credentials.apiKey).getAll()

                // insert ciphers into the local database
                for (cipher in ciphersResponse) {
                    repository.cipher.insert(
                        CipherTable(
                            id = cipher.id,
                            owner = cipher.owner,
                            encryptedCipher = cipher
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.handle(context)
        } finally {
            // get cipher from local repository and update UI
            updateLocalCiphers()

            // set loading state to false
            refreshing = false
        }
    }

    // load ciphers from cache on start
    LaunchedEffect(scope) {
        // get ciphers from the local database and update UI
        updateLocalCiphers()

        // update ciphers from API and update UI
        // and show loading indicator while updating
        // after local ciphers are loaded to prevent empty screen
        updateCiphers()
    }

    // refresh ciphers on pull to refresh
    val pullRefreshState = rememberPullRefreshState(refreshing, ::updateCiphers)

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.pullRefresh(pullRefreshState)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(ciphers.size) { index ->
                    CipherCard(
                        cipher = ciphers[index],
                        onClick = { cipher ->
                            navController.navigate(
                                screen = Screen.CipherView,
                                argument = Argument.CipherId to cipher.id.toString()
                            )
                        },
                        onEdit = { cipher ->
                            navController.navigate(
                                screen = Screen.CipherEdit,
                                argument = Argument.CipherId to cipher.id.toString()
                            )
                        },
                        onDelete = { cipher ->
                            scope.launch(Dispatchers.IO) {
                                try {
                                    CipherClient(credentials.apiKey).delete(cipher.id)
                                    repository.cipher.delete(cipher.id)

                                    ciphers = ciphers.filter { it.id != cipher.id }
                                } catch (e: Exception) {
                                    e.handle(context)
                                }
                            }
                        }
                    )
                }
            }

            // pull to refresh indicator must be aligned to the top
            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}
