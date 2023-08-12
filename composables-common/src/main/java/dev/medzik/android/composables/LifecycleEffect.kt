package dev.medzik.android.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * Creates a composable effect that executes the given handler function
 * when the lifecycle of the current composable reaches the given event.
 * @param event The lifecycle event to listen for.
 * @param skip The number of times the event must be received before the `handler` function is executed.
 * @param handler The function to execute when the event is received.
 */
@Composable
fun LifecycleEffect(event: Lifecycle.Event, skip: Int = 1, handler: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var skipped by remember { mutableIntStateOf(0) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, e ->
            if (e == event) {
                if (skipped < skip) {
                    skipped += 1
                } else {
                    handler()
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
