package dev.medzik.librepass.android.ui.screens.vault

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.medzik.android.components.LoadingButton
import dev.medzik.android.components.getString
import dev.medzik.android.components.rememberMutable
import dev.medzik.android.components.rememberMutableBoolean
import dev.medzik.android.utils.runOnUiThread
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.data.CipherTable
import dev.medzik.librepass.android.data.getRepository
import dev.medzik.librepass.android.ui.Argument
import dev.medzik.librepass.android.ui.components.CipherEditFieldsCard
import dev.medzik.librepass.android.ui.components.CipherEditFieldsLogin
import dev.medzik.librepass.android.ui.components.CipherEditFieldsSecureNote
import dev.medzik.librepass.android.ui.components.TopBar
import dev.medzik.librepass.android.ui.components.TopBarBackIcon
import dev.medzik.librepass.android.utils.SecretStore.getUserSecrets
import dev.medzik.librepass.android.utils.showErrorToast
import dev.medzik.librepass.client.Server
import dev.medzik.librepass.client.api.CipherClient
import dev.medzik.librepass.types.cipher.Cipher
import dev.medzik.librepass.types.cipher.CipherType
import dev.medzik.librepass.types.cipher.EncryptedCipher
import dev.medzik.librepass.types.cipher.data.PasswordHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

@Composable
fun CipherEditScreen(navController: NavController) {
    val context = LocalContext.current

    val repository = context.getRepository()

    val cipherId = navController.getString(Argument.CipherId) ?: return
    val cipherTable = repository.cipher.get(UUID.fromString(cipherId)) ?: return
    val oldCipher = Cipher(cipherTable.encryptedCipher, context.getUserSecrets()!!.secretKey)
    var cipher by rememberMutable(oldCipher)

    var loading by rememberMutableBoolean()
    val scope = rememberCoroutineScope()

    val userSecrets = context.getUserSecrets()!!
    val credentials = repository.credentials.get()!!

    val cipherClient =
        CipherClient(
            apiKey = credentials.apiKey,
            apiUrl = credentials.apiUrl ?: Server.PRODUCTION
        )

    fun submit() {
        loading = true

        scope.launch(Dispatchers.IO) {
            if (cipher.type == CipherType.Login) {
                val basePassword = oldCipher.loginData!!.password
                val newPassword = cipher.loginData!!.password

                if (basePassword != null && basePassword != newPassword) {
                    val newList = mutableListOf<PasswordHistory>()
                    val oldList = oldCipher.loginData!!.passwordHistory
                    if (oldList != null) newList.addAll(oldList)

                    newList.add(PasswordHistory(basePassword, Date()))

                    cipher = cipher.copy(loginData = cipher.loginData!!.copy(passwordHistory = newList))
                }
            }

            val encryptedCipher = EncryptedCipher(cipher, userSecrets.secretKey)

            try {
                // update cipher in server
                cipherClient.update(encryptedCipher)

                // update cipher in local repository
                repository.cipher.update(CipherTable(encryptedCipher))

                runOnUiThread { navController.popBackStack() }
            } catch (e: Exception) {
                loading = false
                e.showErrorToast(context)
            }
        }
    }

    @Composable
    fun buttonEnabled(): Boolean {
        return when (cipher.type) {
            CipherType.Login -> {
                cipher.loginData!!.name.isNotEmpty()
            }
            CipherType.SecureNote -> {
                cipher.secureNoteData!!.title.isNotEmpty() &&
                    cipher.secureNoteData!!.note.isNotEmpty()
            }
            CipherType.Card -> {
                cipher.cardData!!.name.isNotEmpty() &&
                    cipher.cardData!!.cardholderName.isNotEmpty() &&
                    cipher.cardData!!.number.isNotEmpty()
            }
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.AddNewCipher),
                navigationIcon = { TopBarBackIcon(navController) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
        ) {
            @Composable
            fun button(): @Composable (cipher: Cipher) -> Unit {
                return {
                    cipher = it

                    LoadingButton(
                        loading = loading,
                        onClick = { submit() },
                        enabled = buttonEnabled(),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .padding(horizontal = 40.dp)
                    ) {
                        Text(stringResource(R.string.Save))
                    }
                }
            }

            when (cipher.type) {
                CipherType.Login -> {
                    CipherEditFieldsLogin(
                        navController,
                        cipher,
                        button()
                    )
                }
                CipherType.SecureNote -> {
                    CipherEditFieldsSecureNote(
                        cipher,
                        button()
                    )
                }
                CipherType.Card -> {
                    CipherEditFieldsCard(
                        cipher,
                        button()
                    )
                }
            }
        }
    }
}
