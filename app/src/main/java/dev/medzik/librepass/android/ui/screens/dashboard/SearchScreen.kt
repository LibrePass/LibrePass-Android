package dev.medzik.librepass.android.ui.screens.dashboard

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.medzik.android.components.navigate
import dev.medzik.android.components.rememberMutableString
import dev.medzik.librepass.android.data.getRepository
import dev.medzik.librepass.android.ui.Argument
import dev.medzik.librepass.android.ui.Screen
import dev.medzik.librepass.android.ui.composables.CipherCard
import dev.medzik.librepass.android.utils.SecretStore.getUserSecrets
import dev.medzik.librepass.android.utils.TopBarBackIcon
import dev.medzik.librepass.types.cipher.Cipher
import dev.medzik.librepass.types.cipher.CipherType
import dev.medzik.librepass.types.cipher.data.CipherLoginData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController) {
    val context = LocalContext.current

    val userSecrets = context.getUserSecrets()
        ?: return

    // database repository
    val repository = context.getRepository()
    val credentials = repository.credentials.get()!!

    val localCiphers = repository.cipher.getAll(credentials.userId)
    // decrypt ciphers
    val decryptedCiphers = localCiphers.map {
        try {
            Cipher(it.encryptedCipher, userSecrets.secretKey)
        } catch (e: Exception) {
            Cipher(
                id = it.encryptedCipher.id,
                owner = it.encryptedCipher.owner,
                type = CipherType.Login,
                loginData = CipherLoginData(
                    name = "Encryption error"
                )
            )
        }
    }
    // sort ciphers by name
    val ciphers = decryptedCiphers.sortedBy { it.loginData!!.name }

    var searchText by rememberMutableString()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                    )
                },
                navigationIcon = { TopBarBackIcon(navController) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            val filteredCiphers = ciphers.filter {
                it.loginData!!.name.lowercase().contains(searchText) || it.loginData!!.username?.lowercase()
                    ?.contains(searchText) ?: false
            }

            for (cipher in filteredCiphers) {
                item {
                    CipherCard(
                        cipher = cipher,
                        showCipherActions = false,
                        onClick = {
                            navController.navigate(
                                screen = Screen.CipherView,
                                args = arrayOf(Argument.CipherId to cipher.id.toString())
                            )
                        },
                        onEdit = {},
                        onDelete = {}
                    )
                }
            }
        }
    }
}
