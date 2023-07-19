package dev.medzik.android.composables.dialog

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun <T> PickerDialog(
    state: DialogState,
    @StringRes title: Int,
    items: List<T>,
    onSelected: (T) -> Unit,
    content: @Composable (T) -> Unit
) {
    BaseDialog(state) {
        Column {
            Text(
                text = stringResource(title),
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 8.dp)
            )

            items.forEach { item ->
                Box(
                    modifier = Modifier
                        .clickable {
                            onSelected(item)
                            state.dismiss()
                        }
                        .padding(horizontal = 24.dp)
                ) {
                    content(item)
                }
            }
        }
    }
}
