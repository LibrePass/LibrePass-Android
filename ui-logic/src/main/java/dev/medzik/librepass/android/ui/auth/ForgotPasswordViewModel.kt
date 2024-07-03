package dev.medzik.librepass.android.ui.auth

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dev.medzik.android.compose.ui.textfield.TextFieldValue
import dev.medzik.android.utils.showToast
import dev.medzik.librepass.android.ui.R
import dev.medzik.librepass.client.api.AuthClient

class ForgotPasswordViewModel : ViewModel() {
    var email = TextFieldValue.fromMutableState()
    private lateinit var authClient: AuthClient

    fun requestPasswordHint(context: Context) {
        if (email.value.isEmpty()) {
            context.showToast(context.getString(R.string.EnterEmail))
            return
        }

        try {
            authClient.requestPasswordHint(email.value)

            context.showToast(context.getString(R.string.PasswordHintWasSent))
        } catch (e: Exception) {
//            e.showErrorToast(context)
        }
    }

    fun setServer(server: String) {
        AuthClient(apiUrl = server)
    }
}
