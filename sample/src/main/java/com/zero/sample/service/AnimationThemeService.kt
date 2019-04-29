package com.zero.sample.service

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
        //TODO make 1 value
        val colorLegend =
            PropertyValuesHolder.ofObject(COLOR_LEGEND, evaluator, fromTheme.colorLegend, toTheme.colorLegend)
        val colorGrid =
            PropertyValuesHolder.ofObject(COLOR_GRID, evaluator, fromTheme.colorGrid, toTheme.colorGrid)
        val colorPopupLine =
            PropertyValuesHolder.ofObject(COLOR_POPUP_LINE, evaluator, fromTheme.colorPopupLine, toTheme.colorPopupLine)
        val colorFrameControl =
            PropertyValuesHolder.ofObject(COLOR_FRAME_CONTROL, evaluator, fromTheme.colorFrameControl, toTheme.colorFrameControl)
        val colorFogControl = PropertyValuesHolder.ofObject(
            COLOR_FOG_CONTROL, evaluator, fromTheme.colorFogControl, toTheme.colorFogControl
        )

        colorAnimator.setValues(
            colorBackground,
            colorLegend,
            colorGrid,
            colorPopupLine,
            colorFrameControl,
            colorFogControl
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
                    animator.getAnimatedValue(COLOR_GRID) as Int,
                    animator.getAnimatedValue(COLOR_POPUP_LINE) as Int,
                    animator.getAnimatedValue(COLOR_FRAME_CONTROL) as Int,
                    animator.getAnimatedValue(COLOR_FOG_CONTROL) as Int
                )
                onInvalidate?.invoke(colors)
            }
        }

    companion object {
        private const val COLOR_BACKGROUND = "COLOR_BACKGROUND"
        private const val COLOR_LEGEND = "COLOR_LEGEND"
        private const val COLOR_GRID = "COLOR_GRID"
        private const val COLOR_POPUP_LINE = "COLOR_POPUP_LINE"
        private const val COLOR_FRAME_CONTROL = "COLOR_FRAME_CONTROL"
        private const val COLOR_FOG_CONTROL = "COLOR_FOG_CONTROL"
    }
}