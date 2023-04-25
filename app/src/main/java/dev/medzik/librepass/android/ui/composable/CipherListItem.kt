package dev.medzik.librepass.android.ui.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.medzik.librepass.types.api.Cipher
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CipherListItem(
    cipher: Cipher,
    sheetState: SheetState,
    sheetContent: MutableState<@Composable () -> Unit>,
    onItemClick: (Cipher) -> Unit,
    onItemDelete: (Cipher) -> Unit
) {
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(cipher) }
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
            onClick = {
                scope.launch {
                    sheetContent.value = {
                        CipherListItemSheetContent(cipher, sheetState, onItemClick, onItemDelete)
                    }

                    sheetState.show()
                }
            }
        ) {
            Icon(Icons.Default.MoreHoriz, contentDescription = null)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CipherListItemSheetContent(cipher: Cipher, sheetState: SheetState, onItemClick: (Cipher) -> Unit, onItemDelete: (Cipher) -> Unit) {
    val scope = rememberCoroutineScope()

    Column {
        TextButton(
            onClick = {
                scope.launch { sheetState.hide() }
                onItemClick(cipher)
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
                scope.launch { sheetState.hide() }
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
