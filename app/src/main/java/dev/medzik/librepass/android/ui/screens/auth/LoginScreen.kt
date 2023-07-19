package dev.medzik.librepass.android.ui.screens.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import dev.medzik.android.composables.LoadingIndicator
import dev.medzik.android.composables.TextInputField
import dev.medzik.android.composables.TopBar
import dev.medzik.android.composables.TopBarBackIcon
import dev.medzik.android.composables.res.Text
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.data.Credentials
import dev.medzik.librepass.android.data.getRepository
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.ui.theme.LibrePassTheme
import dev.medzik.librepass.android.utils.DataStoreUserSecrets
import dev.medzik.librepass.android.utils.Navigation.navigate
import dev.medzik.librepass.android.utils.Remember.rememberLoadingState
import dev.medzik.librepass.android.utils.Remember.rememberStringData
import dev.medzik.librepass.android.utils.exception.handle
import dev.medzik.librepass.client.api.AuthClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    var loading by rememberLoadingState()
    var email by rememberStringData()
    var password by rememberStringData()

    val credentialsRepository = context.getRepository().credentials

    val authClient = AuthClient()

    fun submit(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty())
            return

        loading = true

        scope.launch(Dispatchers.IO) {
            try {
                val preLogin = authClient.preLogin(email)

                val credentials = authClient.login(
                    email = email,
                    password = password
                )

                // save credentials
                credentialsRepository.insert(
                    Credentials(
                        userId = credentials.userId,
                        email = email,
                        apiKey = credentials.apiKey,
                        publicKey = credentials.keyPair.publicKey,
                        // Argon2id parameters
                        memory = preLogin.memory,
                        iterations = preLogin.iterations,
                        parallelism = preLogin.parallelism
                    )
                )

                // save secrets in encrypted datastore
                DataStoreUserSecrets(
                    privateKey = credentials.keyPair.privateKey,
                    secretKey = credentials.secretKey
                ).save(context)

                // navigate to dashboard
                scope.launch(Dispatchers.Main) {
                    navController.navigate(
                        screen = Screen.Dashboard,
                        disableBack = true
                    )
                }
            } catch (e: Exception) {
                loading = false
                e.handle(context)
            }
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = R.string.TopBar_Login,
                navigationIcon = { TopBarBackIcon(navController) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            TextInputField(
                label = R.string.InputField_Email,
                value = email,
                onValueChange = { email = it },
                keyboardType = KeyboardType.Email
            )

            TextInputField(
                label = R.string.InputField_Password,
                value = password,
                onValueChange = { password = it },
                hidden = true,
                keyboardType = KeyboardType.Password
            )

            Button(
                onClick = { submit(email, password) },
                // disable button if email or password is empty or loading is in progress
                enabled = email.isNotEmpty() && password.isNotEmpty() && !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .padding(horizontal = 40.dp)
            ) {
                if (loading)
                    LoadingIndicator(animating = true)
                else
                    Text(R.string.Button_Login)
            }
        }
    }
}

@Preview
@Composable
fun LoginPreview() {
    LibrePassTheme {
        LoginScreen(NavHostController(LocalContext.current))
    }
}
