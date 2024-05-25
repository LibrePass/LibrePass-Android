package dev.medzik.librepass.android.ui.components.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.medzik.android.components.PickerDialog
import dev.medzik.android.components.navigate
import dev.medzik.android.components.rememberDialogState
import dev.medzik.librepass.android.BuildConfig
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.utils.SecretStore.readKey
import dev.medzik.librepass.android.utils.StoreKey
import dev.medzik.librepass.client.Server

@Composable
fun ChoiceServer(navController: NavController, server: MutableState<String>) {
    val serverChoiceDialog = rememberDialogState()

    @Composable
    fun getServerName(server: String): String {
        return when (server) {
            Server.PRODUCTION -> {
                stringResource(R.string.Server_Official)
            }
            else -> server
        }
    }

    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .clickable { serverChoiceDialog.show() }
    ) {
        Text(
            text = stringResource(R.string.ServerAddress) + ": ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Text(
            text = getServerName(server.value),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
    
    val servers = listOf(Server.PRODUCTION)
        .plus(LocalContext.current.readKey(StoreKey.CustomServers))
        .plus("custom_server")

    PickerDialog(
        state = serverChoiceDialog,
        title = stringResource(R.string.ServerAddress),
        items = servers,
        onSelected = {
            if (it == "custom_server") {
                navController.navigate(Screen.AddCustomServer)
            } else {
                server.value = it
            }
        }
    ) {
        Text(
            text = when (it) {
                "custom_server" -> { stringResource(R.string.Server_Choice_Dialog_AddCustom) }
                else -> getServerName(it)
            },
            modifier = Modifier
                .padding(vertical = 12.dp)
                .fillMaxWidth()
        )
    }
}
