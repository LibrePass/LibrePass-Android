package dev.medzik.librepass.android.ui.screens.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.data.Credentials
import dev.medzik.librepass.android.data.Repository
import dev.medzik.librepass.android.ui.Argument
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.ui.composables.common.LoadingIndicator
import dev.medzik.librepass.android.ui.composables.common.TextInputField
import dev.medzik.librepass.android.ui.composables.common.TopBar
import dev.medzik.librepass.android.ui.composables.common.TopBarBackIcon
import dev.medzik.librepass.android.ui.theme.LibrePassTheme
import dev.medzik.librepass.android.utils.exception.handle
import dev.medzik.librepass.android.utils.navigation.navigate
import dev.medzik.librepass.client.api.v1.AuthClient
import dev.medzik.librepass.client.utils.Cryptography
import dev.medzik.librepass.client.utils.Cryptography.computeBasePasswordHash
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    // get composable context
    val context = LocalContext.current

    // repository
    val repository = Repository(context = context)

    // login data
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    // loading state
    var loading by remember { mutableStateOf(false) }

    // coroutine scope
    val scope = rememberCoroutineScope()
    // snackbar state
    val snackbarHostState = remember { SnackbarHostState() }

    // API client
    val authClient = AuthClient()

    /**
     * Login user with given credentials and navigate to dashboard.
     */
    fun onLogin(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            return
        }

        // set loading state
        loading = true

        scope.launch(Dispatchers.IO) {
            try {
                // get argon2id parameters
                val argon2idParameters = authClient.getUserArgon2idParameters(email)

                // compute base password hash
                val basePasswordHash = computeBasePasswordHash(
                    password = password,
                    email = email,
                    parameters = argon2idParameters
                )

                // authenticate user and get credentials
                val credentials = authClient.login(
                    email = email,
                    password = password,
                    basePassword = basePasswordHash
                )

                // insert credentials into local database
                repository.credentials.insert(
                    Credentials(
                        userId = credentials.userId,
                        email = email,
                        apiKey = credentials.apiKey,
                        // Curve25519 key pair
                        publicKey = credentials.publicKey,
                        protectedPrivateKey = credentials.protectedPrivateKey,
                        // Argon2id parameters
                        memory = argon2idParameters.memory,
                        iterations = argon2idParameters.iterations,
                        parallelism = argon2idParameters.parallelism,
                        version = argon2idParameters.version
                    )
                )

                // decrypt private key
                val privateKey = credentials.decryptPrivateKey(basePasswordHash)

                // calculate secret key
                val secretKey = Cryptography.calculateSecretKey(
                    privateKey = privateKey,
                    publicKey = credentials.publicKey
                )

                // navigate to dashboard
                scope.launch(Dispatchers.Main) {
                    navController.navigate(
                        screen = Screen.Dashboard,
                        arguments = listOf(
                            Argument.SecretKey to secretKey,
                            Argument.PrivateKey to privateKey
                        ),
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
        modifier = Modifier.navigationBarsPadding(),
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
                onClick = { onLogin(email, password) },
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
