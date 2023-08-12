package dev.medzik.android.composables.sheet

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import dev.medzik.android.composables.ElevationTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseBottomSheet(
    state: BottomSheetState,
    content: @Composable (ColumnScope.() -> Unit)
) {
    if (state.expanded) {
        ModalBottomSheet(
            tonalElevation = ElevationTokens.Level2,
            onDismissRequest = { state.expanded = false },
            sheetState = state.sheetState,
            content = content
        )
    }
}
