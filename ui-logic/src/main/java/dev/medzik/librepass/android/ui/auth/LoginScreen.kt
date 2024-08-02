package dev.medzik.librepass.android.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import dev.medzik.android.compose.icons.TopAppBarBackIcon
import dev.medzik.android.compose.theme.spacing
import dev.medzik.android.compose.ui.LoadingButton
import dev.medzik.android.compose.ui.textfield.TextFieldValue
import dev.medzik.librepass.android.database.RepositoryImpl
import dev.medzik.librepass.android.ui.R
import kotlinx.serialization.Serializable

@Serializable
object Login

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    TopAppBarBackIcon(navController)
                },
                title = {
                    Text(stringResource(R.string.Login))
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EmailTextField(
                        TextFieldValue.fromMutableState(
                            state = viewModel.email
                        )
                    )
                    PasswordTextField(
                        TextFieldValue.fromMutableState(
                            state = viewModel.password
                        )
                    )

                    ChoiceServer(viewModel.server)
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LoadingButton(
                        modifier = Modifier
                            .padding(horizontal = MaterialTheme.spacing.horizontalPadding)
                            .height(50.dp)
                            .fillMaxWidth(0.85f),
                        onClick = { viewModel.login() },
                        enabled = viewModel.email.value.isNotEmpty() && viewModel.email.value.isNotEmpty()
                    ) {
                        Text(
                            text = stringResource(R.string.Login),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    OutlinedButton(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .height(45.dp)
                            .fillMaxWidth(0.75f),
                        onClick = {
                            navController.navigate(
                                ForgotPassword(
                                    server = viewModel.server.value
                                )
                            )
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.ForgotPassword),
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    val context = LocalContext.current
    val repository = RepositoryImpl(LocalContext.current)

    MaterialTheme {
        LoginScreen(
            navController = rememberNavController(),
            viewModel = LoginViewModel(
                context = context,
                repository = repository
            )
        )
    }
}
