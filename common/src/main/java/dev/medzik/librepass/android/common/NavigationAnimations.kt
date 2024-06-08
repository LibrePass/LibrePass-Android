package dev.medzik.librepass.android.common

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

object NavigationAnimations {
    private const val TRANSMISSION_DURATION_MILLS = 400

    private fun <T> tweenEasing(
        durationMillis: Int
    ) = tween<T>(
        durationMillis = durationMillis,
        easing = LinearOutSlowInEasing,
    )

    private fun <T> tweenFade(
        durationMillis: Int
    ) = tween<T>(
        durationMillis = durationMillis,
        easing = FastOutLinearInEasing,
    )

    fun enterTransition(): EnterTransition = slideInHorizontally(
        animationSpec = tweenEasing(TRANSMISSION_DURATION_MILLS),
        initialOffsetX = { fullWidth -> fullWidth / 12 },
    ) + fadeIn(
        animationSpec = tweenEasing(TRANSMISSION_DURATION_MILLS / 2),
    )

    fun exitTransition(): ExitTransition = slideOutHorizontally(
        animationSpec = tweenFade(TRANSMISSION_DURATION_MILLS),
        targetOffsetX = { fullWidth -> fullWidth / 8 },
    ) + fadeOut(
        animationSpec = tweenFade(TRANSMISSION_DURATION_MILLS / 2),
    )

    fun popEnterTransition(): EnterTransition = slideInHorizontally(
        animationSpec = tweenEasing(TRANSMISSION_DURATION_MILLS),
        initialOffsetX = { fullWidth -> -fullWidth / 12 },
    ) + fadeIn(
        animationSpec = tweenEasing(TRANSMISSION_DURATION_MILLS / 2),
    )

    fun popExitTransition(): ExitTransition = slideOutHorizontally(
        animationSpec = tweenFade(TRANSMISSION_DURATION_MILLS),
        targetOffsetX = { fullWidth -> fullWidth / 8 },
    ) + fadeOut(
        animationSpec = tweenFade(TRANSMISSION_DURATION_MILLS / 2),
    )
}
