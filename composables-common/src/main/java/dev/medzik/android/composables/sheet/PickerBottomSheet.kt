package dev.medzik.android.composables.sheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun <T> PickerBottomSheet(
    state: BottomSheetState,
    items: List<T>,
    onSelected: (T) -> Unit,
    content: @Composable (T) -> Unit
) {
    val scope = rememberCoroutineScope()

    BaseBottomSheet(state) {
        Column {
            items.forEach { item ->
                Box(
                    modifier = Modifier
                        .clickable {
                            scope.launch { state.dismiss() }
                            onSelected(item)
                        }
                        .padding(horizontal = 24.dp)
                ) {
                    content(item)
                }
            }
        }
    }
}
