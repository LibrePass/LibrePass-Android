package dev.medzik.android.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * Runs the provided code block on the main user interface (UI) thread using Kotlin Coroutines.
 * @param block The code block containing operations to be executed on the UI thread.
 */
fun runOnUiThread(block: suspend () -> Unit) = MainScope().launch(Dispatchers.Main) { block() }
