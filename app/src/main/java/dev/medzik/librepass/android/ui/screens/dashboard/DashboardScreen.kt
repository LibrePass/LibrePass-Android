package dev.medzik.librepass.android.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pullrefresh.PullRefreshIndicator
import androidx.compose.material3.pullrefresh.pullRefresh
import androidx.compose.material3.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.data.CipherTable
import dev.medzik.librepass.android.data.Repository
import dev.medzik.librepass.android.ui.Argument
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.ui.composables.CipherListItem
import dev.medzik.librepass.android.ui.composables.common.LoadingIndicator
import dev.medzik.librepass.android.ui.composables.common.TopBar
import dev.medzik.librepass.android.utils.navController.getString
import dev.medzik.librepass.client.api.v1.AuthClient
import dev.medzik.librepass.client.api.v1.CipherClient
import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.client.errors.ClientException
import dev.medzik.librepass.types.api.Cipher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    sheetState: SheetState,
    sheetContent: MutableState<@Composable () -> Unit>
) {
    // get encryption key from navController
    val encryptionKey = navController.getString(Argument.EncryptionKey)
        ?: return

    // get composable context
    val context = LocalContext.current

    // database
    val repository = Repository(context = context)
    var credentials = repository.credentials.get()!!

    // coroutines scope
    val scope = rememberCoroutineScope()

    // snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    // remember mutable state
    var ciphers by remember { mutableStateOf(listOf<Cipher>()) }
    val loading = remember { mutableStateOf(true) }
    val refreshing = remember { mutableStateOf(false) }

    /**
     * Get ciphers from local repository and update UI
     */
    fun updateLocalCiphers() {
        // get ciphers from local database
        val dbCiphers = repository.cipher.getAll(credentials.userId)

        // decrypt ciphers
        val decryptedCiphers = dbCiphers.map { it.encryptedCipher.toCipher(encryptionKey) }

        // sort ciphers by name and update UI
        ciphers = decryptedCiphers.sortedBy { it.data.name }
    }

    /**
     * Update ciphers from API and local database and update UI
     * @param state MutableState<Boolean> to update loading state
     */
    @Throws(ClientException::class, ApiException::class)
    fun updateCiphers(state: MutableState<Boolean>) = scope.launch(Dispatchers.IO) {
        // set loading state to true
        state.value = true

        if (credentials.requireRefresh) {
            try {
                // refresh access token
                val newCredentials = AuthClient().refresh(refreshToken = credentials.refreshToken)

                // save new credentials
                credentials = credentials.copy(
                    accessToken = newCredentials.accessToken,
                    refreshToken = newCredentials.refreshToken,
                    requireRefresh = false
                )
                repository.credentials.update(credentials)
            } catch (e: ClientException) {
                scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.network_error)) }

                // show cipher from local repository
                updateLocalCiphers()

                // set loading state to false
                state.value = false

                // do not continue
                return@launch
            } catch (e: ApiException) {
                // TODO: handle api error
                scope.launch { snackbarHostState.showSnackbar(e.toString()) }

                // show cipher from local repository
                updateLocalCiphers()

                // set loading state to false
                state.value = false

                // do not continue
                return@launch
            }
        }

        // caching
        val cachedCiphers = repository.cipher.getAllIDs(credentials.userId)
        val lastSync = repository.credentials.get()!!.lastSync

        if (lastSync != null) {
            // update last sync date
            repository.credentials.update(credentials.copy(lastSync = Date().time / 1000))

            // get ciphers from API
            val syncResponse = CipherClient(credentials.accessToken).sync(Date(lastSync * 1000))

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
            val ciphersResponse = CipherClient(credentials.accessToken).getAll()

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

        // get cipher from local repository and update UI
        updateLocalCiphers()

        // set loading state to false
        state.value = false
    }

    // get ciphers on first load
    LaunchedEffect(scope) {
        // TODO: do not update ciphers if go back to dashboard
        updateCiphers(loading)
    }

    // onUnload
    DisposableEffect(Unit) {
        onDispose {
            scope.launch { sheetState.hide() }
        }
    }

    // refresh ciphers on pull to refresh
    fun refresh() = updateCiphers(refreshing)
    val state = rememberPullRefreshState(refreshing.value, ::refresh)

    Scaffold(
        topBar = {
            TopBar(title = stringResource(id = R.string.dashboard))
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(
                        Screen.CipherAdd.fill(
                            Argument.EncryptionKey to encryptionKey
                        )
                    )
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
        // snackbar is already in bottom sheet
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // loading indicator if loading
            if (loading.value) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    LoadingIndicator(animating = true)
                }
            }

            Box(
                modifier = Modifier.pullRefresh(state)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(ciphers.size) { index ->
                        CipherListItem(
                            ciphers[index],
                            sheetState = sheetState,
                            sheetContent = sheetContent,
                            onItemClick = { cipher ->
                                scope.launch { sheetState.hide() }

                                navController.navigate(
                                    Screen.CipherView.fill(
                                        Argument.CipherId to cipher.id.toString(),
                                        Argument.EncryptionKey to encryptionKey
                                    )
                                ) {
                                    // TODO: restore state of dashboard screen after navigating back
                                    popUpTo(Screen.Dashboard.get) { saveState = true }
                                }
                            },
                            onItemDelete = { cipher ->
                                scope.launch(Dispatchers.IO) {
                                    CipherClient(credentials.accessToken).delete(cipher.id)
                                    repository.cipher.delete(cipher.id)

                                    ciphers = ciphers.filter { it.id != cipher.id }
                                }
                            }
                        )
                    }
                }

                // pull to refresh indicator must be aligned to top
                PullRefreshIndicator(
                    refreshing = refreshing.value,
                    state = state,
                    modifier = Modifier.align(Alignment.TopCenter),
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
