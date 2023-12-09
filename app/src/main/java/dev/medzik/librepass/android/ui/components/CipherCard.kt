package dev.medzik.librepass.android.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.medzik.android.components.BottomSheetState
import dev.medzik.android.components.PickerBottomSheet
import dev.medzik.android.components.rememberBottomSheetState
import dev.medzik.librepass.android.R
import dev.medzik.librepass.android.utils.SHORTEN_NAME_LENGTH
import dev.medzik.librepass.android.utils.SHORTEN_USERNAME_LENGTH
import dev.medzik.librepass.android.utils.shorten
import dev.medzik.librepass.client.api.CipherClient
import dev.medzik.librepass.types.cipher.Cipher
import dev.medzik.librepass.types.cipher.CipherType

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CipherCard(
    cipher: Cipher,
    onClick: (Cipher) -> Unit,
    onEdit: (Cipher) -> Unit,
    onDelete: (Cipher) -> Unit,
    showCipherActions: Boolean = true
) {
    val sheetState = rememberBottomSheetState()

    fun showMoreOptions() = sheetState.show()

    fun getDomain(): String? {
        val uris = cipher.loginData?.uris
        return if (!uris.isNullOrEmpty()) uris[0] else null
    }

    @Composable
    fun CipherIcon() {
        when (cipher.type) {
            CipherType.Login -> {
                val domain = getDomain()
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
                    Icons.Default.Notes,
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
    fun CipherText() {
        val title: String
        var subtitle: String? = null

        when (cipher.type) {
            CipherType.Login -> {
                title = cipher.loginData!!.name
                subtitle = cipher.loginData!!.username
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
            text = title.shorten(SHORTEN_NAME_LENGTH),
            style = MaterialTheme.typography.titleMedium
        )

        if (subtitle != null) {
            Text(
                text = subtitle.shorten(SHORTEN_USERNAME_LENGTH),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }

    Card(
        modifier =
            Modifier
                .padding(vertical = 8.dp)
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { onClick(cipher) },
                        onLongClick = { showMoreOptions() }
                    )
                    .heightIn(min = 64.dp)
                    .padding(horizontal = 24.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CipherIcon()

            Column(
                modifier =
                    Modifier
                        .padding(start = 16.dp)
                        .fillMaxSize()
                        .weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                CipherText()
            }

            if (showCipherActions) {
                IconButton(onClick = { showMoreOptions() }) {
                    Icon(Icons.Default.MoreHoriz, contentDescription = null)
                }

                CipherActionsSheet(
                    state = sheetState,
                    onClick = { onClick(cipher) },
                    onEdit = { onEdit(cipher) },
                    onDelete = { onDelete(cipher) }
                )
            }
        }
    }
}

@Composable
fun CipherActionsSheet(
    state: BottomSheetState,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    PickerBottomSheet(
        state = state,
        navigationBarPadding = true,
        items =
            listOf(
                R.string.View,
                R.string.Edit,
                R.string.Delete
            ),
        onSelected = {
            when (it) {
                R.string.View -> {
                    onClick()
                }

                R.string.Edit -> onEdit()
                R.string.Delete -> onDelete()
            }
        }
    ) {
        Text(
            text = stringResource(it),
            modifier =
                Modifier
                    .padding(vertical = 12.dp)
                    .fillMaxWidth()
        )
    }
}
