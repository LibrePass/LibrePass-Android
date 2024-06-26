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
import dev.medzik.android.compose.icons.TopAppBarBackIcon
import dev.medzik.android.compose.rememberMutable
import dev.medzik.android.compose.ui.LoadingButton
import dev.medzik.android.utils.runOnUiThread
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.common.LibrePassViewModel
import dev.medzik.librepass.android.ui.components.CipherEditFieldsCard
import dev.medzik.librepass.android.ui.components.CipherEditFieldsLogin
import dev.medzik.librepass.android.ui.components.CipherEditFieldsSecureNote
import dev.medzik.librepass.android.ui.components.TopBar
import dev.medzik.librepass.android.utils.showErrorToast
import dev.medzik.librepass.types.cipher.Cipher
import dev.medzik.librepass.types.cipher.CipherType
import dev.medzik.librepass.types.cipher.data.PasswordHistory
import dev.medzik.otp.OTPParameters
import dev.medzik.otp.TOTPGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.Date
import java.util.UUID

@Serializable
data class CipherEdit(val cipherId: String)

@Composable
fun CipherEditScreen(
    navController: NavController,
    args: CipherEdit,
    viewModel: LibrePassViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    val oldCipher = remember { viewModel.vault.find(UUID.fromString(args.cipherId)) } ?: return
    var cipher by rememberMutable(oldCipher)

    var loading by rememberMutable(false)

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
                cipher.loginData!!.name.isNotEmpty() &&
                    (
                        cipher.loginData!!.twoFactor.isNullOrBlank() ||
                            runCatching {
                                val params = OTPParameters.parseUrl(cipher.loginData?.twoFactor)
                                TOTPGenerator.now(params)
                            }.isSuccess
                    )
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
                navigationIcon = { TopAppBarBackIcon(navController) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
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
