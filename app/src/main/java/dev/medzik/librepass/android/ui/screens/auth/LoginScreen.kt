package dev.medzik.librepass.android.ui.screens.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.data.Credentials
import dev.medzik.librepass.android.data.getRepository
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.ui.composables.common.LoadingIndicator
import dev.medzik.librepass.android.ui.composables.common.TextInputField
import dev.medzik.librepass.android.ui.composables.common.TopBar
import dev.medzik.librepass.android.ui.composables.common.TopBarBackIcon
import dev.medzik.librepass.android.ui.theme.LibrePassTheme
import dev.medzik.librepass.android.utils.UserDataStoreSecrets
import dev.medzik.librepass.android.utils.exception.handle
import dev.medzik.librepass.android.utils.navigation.navigate
import dev.medzik.librepass.android.utils.remember.rememberLoadingState
import dev.medzik.librepass.android.utils.remember.rememberSnackbarHostState
import dev.medzik.librepass.android.utils.remember.rememberStringData
import dev.medzik.librepass.android.utils.writeUserSecrets
import dev.medzik.librepass.client.api.v1.AuthClient
import dev.medzik.librepass.client.utils.Cryptography.computePasswordHash
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()
    val snackbarHostState = rememberSnackbarHostState()

    var loading by rememberLoadingState()
    var email by rememberStringData()
    var password by rememberStringData()

    val credentialsRepository = context.getRepository().credentials

    val authClient = AuthClient()

    // Login user with given credentials and navigate to dashboard.
    fun submit(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            return
        }

        // set loading state
        loading = true

        scope.launch(Dispatchers.IO) {
            try {
                val preLogin = authClient.preLogin(email)

                // compute base password hash
                val passwordHash = computePasswordHash(
                    password = password,
                    email = email,
                    argon2Function = preLogin.toArgon2()
                )

                // authenticate user and get credentials
                val credentials = authClient.login(
                    email = email,
                    passwordHash = passwordHash
                )

                // insert credentials into local database
                credentialsRepository.insert(
                    Credentials(
                        userId = credentials.userId,
                        email = email,
                        apiKey = credentials.apiKey,
                        publicKey = credentials.keyPair.publicKey,
                        // Argon2id parameters
                        memory = preLogin.memory,
                        iterations = preLogin.iterations,
                        parallelism = preLogin.parallelism,
                        version = preLogin.version
                    )
                )

                context.writeUserSecrets(
                    UserDataStoreSecrets(
                        privateKey = credentials.keyPair.privateKey,
                        secretKey = credentials.secretKey
                    )
                )

                // navigate to dashboard
                scope.launch(Dispatchers.Main) {
                    navController.navigate(
                        screen = Screen.Dashboard,
                        disableBack = true
                    )
                }
            } catch (e: Exception) {
                loading = false

                e.handle(context, snackbarHostState)
            }
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(id = R.string.TopBar_Login),
                navigationIcon = {
                    TopBarBackIcon(navController = navController)
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            TextInputField(
                label = stringResource(id = R.string.InputField_Email),
                value = email,
                onValueChange = { email = it },
                keyboardType = KeyboardType.Email
            )

            TextInputField(
                label = stringResource(id = R.string.InputField_Password),
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
                if (loading) {
                    LoadingIndicator(animating = true)
                } else {
                    Text(text = stringResource(id = R.string.Button_Login))
                }
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
