package dev.medzik.android.composables.sheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberBottomSheetState(): BottomSheetState {
    val sheetState = SheetState(skipPartiallyExpanded = true, density = LocalDensity.current)
    return remember { BottomSheetState(sheetState) }
}

@OptIn(ExperimentalMaterial3Api::class)
class BottomSheetState constructor(
    internal val sheetState: SheetState
) {
    internal var expanded by mutableStateOf(false)

    suspend fun dismiss() {
        sheetState.hide()
        expanded = false
    }

    fun expand() {
        expanded = true
    }
}
