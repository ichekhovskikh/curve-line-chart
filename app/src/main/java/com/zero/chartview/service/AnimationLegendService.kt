package com.zero.chartview.service

import android.animation.Animator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.view.animation.DecelerateInterpolator
import com.zero.chartview.model.AnimatingLegendSeries
import com.zero.chartview.utils.AnimatorListenerAdapter

class AnimationLegendService(var duration: Long = 300L, var onInvalidate: (() -> Unit)? = null) {

    var maxY = 0F
        private set
    var minY = 0F
        private set

    internal var legendSeries: MutableList<AnimatingLegendSeries> = mutableListOf()
        private set

    private val tensionAnimator = createTensionAnimator(onEnd = ::onAnimationEnd)

    fun setYAxis(minY: Float, maxY: Float) {
        if (this.maxY == maxY && this.minY == minY) return

        tensionAnimator.cancel()
        val transparency = PropertyValuesHolder.ofFloat(TRANSPARENCY_VALUE, 0.8f, 1f)
        val minProperty = PropertyValuesHolder.ofFloat(MIN_Y, this.minY, minY)
        val maxProperty = PropertyValuesHolder.ofFloat(MAX_Y, this.maxY, maxY)
        tensionAnimator.setValues(transparency, maxProperty, minProperty)
        tensionAnimator.start()
    }

    private fun onAnimationEnd() {
        legendSeries.removeAll { !it.isAppearing }
    }

    private fun createTensionAnimator(onEnd: (() -> Unit)? = null) =
        ValueAnimator().apply {
            interpolator = DecelerateInterpolator()
            duration = this@AnimationLegendService.duration

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    onEnd?.invoke()
                }
            })

            addUpdateListener { animator ->
                val transparency = animator.getAnimatedValue(TRANSPARENCY_VALUE) as Float
                minY = animator.getAnimatedValue(MIN_Y) as Float
                maxY = animator.getAnimatedValue(MAX_Y) as Float
                legendSeries.forEach { series ->
                    series.animationValue = Math.max(series.animationValue, transparency)
                }
                onInvalidate?.invoke()
            }
        }

    companion object {
        private const val TRANSPARENCY_VALUE = "TRANSPARENCY_VALUE"
        private const val MIN_Y = "MIN_Y"
        private const val MAX_Y = "MAX_Y"
    }
}