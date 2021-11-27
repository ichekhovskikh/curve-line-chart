package com.zero.chartview.anim

import android.animation.Animator
import android.animation.ValueAnimator
import com.zero.chartview.BuildConfig
import com.zero.chartview.tools.AnimatorListenerAdapter

internal class AppearanceAnimator(
    var duration: Long = BuildConfig.ANIMATION_DURATION_MS,
    var onUpdate: ((value: Float) -> Unit)? = null
) {
    private var doOnEndListener: (() -> Unit)? = null

    private val appearanceAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = this@AppearanceAnimator.duration
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                doOnEndListener?.invoke()
            }
        })
        addUpdateListener { animator ->
            onUpdate?.invoke(animator.animatedValue as Float)
        }
    }

    fun doOnEnd(listener: (() -> Unit)? = null) = apply {
        doOnEndListener = listener
    }

    fun cancel() {
        appearanceAnimator.cancel()
    }

    fun start() {
        appearanceAnimator.start()
    }

    fun reStart() {
        cancel()
        start()
    }
}