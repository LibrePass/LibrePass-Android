package dev.medzik.librepass.android.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import dev.medzik.android.compose.icons.TopAppBarBackIcon
import dev.medzik.android.compose.theme.infoContainer
import dev.medzik.android.compose.theme.spacing
import dev.medzik.android.compose.ui.LoadingButton
import dev.medzik.librepass.android.ui.R
import dev.medzik.librepass.client.Server
import kotlinx.serialization.Serializable

@Serializable
data class ForgotPassword(
    val server: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    args: ForgotPassword
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.ForgotPassword))
                },
                navigationIcon = {
                    TopAppBarBackIcon(navController)
                }
            )
        }
    ) { innerPadding ->
        ForgotPasswordScreenContent(args, innerPadding)
    }
}

@Composable
fun ForgotPasswordScreenContent(
    args: ForgotPassword,
    innerPadding: PaddingValues,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.setServer(args.server)
    }

    Column(
        modifier = Modifier
            .padding(innerPadding)
            .padding(horizontal = MaterialTheme.spacing.horizontalPadding)
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.infoContainer,
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    modifier = Modifier.padding(12.dp),
                    text = stringResource(R.string.ForgotPassword_Info)
                )
            }

            EmailTextField(viewModel.email)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            LoadingButton(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .height(50.dp)
                    .fillMaxWidth(0.85f),
                onClick = { viewModel.requestPasswordHint(context) }
            ) {
                Text(
                    text = stringResource(R.string.GetPasswordHint),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Preview
@Composable
fun ForgotPasswordScreenPreview() {
    MaterialTheme {
        ForgotPasswordScreen(
            navController = rememberNavController(),
            args = ForgotPassword(Server.PRODUCTION)
        )
    }
}
