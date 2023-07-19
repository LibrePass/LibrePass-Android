package dev.medzik.android.composables.sheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun rememberBottomSheetState() = remember { BottomSheetState() }

class BottomSheetState {
    @OptIn(ExperimentalMaterial3Api::class)
    internal val sheetState = SheetState(skipPartiallyExpanded = true)
    internal var expanded by mutableStateOf(false)

    @OptIn(ExperimentalMaterial3Api::class)
    suspend fun dismiss() {
        sheetState.hide()
        expanded = false
    }

    fun expand() {
        expanded = true
    }
}
