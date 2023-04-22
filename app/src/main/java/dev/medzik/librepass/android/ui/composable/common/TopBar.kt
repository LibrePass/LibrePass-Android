package dev.medzik.librepass.android.ui.composable.common

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    navigationIcon: @Composable (() -> Unit)? = null,
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = navigationIcon ?: {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarTwoColor(text1: String, text2: String) {
    TopAppBar(
        title = {
            Row {
                Text(
                    text = text1,
                    style = MaterialTheme.typography.titleLarge,
                )

                Text(
                    text = text2,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
    )
}
