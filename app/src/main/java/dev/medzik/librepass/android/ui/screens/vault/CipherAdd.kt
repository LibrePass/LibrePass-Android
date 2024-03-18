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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.medzik.android.components.LoadingButton
import dev.medzik.android.components.getString
import dev.medzik.android.components.rememberMutable
import dev.medzik.android.components.rememberMutableBoolean
import dev.medzik.android.utils.runOnUiThread
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.ui.Argument
import dev.medzik.librepass.android.ui.LibrePassViewModel
import dev.medzik.librepass.android.ui.components.CipherEditFieldsCard
import dev.medzik.librepass.android.ui.components.CipherEditFieldsLogin
import dev.medzik.librepass.android.ui.components.CipherEditFieldsSecureNote
import dev.medzik.librepass.android.ui.components.TopBar
import dev.medzik.librepass.android.ui.components.TopBarBackIcon
import dev.medzik.librepass.android.utils.showErrorToast
import dev.medzik.librepass.types.cipher.Cipher
import dev.medzik.librepass.types.cipher.CipherType
import dev.medzik.librepass.types.cipher.data.CipherCardData
import dev.medzik.librepass.types.cipher.data.CipherLoginData
import dev.medzik.librepass.types.cipher.data.CipherSecureNoteData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun CipherAddScreen(
    navController: NavController,
    viewModel: LibrePassViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val cipherTypeArg = remember { navController.getString(Argument.CipherType) } ?: return
    val cipherType = remember { CipherType.from(cipherTypeArg.toInt()) }

    val credentials = remember { viewModel.credentialRepository.get() } ?: return

    var cipher by rememberMutable(
        Cipher(
            id = UUID.randomUUID(),
            owner = credentials.userId,
            type = cipherType,
            loginData = if (cipherType == CipherType.Login) CipherLoginData(name = "") else null,
            cardData = if (cipherType == CipherType.Card) CipherCardData(name = "", cardholderName = "", number = "") else null,
            secureNoteData = if (cipherType == CipherType.SecureNote) CipherSecureNoteData(title = "", note = "") else null
        )
    )

    var loading by rememberMutableBoolean()
    val scope = rememberCoroutineScope()

    fun submit() {
        loading = true

        scope.launch(Dispatchers.IO) {
            try {
                viewModel.vault.save(cipher)

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
                        Text(stringResource(R.string.Add))
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
