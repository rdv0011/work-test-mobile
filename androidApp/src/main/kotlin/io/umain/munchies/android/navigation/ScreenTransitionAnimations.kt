package io.umain.munchies.android.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

object ScreenTransitionAnimations {
    const val TRANSITION_DURATION_MS = 300

    fun enter(isRtl: Boolean): EnterTransition =
        (if (isRtl) slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = androidx.compose.animation.core.tween(TRANSITION_DURATION_MS)
        )
        else slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = androidx.compose.animation.core.tween(TRANSITION_DURATION_MS)
        )) + fadeIn(animationSpec = androidx.compose.animation.core.tween(TRANSITION_DURATION_MS))

    fun exit(isRtl: Boolean): ExitTransition =
        (if (isRtl) slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = androidx.compose.animation.core.tween(TRANSITION_DURATION_MS)
        )
        else slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = androidx.compose.animation.core.tween(TRANSITION_DURATION_MS)
        )) + fadeOut(animationSpec = androidx.compose.animation.core.tween(TRANSITION_DURATION_MS))

    fun popEnter(isRtl: Boolean): EnterTransition =
        (if (isRtl) slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = androidx.compose.animation.core.tween(TRANSITION_DURATION_MS)
        )
        else slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = androidx.compose.animation.core.tween(TRANSITION_DURATION_MS)
        )) + fadeIn(animationSpec = androidx.compose.animation.core.tween(TRANSITION_DURATION_MS))

    fun popExit(isRtl: Boolean): ExitTransition =
        (if (isRtl) slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = androidx.compose.animation.core.tween(TRANSITION_DURATION_MS)
        )
        else slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = androidx.compose.animation.core.tween(TRANSITION_DURATION_MS)
        )) + fadeOut(animationSpec = androidx.compose.animation.core.tween(TRANSITION_DURATION_MS))

    val fadeInAnimation: EnterTransition = fadeIn(
        animationSpec = androidx.compose.animation.core.tween(TRANSITION_DURATION_MS)
    )

    val fadeOutAnimation: ExitTransition = fadeOut(
        animationSpec = androidx.compose.animation.core.tween(TRANSITION_DURATION_MS)
    )

    val fadeOnlyTransition: ContentTransform = fadeInAnimation.togetherWith(fadeOutAnimation)
}