package dev.medzik.librepass.android.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.medzik.android.components.DialogState
import dev.medzik.android.components.PickerDialog
import dev.medzik.librepass.android.R
import dev.medzik.librepass.types.cipher.CipherType

@Composable
fun CipherTypeDialog(
    state: DialogState,
    onSelected: (CipherType) -> Unit
) {
    @Composable
    fun getTranslated(type: CipherType): String {
        return stringResource(
            when (type) {
                CipherType.Login -> R.string.CipherType_Login
                CipherType.SecureNote -> R.string.CipherType_SecureNote
                CipherType.Card -> R.string.CipherType_Card
            }
        )
    }

    PickerDialog(
        state,
        title = stringResource(R.string.SelectCipherType),
        items = CipherType.entries,
        onSelected
    ) {
        Text(
            text = getTranslated(it),
            modifier = Modifier
                .padding(vertical = 12.dp)
                .fillMaxWidth()
        )
    }
}
