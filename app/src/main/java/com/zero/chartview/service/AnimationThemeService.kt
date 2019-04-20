package com.zero.chartview.service

import android.animation.ArgbEvaluator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.view.animation.DecelerateInterpolator
import com.zero.chartview.Themeable

class AnimationThemeService(var duration: Long = 300L, var onInvalidate: ((Themeable.ThemeColor) -> Unit)? = null) {

    private val colorAnimator = createColorAnimator()

    fun updateTheme(fromTheme: Themeable.ThemeColor, toTheme: Themeable.ThemeColor) {
        val evaluator = ArgbEvaluator()
        val colorBackground = PropertyValuesHolder.ofObject(
            COLOR_BACKGROUND, evaluator, fromTheme.colorBackground, toTheme.colorBackground
        )
        val colorLegend =
            PropertyValuesHolder.ofObject(COLOR_LEGEND, evaluator, fromTheme.colorLegend, toTheme.colorLegend)
        val colorTitle =
            PropertyValuesHolder.ofObject(COLOR_TITLE, evaluator, fromTheme.colorTitle, toTheme.colorTitle)
        val colorLabel =
            PropertyValuesHolder.ofObject(COLOR_LABEL, evaluator, fromTheme.colorLabel, toTheme.colorLabel)
        val colorGrid =
            PropertyValuesHolder.ofObject(COLOR_GRID, evaluator, fromTheme.colorGrid, toTheme.colorGrid)
        val colorInactiveControl = PropertyValuesHolder.ofObject(
            COLOR_INACTIVE_CONTROL, evaluator, fromTheme.colorInactiveControl, toTheme.colorInactiveControl
        )
        val colorActiveControl = PropertyValuesHolder.ofObject(
            COLOR_ACTIVE_CONTROL, evaluator, fromTheme.colorActiveControl, toTheme.colorActiveControl
        )

        colorAnimator.setValues(
            colorBackground,
            colorLegend,
            colorTitle,
            colorLabel,
            colorGrid,
            colorInactiveControl,
            colorActiveControl
        )
        colorAnimator.start()
    }

    private fun createColorAnimator() =
        ValueAnimator().apply {
            interpolator = DecelerateInterpolator()
            duration = this@AnimationThemeService.duration

            addUpdateListener { animator ->
                val colors = Themeable.ThemeColor(
                    animator.getAnimatedValue(COLOR_BACKGROUND) as Int,
                    animator.getAnimatedValue(COLOR_LEGEND) as Int,
                    animator.getAnimatedValue(COLOR_TITLE) as Int,
                    animator.getAnimatedValue(COLOR_LABEL) as Int,
                    animator.getAnimatedValue(COLOR_GRID) as Int,
                    animator.getAnimatedValue(COLOR_INACTIVE_CONTROL) as Int,
                    animator.getAnimatedValue(COLOR_ACTIVE_CONTROL) as Int
                )
                onInvalidate?.invoke(colors)
            }
        }

    companion object {
        private const val COLOR_BACKGROUND = "COLOR_BACKGROUND"
        private const val COLOR_LEGEND = "COLOR_LEGEND"
        private const val COLOR_TITLE = "COLOR_TITLE"
        private const val COLOR_LABEL = "COLOR_LABEL"
        private const val COLOR_GRID = "COLOR_GRID"
        private const val COLOR_INACTIVE_CONTROL = "COLOR_INACTIVE_CONTROL"
        private const val COLOR_ACTIVE_CONTROL = "COLOR_ACTIVE_CONTROL"
    }
}