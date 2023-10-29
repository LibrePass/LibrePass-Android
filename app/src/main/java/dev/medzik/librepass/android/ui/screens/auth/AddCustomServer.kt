package dev.medzik.librepass.android.ui.screens.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.medzik.android.components.LoadingButton
import dev.medzik.android.components.rememberMutableBoolean
import dev.medzik.android.components.rememberMutableString
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.ui.components.TextInputField
import dev.medzik.librepass.android.utils.SecretStore.readKey
import dev.medzik.librepass.android.utils.SecretStore.writeKey
import dev.medzik.librepass.android.utils.StoreKey

@Composable
fun AddCustomServerScreen(navController: NavController) {
    val context = LocalContext.current

    var loading by rememberMutableBoolean()
    var server by rememberMutableString()

    fun submit(server: String) {
        loading = true

        // TODO: ping server to check connection

        val servers = context.readKey(StoreKey.CustomServers)
        context.writeKey(StoreKey.CustomServers, servers.plus(server))
        navController.popBackStack()

        loading = false
    }

    TextInputField(
        label = stringResource(R.string.Server_Add_InputField_Server),
        value = server,
        onValueChange = { server = it }
    )

    LoadingButton(
        loading = loading,
        onClick = { submit(server) },
        enabled = server.isNotEmpty(),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 80.dp, vertical = 8.dp)
    ) {
        Text(stringResource(R.string.Button_Add))
    }
}
