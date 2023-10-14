package dev.medzik.librepass.android.ui.screens.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import dev.medzik.android.components.getString
import dev.medzik.librepass.android.data.getRepository
import dev.medzik.librepass.android.ui.Argument
import dev.medzik.librepass.android.utils.SecretStore.getUserSecrets
import dev.medzik.librepass.types.cipher.Cipher
import java.util.UUID

@Composable
fun CipherEditScreen(navController: NavController) {
    val context = LocalContext.current

    val repository = context.getRepository()

    val cipherId = navController.getString(Argument.CipherId) ?: return
    val cipherTable = repository.cipher.get(UUID.fromString(cipherId)) ?: return
    val cipher = Cipher(cipherTable.encryptedCipher, context.getUserSecrets()!!.secretKey)

    CipherAddEditView(navController, cipher)
}
