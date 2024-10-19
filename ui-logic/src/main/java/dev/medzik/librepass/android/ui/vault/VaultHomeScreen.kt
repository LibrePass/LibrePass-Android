package dev.medzik.librepass.android.ui.vault

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import dev.medzik.android.compose.theme.combineAlpha
import dev.medzik.android.compose.theme.spacing
import dev.medzik.android.compose.ui.IconBox
import dev.medzik.android.compose.ui.bottomsheet.rememberBottomSheetState
import dev.medzik.common.extensions.truncate
import dev.medzik.librepass.android.database.Credentials
import dev.medzik.librepass.android.ui.R
import dev.medzik.librepass.client.api.CipherClient
import dev.medzik.librepass.types.cipher.Cipher
import dev.medzik.librepass.types.cipher.CipherType
import dev.medzik.librepass.types.cipher.data.CipherLoginData
import java.util.UUID

data class VaultHome(
    val credentials: Credentials
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultHomeScreen(
    args: VaultHome,
    navController: NavController,
    viewModel: VaultHomeViewModel = hiltViewModel()
) {
    val sheetState = rememberBottomSheetState()

    val ciphers = listOf(
        Cipher(
            id = UUID.randomUUID(),
            owner = UUID.randomUUID(),
            type = CipherType.Login,
            loginData = CipherLoginData(
                name = "Test"
            )
        ),
        Cipher(
            id = UUID.randomUUID(),
            owner = UUID.randomUUID(),
            type = CipherType.Login,
            loginData = CipherLoginData(
                name = "Name",
                username = "Username"
            )
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.Vault))
                },
                actions = {
                    IconButton(
                        onClick = { /* TODO */ }
                    ) {
                        IconBox(Icons.Default.Search)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { sheetState.show() }
            ) {
                IconBox(Icons.Default.Add)
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(horizontal = MaterialTheme.spacing.horizontalPadding)
                .padding(innerPadding)
                .fillMaxSize(),
            // Apply padding to the LazyColumn to prevent the FAB from covering items
            contentPadding = PaddingValues(bottom = 56.dp)
        ) {
            ciphers.forEach { cipher ->
                item {
                    CipherCard(cipher)
                }
            }
        }
    }
}

@Composable
private fun CipherCard(cipher: Cipher) {
    Card(
        modifier = Modifier.padding(vertical = MaterialTheme.spacing.small)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
//                .combinedClickable(
//                    onClick = { onClick(cipher) },
//                    onLongClick = { showMoreOptions() }
//                )
                .heightIn(min = 64.dp)
                .padding(horizontal = 24.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CipherCardIcon(cipher)

            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                CipherCardText(cipher)
            }

//                            if (showCipherActions) {
//                                IconButton(onClick = { showMoreOptions() }) {
//                                    Icon(Icons.Default.MoreHoriz, contentDescription = null)
//                                }
//
//                                CipherActionsDialog(
//                                    state = dialogState,
//                                    onClick = { onClick(cipher) },
//                                    onEdit = { onEdit(cipher) },
//                                    onDelete = { onDelete(cipher) }
//                                )
//                            }
        }
    }
}

@Composable
private fun CipherCardIcon(cipher: Cipher) {
    when (cipher.type) {
        CipherType.Login -> {
            val domain = run {
                val uris = cipher.loginData?.uris
                if (!uris.isNullOrEmpty()) uris[0] else null
            }
            if (domain != null) {
                AsyncImage(
                    // TODO: custom api url
                    model = CipherClient.getFavicon(domain = domain),
                    contentDescription = null,
                    error = rememberVectorPainter(Icons.Default.Person),
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Image(
                    Icons.Default.Person,
                    contentDescription = null,
                )
            }
        }

        CipherType.SecureNote -> {
            Image(
                Icons.AutoMirrored.Filled.Notes,
                contentDescription = null,
            )
        }

        CipherType.Card -> {
            Image(
                Icons.Default.CreditCard,
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun CipherCardText(cipher: Cipher) {
    val title: String
    var subtitle: String? = null

    when (cipher.type) {
        CipherType.Login -> {
            title = cipher.loginData!!.name

            if (!cipher.loginData!!.username.isNullOrEmpty()) {
                subtitle = cipher.loginData!!.username
            } else if (!cipher.loginData!!.email.isNullOrEmpty()) {
                subtitle = cipher.loginData!!.email
            }
        }

        CipherType.SecureNote -> {
            title = cipher.secureNoteData!!.title
        }

        CipherType.Card -> {
            title = cipher.cardData!!.name
            subtitle = "•••• " + cipher.cardData!!.number.takeLast(4)
        }
    }

    Text(
        text = title.truncate(16),
        style = MaterialTheme.typography.titleMedium
    )

    if (subtitle != null) {
        Text(
            text = subtitle.truncate(25),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.combineAlpha(0.6f)
        )
    }
}

@Preview
@Composable
fun VaultHomeScreenPreview() {
    VaultHomeScreen(
        args = VaultHome(
            credentials = Credentials(
                userId = UUID.randomUUID(),
                email = "example@example.com",
                apiKey = "",
                publicKey = "",
                memory = 64 * 1024,
                iterations = 2,
                parallelism = 3
            )
        ),
        navController = rememberNavController(),
        viewModel = VaultHomeViewModel()
    )
}
