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
import com.google.gson.JsonSyntaxException
import dev.medzik.android.components.rememberMutable
import dev.medzik.android.components.rememberMutableBoolean
import dev.medzik.android.components.ui.LoadingButton
import dev.medzik.android.utils.runOnIOThread
import dev.medzik.android.utils.runOnUiThread
import dev.medzik.android.utils.showToast
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.database.datastore.CustomServers
import dev.medzik.librepass.android.database.datastore.readCustomServers
import dev.medzik.librepass.android.database.datastore.writeCustomServers
import dev.medzik.librepass.android.ui.components.TextInputField
import dev.medzik.librepass.client.api.checkApiConnection
import kotlinx.serialization.Serializable

@Serializable
object AddCustomServer

@Composable
fun AddCustomServerScreen(navController: NavController) {
    val context = LocalContext.current

    var loading by rememberMutableBoolean()
    var server by rememberMutable(CustomServers("", ""))

    fun submit() {
        loading = true

        runOnIOThread {
            // TODO: Delete try when released new version of LibrePass client library
            try {
                if (!checkApiConnection(server.address)) {
                    context.showToast(R.string.Tost_NoServerConnection)

                    loading = false

                    return@runOnIOThread
                }
            } catch (e: JsonSyntaxException) {
                context.showToast(R.string.Tost_NoServerConnection)

                loading = false

                return@runOnIOThread
            }

            val servers = readCustomServers(context)
            writeCustomServers(context, servers.plus(server))

            runOnUiThread { navController.popBackStack() }
        }
    }

    TextInputField(
        label = stringResource(R.string.Name),
        value = server.name,
        onValueChange = { server = server.copy(name = it) }
    )

    TextInputField(
        label = stringResource(R.string.ServerAddress),
        value = server.address,
        onValueChange = { server = server.copy(address = it) }
    )

    LoadingButton(
        loading = loading,
        onClick = { submit() },
        enabled = server.name.isNotEmpty() && server.address.isNotEmpty(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 80.dp, vertical = 8.dp)
    ) {
        Text(stringResource(R.string.Add))
    }
}
