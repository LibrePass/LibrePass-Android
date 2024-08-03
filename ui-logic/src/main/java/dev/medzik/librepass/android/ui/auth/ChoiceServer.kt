package dev.medzik.librepass.android.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.medzik.android.compose.rememberMutable
import dev.medzik.android.compose.theme.spacing
import dev.medzik.android.compose.ui.IconBox
import dev.medzik.android.compose.ui.LoadingButton
import dev.medzik.android.compose.ui.bottomsheet.BaseBottomSheet
import dev.medzik.android.compose.ui.bottomsheet.BottomSheetState
import dev.medzik.android.compose.ui.bottomsheet.PickerBottomSheet
import dev.medzik.android.compose.ui.bottomsheet.rememberBottomSheetState
import dev.medzik.android.compose.ui.textfield.AnimatedTextField
import dev.medzik.android.compose.ui.textfield.TextFieldValue
import dev.medzik.android.utils.showToast
import dev.medzik.librepass.android.database.tables.CustomServer
import dev.medzik.librepass.android.ui.R
import dev.medzik.librepass.client.Server
import dev.medzik.librepass.client.api.checkApiConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoiceServer(
    server: MutableState<String>,
    viewModel: ChoiceServerViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    val choiceServerSheet = rememberBottomSheetState()
    val addServerSheet = rememberBottomSheetState()

    Button(
        onClick = { choiceServerSheet.show() },
        colors = ButtonDefaults.buttonColors().copy(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row {
            Text(
                text = stringResource(R.string.SelectServer),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    var servers by rememberMutable { emptyList<CustomServer>() }

    LaunchedEffect(Unit) {
        val customServers = viewModel.getCustomServers()

        servers = listOf(
            CustomServer(
                name = context.getString(R.string.Official),
                address = Server.PRODUCTION
            ),
            *customServers.toTypedArray(),
            CustomServer(
                context.getString(R.string.AddCustomServer),
                "custom_server"
            )
        )
    }

    PickerBottomSheet(
        state = choiceServerSheet,
        items = servers,
        onSelected = {
            if (it.address == "custom_server") {
                addServerSheet.show()
            } else {
                server.value = it.address
            }
        },
        onDismiss = {
            scope.launch { choiceServerSheet.hide() }
        }
    ) {
        val color = if (it.address == server.value) {
            MaterialTheme.colorScheme.primary
        } else Color.Unspecified

        Text(
            text = it.name,
            modifier = Modifier
                .padding(vertical = MaterialTheme.spacing.medium)
                .fillMaxWidth(),
            color = color,
            fontWeight = if (it.address == server.value) FontWeight.Bold else null
        )
    }

    BaseBottomSheet(
        state = addServerSheet,
        onDismiss = {
            scope.launch { addServerSheet.hide() }
        }
    ) {
        AddServerSheetContent(
            addServerSheet,
            server,
            viewModel
        )
    }
}

@Composable
private fun AddServerSheetContent(
    sheetState: BottomSheetState,
    server: MutableState<String>,
    viewModel: ChoiceServerViewModel
) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    var customServer by rememberMutable {
        CustomServer(
            name = "",
            address = "https://"
        )
    }

    Column(
        modifier = Modifier.padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AnimatedTextField(
            label = stringResource(R.string.Name),
            value = TextFieldValue(
                value = customServer.name,
                onChange = { customServer = customServer.copy(name = it) }
            ),
            clearButton = true,
            leading = {
                IconBox(Icons.Default.Draw)
            }
        )

        AnimatedTextField(
            label = stringResource(R.string.ServerAddress),
            value = TextFieldValue(
                value = customServer.address,
                onChange = { customServer = customServer.copy(address = it) }
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri
            ),
            clearButton = true,
            leading = {
                IconBox(Icons.Default.Dns)
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            var loading by rememberMutable { false }

            LoadingButton(
                loading = loading,
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        loading = true

                        if (!checkApiConnection(customServer.address)) {
                            context.showToast(R.string.NoServerConnection)
                            loading = false
                            return@launch
                        }

                        viewModel.insertCustomServer(customServer)

                        server.value = customServer.address

                        sheetState.hide()
                    }
                }
            ) {
                Text(stringResource(R.string.Add))
            }
        }
    }
}
