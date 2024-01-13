package dev.medzik.android.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberBottomSheetState(): BottomSheetState {
    val sheetState = SheetState(skipPartiallyExpanded = true) // , density = LocalDensity.current)
    return remember { BottomSheetState(sheetState) }
}

/** A visibility controller for a bottom sheet */
@OptIn(ExperimentalMaterial3Api::class)
class BottomSheetState(
    internal val sheetState: SheetState
) {
    internal var expanded by mutableStateOf(false)

    /** Show the bottom sheet. */
    suspend fun hide() {
        sheetState.hide()
        expanded = false
    }

    /** Show the bottom sheet. */
    fun show() {
        expanded = true
    }
}

/**
 * A composable function for displaying a basic bottom sheet.
 * @param state the state that controls visibility of the bottom sheet
 * @param navigationBarPadding determines whether the bottom sheet should have navigation bar padding
 * @param content the content of the bottom sheet to display
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseBottomSheet(
    state: BottomSheetState,
    navigationBarPadding: Boolean = false,
    content: @Composable (ColumnScope.() -> Unit)
) {
    val scope = rememberCoroutineScope()

    if (state.expanded) {
        ModalBottomSheet(
            tonalElevation = ElevationTokens.Level2,
            onDismissRequest = {
                scope.launch { state.hide() }
            },
            sheetState = state.sheetState
        ) {
            content()

            if (navigationBarPadding) {
                Spacer(
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        }
    }
}

/**
 * A composable function for displaying a picker bottom sheet with a list of items.
 * @param state the state that controls visibility of the bottom sheet
 * @param items the list of items to display in the bottom sheet
 * @param onSelected a callback function invoked when the item is selected
 * @param navigationBarPadding determines whether the bottom sheet should have navigation bar padding
 * @param content composable lambda that defines the visual representation of each item in the picker
 */
@Composable
fun <T> PickerBottomSheet(
    state: BottomSheetState,
    items: List<T>,
    onSelected: (T) -> Unit,
    navigationBarPadding: Boolean = false,
    content: @Composable (T) -> Unit
) {
    val scope = rememberCoroutineScope()

    BaseBottomSheet(
        state,
        navigationBarPadding
    ) {
        Column {
            items.forEach { item ->
                Box(
                    modifier =
                        Modifier
                            .clickable {
                                scope.launch { state.hide() }
                                onSelected(item)
                            }
                            .padding(horizontal = 24.dp),
                ) {
                    content(item)
                }
            }
        }
    }
}
