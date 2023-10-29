package dev.medzik.android.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun <T> rememberMutable(value: T) = remember { mutableStateOf(value) }

@Composable
fun rememberMutableString(value: String = "") = rememberMutable(value)

@Composable
fun rememberMutableBoolean(value: Boolean = false) = rememberMutable(value)
