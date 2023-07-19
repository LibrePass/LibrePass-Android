package dev.medzik.android.composables.sheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseBottomSheet(
    state: BottomSheetState,
    content: @Composable () -> Unit
) {
    if (state.expanded) {
        ModalBottomSheet(
            // TODO: I don't know if this is how it's done but it works
            tonalElevation = 3.dp,
            onDismissRequest = { state.expanded = false },
            sheetState = state.sheetState
        ) {
            content()
        }
    }
}
