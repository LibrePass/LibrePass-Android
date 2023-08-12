package dev.medzik.android.composables.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun rememberDialogState() = remember { DialogState() }

class DialogState {
    var isVisible by mutableStateOf(false)

    fun dismiss() {
        isVisible = false
    }

    fun show() {
        isVisible = true
    }
}
