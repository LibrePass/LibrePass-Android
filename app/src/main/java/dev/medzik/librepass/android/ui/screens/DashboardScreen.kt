package dev.medzik.librepass.android.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.medzik.librepass.android.ui.composable.TopBar
import dev.medzik.librepass.android.ui.theme.LibrePassTheme

@Composable
fun DashboardScreen() {
    Scaffold(
        topBar = {
            TopBar(title = "Dashboard")
        },
        modifier = Modifier.navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp) // TopBar padding
                .padding(top = 20.dp)
                .padding(horizontal = 16.dp),
        ) {
            Text(text = "Column")
        }
    }
}

@Preview
@Composable
fun DashboardScreenPreview() {
    LibrePassTheme {
        DashboardScreen()
    }
}
