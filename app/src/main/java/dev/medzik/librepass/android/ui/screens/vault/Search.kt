package dev.medzik.librepass.android.ui.screens.vault

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.medzik.android.compose.icons.TopAppBarBackIcon
import dev.medzik.android.compose.rememberMutable
import dev.medzik.librepass.android.common.LibrePassViewModel
import dev.medzik.librepass.android.ui.components.CipherCard
import dev.medzik.librepass.types.cipher.CipherType
import kotlinx.serialization.Serializable

@Serializable
object Search

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: LibrePassViewModel = hiltViewModel()
) {
    val ciphers = remember { viewModel.vault.getSortedCiphers() }

    var searchText by rememberMutable("")

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
                navigationIcon = { TopAppBarBackIcon(navController) }
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
                when (it.type) {
                    CipherType.Login -> {
                        it.loginData!!.name.lowercase().contains(searchText) ||
                                it.loginData!!.username?.lowercase()?.contains(searchText) ?: false
                    }

                    CipherType.SecureNote -> {
                        it.secureNoteData!!.title.lowercase().contains(searchText)
                    }

                    CipherType.Card -> {
                        it.cardData!!.cardholderName.lowercase().contains(searchText)
                    }
                }
            }

            for (cipher in filteredCiphers) {
                item {
                    CipherCard(
                        cipher = cipher,
                        showCipherActions = false,
                        onClick = {
                            navController.navigate(
                                CipherView(
                                    cipher.id.toString()
                                )
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
