package dev.medzik.librepass.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pullrefresh.PullRefreshIndicator
import androidx.compose.material3.pullrefresh.pullRefresh
import androidx.compose.material3.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.data.CipherTable
import dev.medzik.librepass.android.data.Repository
import dev.medzik.librepass.android.ui.Argument
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.ui.composable.CipherListItem
import dev.medzik.librepass.android.ui.composable.common.LoadingIndicator
import dev.medzik.librepass.android.ui.composable.common.TopBar
import dev.medzik.librepass.android.ui.theme.LibrePassTheme
import dev.medzik.librepass.client.api.v1.CipherClient
import dev.medzik.librepass.types.api.Cipher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    val encryptionKey = navController.currentBackStackEntry?.arguments?.getString(Argument.EncryptionKey.get)
        ?: return

    // database
    val repository = Repository(context = LocalContext.current)
    val credentials = repository.credentials.get()!!

    // cipher API client
    val cipherClient = CipherClient(credentials.accessToken)

    // coroutines scope
    val scope = rememberCoroutineScope()

    // remember mutable state
    val ciphers = remember { mutableStateOf(listOf<Cipher>()) }
    val loading = remember { mutableStateOf(true) }
    val refreshing = remember { mutableStateOf(false) }

    /**
     * Update ciphers from API and database and update UI
     * @param state MutableState<Boolean> to update loading state
     */
    fun updateCiphers(state: MutableState<Boolean>) = scope.launch(Dispatchers.IO) {
        // set loading state to true
        state.value = true

        // TODO: add caching
        val cipherIds = cipherClient.getAll()

        // get ciphers from API and insert them into local database
        val tasks = cipherIds.map { id ->
            scope.launch(Dispatchers.IO) {
                val cipher = cipherClient.get(id)
                repository.cipher.insert(CipherTable(id = cipher.id, owner = cipher.owner, encryptedCipher = cipher))
            }
        }

        // wait for all tasks to finish
        runBlocking {
            tasks.joinAll()
        }

        // get ciphers from local database
        val dbCiphers = repository.cipher.getAll(credentials.userId)

        // decrypt ciphers
        val decryptedCiphers = dbCiphers.map { it.encryptedCipher.toCipher(encryptionKey) }

        // sort ciphers by name and update UI
        ciphers.value = decryptedCiphers.sortedBy { it.data.name }

        // set loading state to false
        state.value = false
    }

    // get ciphers on first load
    LaunchedEffect(scope) {
        // TODO: do not update ciphers if go back to dashboard
        updateCiphers(loading)
    }

    // refresh ciphers on pull to refresh
    fun refresh() = updateCiphers(refreshing)
    val state = rememberPullRefreshState(refreshing.value, ::refresh)

    val sheetState = rememberModalBottomSheetState()
    val sheetContent = remember { mutableStateOf<@Composable () -> Unit>({}) }

    Scaffold(
        topBar = {
            TopBar(title = stringResource(id = R.string.dashboard))
        },
        modifier = Modifier.navigationBarsPadding(),
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(Screen.CipherAdd.fill(Argument.EncryptionKey to encryptionKey))
            }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (loading.value) {
                Column(
                    // center
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    LoadingIndicator(animating = true)
                }
            }

            Box(
                modifier = Modifier.pullRefresh(state)
            ) {
                LazyColumn {
                    items(ciphers.value.size) { index ->
                        CipherListItem(
                            ciphers.value[index],
                            sheetState = sheetState,
                            sheetContent = sheetContent,
                            onItemClick = { cipher ->
                                navController.navigate(
                                    Screen.CipherView.fill(
                                        Argument.CipherId to cipher.id.toString(),
                                        Argument.EncryptionKey to encryptionKey
                                    )) {
                                    // TODO: restore state of dashboard screen after navigating back
                                    popUpTo(Screen.Dashboard.get) { saveState = true }
                                }
                            },
                            onItemDelete = { cipher ->
                                scope.launch(Dispatchers.IO) {
                                    cipherClient.delete(cipher.id)
                                    repository.cipher.delete(cipher.id)

                                    ciphers.value = ciphers.value.filter { it.id != cipher.id }
                                }
                            }
                        )
                    }
                }

                PullRefreshIndicator(
                    refreshing = refreshing.value,
                    state = state,
                    modifier = Modifier.align(Alignment.TopCenter),
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (sheetState.isVisible) {
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = {
                    scope.launch {
                        sheetState.hide()
                    }
                },
            ) {
                sheetContent.value()
            }
        }
    }
}

@Preview
@Composable
fun DashboardScreenPreview() {
    LibrePassTheme {
        DashboardScreen(NavHostController(LocalContext.current))
    }
}
