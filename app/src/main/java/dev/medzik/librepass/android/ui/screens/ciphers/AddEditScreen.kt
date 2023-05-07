package dev.medzik.librepass.android.ui.screens.ciphers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.data.CipherTable
import dev.medzik.librepass.android.data.Repository
import dev.medzik.librepass.android.ui.Argument
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.ui.composables.CipherGroup
import dev.medzik.librepass.android.ui.composables.common.LoadingIndicator
import dev.medzik.librepass.android.ui.composables.common.TextInputFieldBase
import dev.medzik.librepass.android.ui.composables.common.TopBar
import dev.medzik.librepass.android.ui.composables.common.TopBarBackIcon
import dev.medzik.librepass.android.utils.navController.getString
import dev.medzik.librepass.client.api.v1.CipherClient
import dev.medzik.librepass.client.errors.ApiException
import dev.medzik.librepass.client.errors.ClientException
import dev.medzik.librepass.types.api.Cipher
import dev.medzik.librepass.types.api.CipherData
import dev.medzik.librepass.types.api.CipherType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.UUID

@Composable
fun CipherAddEditView(
    navController: NavController,
    baseCipher: Cipher? = null
) {
    // get encryption key from navController
    val encryptionKey = navController.getString(Argument.EncryptionKey)
        ?: return

    // database
    val repository = Repository(context = LocalContext.current)
    val credentials = repository.credentials.get()!!

    val cipherClient = CipherClient(credentials.accessToken)

    var cipherData by remember { mutableStateOf(baseCipher?.data ?: CipherData(name = "")) }

    // loading indicator
    var loading by remember { mutableStateOf(false) }

    // coroutine scope
    val scope = rememberCoroutineScope()

    // observe username and password from navController
    // used to get password from password generator
    navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String>("password")?.observeForever {
        cipherData.password = it
    }

    /**
     * Insert or update cipher.
     */
    fun submit() {
        loading = true

        val cipher = baseCipher?.copy(data = cipherData)
            ?: Cipher(
                id = UUID.randomUUID(),
                owner = credentials.userId,
                type = CipherType.Login.type,
                data = cipherData
            )

        scope.launch(Dispatchers.IO) {
            val encryptedCipher = cipher.toEncryptedCipher(encryptionKey)

            try {
                if (baseCipher == null) {
                    cipherClient.insert(encryptedCipher)
                } else {
                    cipherClient.update(encryptedCipher)
                }
            } catch (e: ApiException) {
                // TODO: handle api error
                loading = false
            } catch (e: ClientException) {
                // TODO: handle client error (i.e. no internet connection)
                loading = false
            } finally {
                runBlocking {
                    val cipherTable = CipherTable(
                        id = encryptedCipher.id,
                        owner = encryptedCipher.owner,
                        encryptedCipher = encryptedCipher
                    )

                    if (baseCipher == null) {
                        repository.cipher.insert(cipherTable)
                    } else {
                        repository.cipher.update(cipherTable)
                    }
                }

                scope.launch(Dispatchers.Main) { navController.popBackStack() }

                loading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = baseCipher?.data?.name ?: stringResource(id = R.string.add_new_cipher),
                navigationIcon = { TopBarBackIcon(navController) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            TextInputFieldBase(
                label = stringResource(id = R.string.cipher_field_name),
                modifier = Modifier.fillMaxWidth(),
                value = cipherData.name,
                onValueChange = { cipherData = cipherData.copy(name = it) }
            )

            CipherGroup(stringResource(id = R.string.cipher_group_login)) {
                TextInputFieldBase(
                    label = stringResource(id = R.string.cipher_field_username),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    value = cipherData.username,
                    onValueChange = { cipherData = cipherData.copy(username = it) }
                )

                TextInputFieldBase(
                    label = stringResource(id = R.string.cipher_field_password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    value = cipherData.password,
                    onValueChange = { cipherData = cipherData.copy(password = it) },
                    hidden = true,
                    trailingIcon = {
                        // TODO: add password generator
                        IconButton(onClick = {
                            navController.navigate(Screen.PasswordGenerator.get)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(id = R.string.generate_password)
                            )
                        }
                    }
                )
            }

            CipherGroup(stringResource(id = R.string.cipher_group_website)) {
                TextInputFieldBase(
                    label = stringResource(id = R.string.cipher_field_url),
                    modifier = Modifier.fillMaxWidth(),
                    value = cipherData.uris?.firstOrNull(),
                    onValueChange = { cipherData = cipherData.copy(uris = listOf(it)) }
                )
            }

            CipherGroup(stringResource(id = R.string.cipher_group_other)) {
                TextInputFieldBase(
                    label = stringResource(id = R.string.cipher_field_notes),
                    modifier = Modifier.fillMaxWidth(),
                    value = cipherData.notes,
                    onValueChange = { cipherData = cipherData.copy(notes = it) }
                )
            }

            Button(
                onClick = { submit() },
                enabled = cipherData.name.isNotEmpty() && !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .padding(horizontal = 40.dp)
            ) {
                if (loading) {
                    LoadingIndicator(animating = true)
                } else {
                    Text(
                        text = stringResource(id = baseCipher?.let { R.string.btn_save } ?: R.string.btn_add)
                    )
                }
            }
        }
    }
}
