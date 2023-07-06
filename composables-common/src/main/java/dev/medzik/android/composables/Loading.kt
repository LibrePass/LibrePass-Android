package dev.medzik.android.composables

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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Only the dot that is used in [LoadingIndicator].
 * @param color The color of the dot.
 * @param modifier The modifier to apply to the dot.
 * @see LoadingIndicator
 */
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

/**
 * A loading indicator that animates three dots in a row.
 * @param animating Whether the indicator should be animating.
 * @param modifier The modifier to apply to the indicator.
 * @param color The color of the indicator.
 * @param indicatorSpacing The spacing between the dots.
 * @see LoadingDot
 */
@Composable
fun LoadingIndicator(
    animating: Boolean,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    indicatorSpacing: Dp = 4.dp
) {
    val animatedValues = List(3) { index ->
        var animatedValue by remember(animating) { mutableFloatStateOf(0f) }

        LaunchedEffect(animating) {
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

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
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

@Preview
@Composable
fun LoadingIndicatorPreview() {
    LoadingIndicator(animating = false)
}
