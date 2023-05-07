package dev.medzik.librepass.android.ui.composables.common

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun LoadingDot(
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
fun LoadingIndicator(
    animating: Boolean,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    indicatorSpacing: Dp = 4.dp
) {
    val animatedValues = List(3) { index ->
        var animatedValue by remember(key1 = animating) { mutableStateOf(0f) }

        LaunchedEffect(key1 = animating) {
            if (animating) {
                animate(
                    initialValue = 8 / 2f,
                    targetValue = -8 / 2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 300),
                        repeatMode = RepeatMode.Reverse,
                        initialStartOffset = StartOffset(300 / 3 * index)
                    )
                ) { value, _ -> animatedValue = value }
            }
        }

        animatedValue
    }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        animatedValues.forEach { animatedValue ->
            LoadingDot(
                modifier = Modifier
                    .padding(horizontal = indicatorSpacing)
                    .width(8.dp)
                    .aspectRatio(1f)
                    .offset(y = animatedValue.dp),
                color = color
            )
        }
    }
}
