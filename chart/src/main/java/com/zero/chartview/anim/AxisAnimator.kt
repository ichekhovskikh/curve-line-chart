package com.zero.chartview.anim

import android.animation.Animator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.view.animation.DecelerateInterpolator
import com.zero.chartview.BuildConfig
import com.zero.chartview.model.FloatRange
import com.zero.chartview.model.ZeroRange
import com.zero.chartview.tools.AnimatorListenerAdapter

internal class AxisAnimator(
    var duration: Long = BuildConfig.ANIMATION_DURATION_MS,
    var onUpdate: ((startX: Float, endX: Float, startY: Float, endY: Float) -> Unit)? = null
) {
    private var doOnEndListener: (() -> Unit)? = null

    private val rangeAnimator = ValueAnimator().apply {
        interpolator = DecelerateInterpolator()
        duration = this@AxisAnimator.duration
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                doOnEndListener?.invoke()
            }
        })
        addUpdateListener { animator ->
            onUpdate?.invoke(
                animator.getAnimatedValue(START_X) as Float,
                animator.getAnimatedValue(END_X) as Float,
                animator.getAnimatedValue(START_Y) as Float,
                animator.getAnimatedValue(END_Y) as Float
            )
        }
    }

    fun doOnEnd(listener: (() -> Unit)? = null) = apply {
        doOnEndListener = listener
    }

    fun cancel() {
        rangeAnimator.cancel()
    }

    fun start(
        fromXRange: FloatRange,
        toXRange: FloatRange,
        fromYRange: FloatRange = ZeroRange(),
        toYRange: FloatRange = ZeroRange()
    ) {
        val startXProperty = PropertyValuesHolder.ofFloat(START_X, fromXRange.start, toXRange.start)
        val endXProperty = PropertyValuesHolder.ofFloat(END_X, fromXRange.endInclusive, toXRange.endInclusive)
        val startYProperty = PropertyValuesHolder.ofFloat(START_Y, fromYRange.start, toYRange.start)
        val endYProperty = PropertyValuesHolder.ofFloat(END_Y, fromYRange.endInclusive, toYRange.endInclusive)
        rangeAnimator.setValues(startXProperty, endXProperty, startYProperty, endYProperty)
        rangeAnimator.start()
    }

    fun reStart(
        fromXRange: FloatRange,
        toXRange: FloatRange,
        fromYRange: FloatRange = ZeroRange(),
        toYRange: FloatRange = ZeroRange()
    ) {
        cancel()
        start(fromXRange, toXRange, fromYRange, toYRange)
    }

    private companion object {
        const val START_X = "START_X"
        const val END_X = "END_X"
        const val START_Y = "START_Y"
        const val END_Y = "END_Y"
    }
}