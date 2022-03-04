package com.chekh.chartview.anim

import android.animation.Animator
import android.animation.ValueAnimator
import com.chekh.chartview.BuildConfig
import com.chekh.chartview.tools.AnimatorListenerAdapter

/**
 * The animator of the appearance and disappearance of objects
 */
internal class AppearanceAnimator(
    var duration: Long = BuildConfig.ANIMATION_DURATION_MS,
    var onUpdate: ((value: Float) -> Unit)? = null
) {
    private var doOnEndListener: (() -> Unit)? = null

    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
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
        animator.cancel()
    }

    fun start() {
        animator.start()
    }

    fun reStart() {
        cancel()
        start()
    }
}
