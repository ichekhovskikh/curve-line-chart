package com.zero.chartview.service

import android.animation.Animator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import com.zero.chartview.model.AnimatingCurveLine
import com.zero.chartview.model.CurveLine
import com.zero.chartview.utils.AnimatorAdater
import com.zero.chartview.utils.findMaxYValue
import com.zero.chartview.utils.findMinYValue

class AnimationLineService(
    var duration: Long = 300L,
    private val onInvalidate: (() -> Unit)? = null
) {

    var maxY = 0F
    var minY = 0F

    var lines: MutableList<AnimatingCurveLine> = mutableListOf()
        private set

    private val appearanceAnimator = createAppearanceAnimator(onEnd = ::onAnimationEnd)
    private val tensionAnimator = createTensionAnimator()

    fun setLines(newLines: List<CurveLine>) {
        val current = lines.map { it.curveLine }

        if (newLines == current) return

        val appearing = newLines.minus(current)
        appearing.forEach { line ->
            lines.add(AnimatingCurveLine(line, true, 0F))
        }

        val disappearing = current.minus(newLines)
        disappearing.forEach { line ->
            lines.find { it.curveLine == line }
                .apply { lines.add(AnimatingCurveLine(line, false, 0F)) }
        }
        appearanceAnimator.start()
        updateYAxis()
    }

    private fun updateYAxis() {
        val current = lines.map { it.curveLine }
        val newMaxY = findMaxYValue(current)
        val newMinY = findMinYValue(current)

        if (maxY == newMaxY && minY == newMinY) return

        tensionAnimator.setValues(PropertyValuesHolder.ofFloat("newMaxY", newMaxY))
        tensionAnimator.setValues(PropertyValuesHolder.ofFloat("newMinY", newMinY))
        tensionAnimator.setValues(PropertyValuesHolder.ofFloat("oldMaxY", maxY))
        tensionAnimator.setValues(PropertyValuesHolder.ofFloat("oldMinY", minY))

        tensionAnimator.start()
    }

    private fun onAnimationEnd() {
        lines.removeAll { !it.isAppearing }
    }

    private fun createAppearanceAnimator(onEnd: (() -> Unit)? = null) =
        ValueAnimator.ofFloat(0F, 1F).apply {

            duration = this@AnimationLineService.duration

            addListener(object : AnimatorAdater() {
                override fun onAnimationEnd(animation: Animator?) {
                    onEnd?.invoke()
                }
            })

            addUpdateListener { animator ->
                val animatorValue = animator.animatedValue as Float
                lines.forEach { line ->
                    if (line.animationValue <= animatorValue) {
                        line.animationValue = animatorValue
                        onInvalidate?.invoke()
                    }
                }
            }
        }

    private fun createTensionAnimator() =
        ValueAnimator.ofFloat(0F, 1F).apply {

            duration = this@AnimationLineService.duration

            addUpdateListener { animator ->
                val newMaxY = getAnimatedValue("newMaxY") as Float
                val oldMaxY = getAnimatedValue("oldMaxY") as Float
                val value = animator.animatedValue as Float

                val distance = newMaxY - maxY
                val animatedMaxY = oldMaxY + distance * value
                if (animatedMaxY != maxY) {
                    maxY = animatedMaxY
                    onInvalidate?.invoke()
                }
            }

            addUpdateListener { animator ->
                val newMinY = getAnimatedValue("newMinY") as Float
                val oldMinY = getAnimatedValue("oldMinY") as Float
                val value = animator.animatedValue as Float

                val distance = minY - newMinY
                val animatedMinY = oldMinY - distance * value
                if (animatedMinY != minY) {
                    minY = animatedMinY
                    onInvalidate?.invoke()
                }
            }
        }

}