package com.zero.sample.service

import android.animation.ValueAnimator
import android.view.animation.DecelerateInterpolator

class AnimationThemeService(val duration: Long = 300L, var onInvalidate: ((Float) -> Unit)? = null) {

    private val colorAnimator = createColorAnimator()

    fun updateTheme() {
        colorAnimator.cancel()
        colorAnimator.start()
    }

    private fun createColorAnimator() =
        ValueAnimator.ofFloat(0.0f, 1.0f).apply {
            interpolator = DecelerateInterpolator()
            duration = this@AnimationThemeService.duration

            addUpdateListener {
                val animated = animatedValue as Float
                onInvalidate?.invoke(animated)
            }
        }
}