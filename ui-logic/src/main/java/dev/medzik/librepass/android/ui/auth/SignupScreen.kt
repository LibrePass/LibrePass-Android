package dev.medzik.librepass.android.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation.Companion.keyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import dev.medzik.android.compose.icons.TopAppBarBackIcon
import dev.medzik.android.compose.theme.regularHorizontalPadding
import dev.medzik.android.compose.ui.IconBox
import dev.medzik.android.compose.ui.LoadingButton
import dev.medzik.android.compose.ui.textfield.AnimatedTextField
import dev.medzik.android.compose.ui.textfield.PasswordAnimatedTextField
import dev.medzik.android.compose.ui.textfield.TextFieldValue
import dev.medzik.librepass.android.ui.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object Signup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    navController: NavController,
    viewModel: SignupViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    TopAppBarBackIcon(navController)
                },
                title = {
                    Text(stringResource(R.string.Signup))
                }
            )
        }
    ) { innerPadding ->
        SignupScreenContent(navController, innerPadding, viewModel)
    }
}

@Composable
fun SignupScreenContent(
    navController: NavController,
    innerPadding: PaddingValues,
    viewModel: SignupViewModel
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        item {
            Column(
                modifier = Modifier.regularHorizontalPadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EmailTextField(
                    value = TextFieldValue.fromMutableState(
                        state = viewModel.email
                    )
                )

                PasswordAnimatedTextField(
                    label = stringResource(R.string.Password),
                    value = TextFieldValue.fromMutableState(
                        state = viewModel.password,
                        valueLabel = TextFieldValue.ValueLabel(
                            type = TextFieldValue.ValueLabel.Type.WARNING,
                            text = context.getString(R.string.PasswordLabel_WontBeRecover)
                        )
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    )
                )

                PasswordAnimatedTextField(
                    label = stringResource(R.string.RetypePassword),
                    value = TextFieldValue.fromMutableState(
                        state = viewModel.retypePassword,
                        error = if (!viewModel.retypePasswordIsValid) {
                            "Passwords mismatch"
                        } else null
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    )
                )

                AnimatedTextField(
                    label = stringResource(R.string.PasswordHint),
                    value = TextFieldValue.fromMutableState(
                        state = viewModel.passwordHint,
                        valueLabel = TextFieldValue.ValueLabel(
                            type = TextFieldValue.ValueLabel.Type.INFO,
                            text = context.getString(R.string.PasswordHintLabel)
                        )
                    ),
                    leading = { IconBox(Icons.Default.QuestionMark) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
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
                        .padding(vertical = 12.dp)
                        .height(50.dp)
                        .fillMaxWidth(0.85f),
                    onClick = { viewModel.register(context, navController) },
                    enabled = viewModel.canLogin
                ) {
                    Text(
                        text = stringResource(R.string.Signup),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun SignupPreview() {
    MaterialTheme {
        SignupScreen(
            navController = rememberNavController(),
            viewModel = SignupViewModel(LocalContext.current)
        )
    }
}
