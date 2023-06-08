package dev.medzik.librepass.android.ui.screens.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pullrefresh.PullRefreshIndicator
import androidx.compose.material3.pullrefresh.pullRefresh
import androidx.compose.material3.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import dev.medzik.librepass.android.data.CipherTable
import dev.medzik.librepass.android.data.Repository
import dev.medzik.librepass.android.ui.Argument
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.ui.composables.CipherListItem
import dev.medzik.librepass.android.utils.exception.handle
import dev.medzik.librepass.android.utils.navigation.getString
import dev.medzik.librepass.android.utils.navigation.navigate
import dev.medzik.librepass.client.api.v1.CipherClient
import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.client.errors.ClientException
import dev.medzik.librepass.types.cipher.Cipher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun DashboardScreen(
    navController: NavController,
    openBottomSheet: (sheetContent: @Composable () -> Unit) -> Unit,
    closeBottomSheet: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    // get encryption key from navController
    val secretKey = navController.getString(Argument.SecretKey)
        ?: return

    // get composable context
    val context = LocalContext.current

    // database
    val repository = Repository(context = context)
    val credentials = repository.credentials.get()!!

    // coroutines scope
    val scope = rememberCoroutineScope()

    // remember state
    var ciphers by remember { mutableStateOf(listOf<Cipher>()) }
    var refreshing by remember { mutableStateOf(false) }

    /**
     * Get ciphers from local repository and update UI
     */
    fun updateLocalCiphers() {
        // get ciphers from local database
        val dbCiphers = repository.cipher.getAll(credentials.userId)

        // decrypt ciphers
        val decryptedCiphers = dbCiphers.map { Cipher(it.encryptedCipher, secretKey) }

        // sort ciphers by name and update UI
        ciphers = decryptedCiphers.sortedBy { it.loginData!!.name }
    }

    /**
     * Update ciphers from API and local database and update UI
     */
    @Throws(ClientException::class, ApiException::class)
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

                // delete ciphers from local database that are not in API response
                for (cipher in cachedCiphers) {
                    if (cipher !in syncResponse.ids) {
                        repository.cipher.delete(cipher)
                    }
                }

                // update ciphers in local database
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

                // insert ciphers into local database
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
            e.handle(context, snackbarHostState)
        } finally {
            // get cipher from local repository and update UI
            updateLocalCiphers()

            // set loading state to false
            refreshing = false
        }
    }

    // load ciphers from local database on start
    LaunchedEffect(scope) {
        // get ciphers from local database and update UI
        updateLocalCiphers()

        // update ciphers from API and update UI
        // and show loading indicator while updating
        // after local ciphers are loaded to prevent empty screen
        updateCiphers()
    }

    // close bottom sheet on navigation change
    // to prevent bugs/crashes
    DisposableEffect(Unit) {
        onDispose {
            scope.launch { closeBottomSheet() }
        }
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
                    CipherListItem(
                        cipher = ciphers[index],
                        openBottomSheet = openBottomSheet,
                        closeBottomSheet = closeBottomSheet,
                        itemClick = { cipher ->
                            navController.navigate(
                                screen = Screen.CipherView,
                                arguments = listOf(
                                    Argument.CipherId to cipher.id.toString(),
                                    Argument.SecretKey to secretKey
                                )
                            )
                        },
                        itemEdit = { cipher ->
                            navController.navigate(
                                screen = Screen.CipherEdit,
                                arguments = listOf(
                                    Argument.CipherId to cipher.id.toString(),
                                    Argument.SecretKey to secretKey
                                )
                            )
                        },
                        itemDelete = { cipher ->
                            scope.launch(Dispatchers.IO) {
                                try {
                                    CipherClient(credentials.apiKey).delete(cipher.id)
                                    repository.cipher.delete(cipher.id)

                                    ciphers = ciphers.filter { it.id != cipher.id }
                                } catch (e: Exception) {
                                    e.handle(context, snackbarHostState)
                                }
                            }
                        }
                    )
                }
            }

            // pull to refresh indicator must be aligned to top
            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}
