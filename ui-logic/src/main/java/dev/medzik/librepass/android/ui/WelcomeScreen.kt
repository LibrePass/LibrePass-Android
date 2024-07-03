package dev.medzik.librepass.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.SyncLock
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import dev.medzik.android.compose.ui.IconBox
import dev.medzik.librepass.android.ui.auth.Login
import dev.medzik.librepass.android.ui.auth.Signup
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
fun WelcomeScreen(navController: NavController) {
    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            item {}

            item {
                WelcomePager()
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .height(50.dp)
                            .fillMaxWidth(0.75f),
                        onClick = { navController.navigate(Signup) }
                    ) {
                        Text(
                            text = stringResource(R.string.Signup),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Button(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .height(50.dp)
                            .fillMaxWidth(0.75f),
                        colors = ButtonDefaults.buttonColors().copy(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary,
                        ),
                        onClick = { navController.navigate(Login) }
                    ) {
                        Text(
                            text = stringResource(R.string.Login),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomePager() {
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(
        pageCount = { 3 }
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .height(300.dp)
                        .graphicsLayer {
                            // Calculate the absolute offset for the current page from the
                            // scroll position. We use the absolute value which allows us to mirror
                            // any effects for both directions
                            val pageOffset = (
                                    (pagerState.currentPage - page) + pagerState
                                        .currentPageOffsetFraction
                                    ).absoluteValue

                            // We animate the alpha, between 50% and 100%
                            alpha = lerp(
                                start = 0.5f,
                                stop = 1f,
                                fraction = 1f - pageOffset.coerceIn(0f, 1f)
                            )
                        },
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (page) {
                            0 -> WelcomeFirstPager()
                            1 -> WelcomeSecondPager()
                            2 -> WelcomeThirdPager()
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) {
                    MaterialTheme.colorScheme.primary
                } else MaterialTheme.colorScheme.surfaceVariant

                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(16.dp)
                        .clickable { scope.launch { pagerState.animateScrollToPage(iteration) } }
                )
            }
        }
    }
}

@Composable
private fun WelcomeFirstPager() {
    IconBox(
        imageVector = Icons.Default.VpnLock,
        modifier = Modifier.size(128.dp)
    )

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        text = stringResource(R.string.Welcome_FirstCardTitle),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center
    )

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        text = stringResource(R.string.Welcome_FirstCardSubtitle),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun WelcomeSecondPager() {
    IconBox(
        imageVector = Icons.Default.Password,
        modifier = Modifier.size(128.dp)
    )

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        text = stringResource(R.string.Welcome_PasswordGeneratorCardTitle),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center
    )

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        text = stringResource(R.string.Welcome_PasswordGeneratorSubtitle),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun WelcomeThirdPager() {
    IconBox(
        imageVector = Icons.Default.SyncLock,
        modifier = Modifier.size(128.dp)
    )

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        text = stringResource(R.string.Welcome_SyncCardTitle),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center
    )

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        text = stringResource(R.string.Welcome_PasswordGeneratorSubtitle),
        textAlign = TextAlign.Center
    )
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen(rememberNavController())
}
