package dev.medzik.librepass.android.ui.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.medzik.librepass.types.api.Cipher
import dev.medzik.librepass.types.api.CipherData
import dev.medzik.librepass.types.api.CipherType
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CipherListItem(
    cipher: Cipher,
    openBottomSheet: (sheetContent: @Composable () -> Unit) -> Unit,
    closeBottomSheet: () -> Unit,
    onItemClick: (Cipher) -> Unit,
    onItemDelete: (Cipher) -> Unit
) {
    val scope = rememberCoroutineScope()

    fun showSheet() {
        scope.launch {
            openBottomSheet {
                CipherListItemSheetContent(
                    cipher = cipher,
                    onItemClick = onItemClick,
                    onItemDelete = onItemDelete,
                    closeBottomSheet = closeBottomSheet
                )
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onItemClick(cipher) },
                onLongClick = { showSheet() }
            )
            .padding(vertical = 16.dp, horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.AccountCircle, contentDescription = null)

        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .fillMaxSize()
                .weight(1f)
        ) {
            Text(
                text = cipher.data.name,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = cipher.data.username ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        IconButton(
            onClick = { showSheet() }
        ) {
            Icon(Icons.Default.MoreHoriz, contentDescription = null)
        }
    }
}

@Composable
fun CipherListItemSheetContent(
    cipher: Cipher,
    onItemClick: (Cipher) -> Unit,
    onItemDelete: (Cipher) -> Unit,
    closeBottomSheet: () -> Unit
) {
    Column {
        TextButton(
            onClick = {
                onItemClick(cipher)
                closeBottomSheet()
            },
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "View",
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
        }

        TextButton(
            onClick = {
                onItemDelete(cipher)
                closeBottomSheet()
            },
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Delete",
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun CipherListItemPreview() {
    LazyColumn {
        item {
            CipherListItem(
                cipher = Cipher(
                    id = UUID.randomUUID(),
                    owner = UUID.randomUUID(),
                    type = CipherType.Login.type,
                    data = CipherData(
                        name = "Name",
                        username = "Username"
                    )
                ),
                openBottomSheet = {},
                closeBottomSheet = {},
                onItemClick = {},
                onItemDelete = {}
            )
        }
    }
}
