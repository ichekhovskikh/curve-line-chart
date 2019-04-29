package com.zero.chartview.service

import android.animation.Animator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.view.animation.DecelerateInterpolator
import com.zero.chartview.model.AnimatingCurveLine
import com.zero.chartview.model.CurveLine
import com.zero.chartview.utils.AnimatorListenerAdapter

internal class AnimationLineService(var duration: Long = 300L, var onInvalidate: (() -> Unit)? = null) {

    var maxY = 0F
        private set
    var minY = 0F
        private set

    internal var animationLines: MutableList<AnimatingCurveLine> = mutableListOf()
        private set

    private val appearanceAnimator = createAppearanceAnimator(onEnd = ::onAnimationEnd)
    private val tensionAnimator = createTensionAnimator()

    fun getLines() = animationLines.map { it.curveLine }

    fun setLines(newLines: List<CurveLine>) {
        val current = animationLines.map { it.curveLine }

        if (newLines == current) return

        appearanceAnimator.cancel()
        val appearing = newLines.minus(current)
        appearing.forEach { line ->
            animationLines.add(AnimatingCurveLine(line, true, 0F))
        }

        val disappearing = current.minus(newLines)
        disappearing.forEach { line ->
            animationLines.find { it.curveLine == line }
                ?.apply {
                    isAppearing = false
                    animationValue = 0f }
        }
        appearanceAnimator.start()
    }

    fun addLine(line: CurveLine) {
        val current = animationLines.map { it.curveLine }
        if (current.contains(line)) return

        appearanceAnimator.cancel()
        animationLines.add(AnimatingCurveLine(line, true, 0F))
        appearanceAnimator.start()
    }

    fun removeLine(line: CurveLine) {
        val current = animationLines.map { it.curveLine }
        if (!current.contains(line)) return

        appearanceAnimator.cancel()
        animationLines.find { it.curveLine == line }
            ?.apply {
                isAppearing = false
                animationValue = 0f
            }
        appearanceAnimator.start()
    }

    fun setYAxis(minY: Float, maxY: Float) {
        if (this.maxY == maxY && this.minY == minY) return

        tensionAnimator.cancel()
        val minProperty = PropertyValuesHolder.ofFloat(MIN_Y, this.minY, minY)
        val maxProperty = PropertyValuesHolder.ofFloat(MAX_Y, this.maxY, maxY)
        tensionAnimator.setValues(maxProperty, minProperty)
        tensionAnimator.start()
    }

    private fun onAnimationEnd() {
        animationLines.removeAll { !it.isAppearing }
    }

    private fun createAppearanceAnimator(onEnd: (() -> Unit)? = null) =
        ValueAnimator.ofFloat(0F, 1F).apply {

            duration = this@AnimationLineService.duration

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    onEnd?.invoke()
                }
            })

            addUpdateListener { animator ->
                val animatorValue = animator.animatedValue as Float
                animationLines.forEach { line ->
                    if (line.animationValue <= animatorValue) {
                        line.animationValue = animatorValue
                        onInvalidate?.invoke()
                    }
                }
            }
        }

    private fun createTensionAnimator() =
        ValueAnimator().apply {
            interpolator = DecelerateInterpolator()
            duration = this@AnimationLineService.duration

            addUpdateListener { animator ->
                minY = animator.getAnimatedValue(MIN_Y) as Float
                maxY = animator.getAnimatedValue(MAX_Y) as Float
                onInvalidate?.invoke()
            }
        }

    companion object {
        private const val MIN_Y = "MIN_Y"
        private const val MAX_Y = "MAX_Y"
    }
}