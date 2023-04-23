package dev.medzik.librepass.android.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Password
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.medzik.librepass.android.ui.Argument
import dev.medzik.librepass.android.ui.composable.common.TextInputFieldBase
import dev.medzik.librepass.android.ui.composable.common.TopBar

@Composable
fun CipherAddView(
    navController: NavController
) {
    // get encryption key from navController
    val encryptionKey = navController.currentBackStackEntry?.arguments?.getString(Argument.EncryptionKey.get)
        ?: return

    val name = remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopBar(
                title = "Add Cipher",
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null // stringResource(id = R.string.back)
                        )
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            TextInputFieldBase(
                label = "Name",
                state = name,
            )

            Group("Login")

            TextInputFieldBase(
                label = "Username",
                state = remember { mutableStateOf("") },
            )

            TextInputFieldBase(
                label = "Password",
                state = remember { mutableStateOf("") },
                hidden = true,
                trailingIcon = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            imageVector = Icons.Default.Password,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun Group(name: String) {
    Text(
        text = name,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
    )
}
