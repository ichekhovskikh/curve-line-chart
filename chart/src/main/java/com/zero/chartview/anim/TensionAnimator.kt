package com.zero.chartview.anim

import android.animation.Animator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.view.animation.DecelerateInterpolator
import com.zero.chartview.BuildConfig
import com.zero.chartview.tools.AnimatorListenerAdapter

internal class TensionAnimator(
    var duration: Long = BuildConfig.ANIMATION_DURATION_MS,
    var onUpdate: ((tension: Float, min: Float, max: Float) -> Unit)? = null
) {
    private var doOnEndListener: (() -> Unit)? = null

    private val tensionAnimator = ValueAnimator().apply {
        interpolator = DecelerateInterpolator()
        duration = this@TensionAnimator.duration
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                doOnEndListener?.invoke()
            }
        })
        addUpdateListener { animator ->
            onUpdate?.invoke(
                animator.getAnimatedValue(TENSION) as Float,
                animator.getAnimatedValue(MIN) as Float,
                animator.getAnimatedValue(MAX) as Float
            )
        }
    }

    fun doOnEnd(listener: (() -> Unit)? = null) {
        doOnEndListener = listener
    }

    fun cancel() {
        tensionAnimator.cancel()
    }

    fun start(
        fromMin: Float,
        toMin: Float,
        fromMax: Float,
        toMax: Float
    ) {
        start(0f, 1f, fromMin, toMin, fromMax, toMax)
    }

    fun start(
        fromTension: Float,
        toTension: Float,
        fromMin: Float,
        toMin: Float,
        fromMax: Float,
        toMax: Float
    ) {
        val tensionProperty = PropertyValuesHolder.ofFloat(TENSION, fromTension, toTension)
        val minProperty = PropertyValuesHolder.ofFloat(MIN, fromMin, toMin)
        val maxProperty = PropertyValuesHolder.ofFloat(MAX, fromMax, toMax)
        tensionAnimator.setValues(tensionProperty, maxProperty, minProperty)
        tensionAnimator.start()
    }

    fun reStart(
        fromMin: Float,
        toMin: Float,
        fromMax: Float,
        toMax: Float
    ) {
        reStart(0f, 1f, fromMin, toMin, fromMax, toMax)
    }

    fun reStart(
        fromTension: Float,
        toTension: Float,
        fromMin: Float,
        toMin: Float,
        fromMax: Float,
        toMax: Float
    ) {
        cancel()
        start(fromTension, toTension, fromMin, toMin, fromMax, toMax)
    }

    private companion object {
        const val TENSION = "TENSION"
        const val MIN = "MIN"
        const val MAX = "MAX"
    }
}