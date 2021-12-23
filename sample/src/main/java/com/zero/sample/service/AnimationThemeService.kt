package com.zero.sample.service

import android.animation.ValueAnimator
import androidx.core.graphics.ColorUtils
import android.view.animation.DecelerateInterpolator
import com.zero.sample.Stainable

class AnimationThemeService(val duration: Long = 300L, var onInvalidate: ((Stainable.ThemeColors) -> Unit)? = null) {

    private val colorAnimator = createColorAnimator()

    private var oldColor = Stainable.ThemeColors()
    private var newColor = Stainable.ThemeColors()

    fun updateTheme(oldColors: Stainable.ThemeColors, newColors: Stainable.ThemeColors) {
        this.oldColor = oldColors
        this.newColor = newColors
        colorAnimator.cancel()
        colorAnimator.start()
    }

    private fun createColorAnimator() =
        ValueAnimator.ofFloat(0.0f, 1.0f).apply {
            interpolator = DecelerateInterpolator()
            duration = this@AnimationThemeService.duration

            addUpdateListener {
                val value = animatedValue as Float
                onInvalidate?.invoke(createColors(value))
            }
        }

    private fun createColors(animatedValue: Float): Stainable.ThemeColors {
        val colors = Stainable.ThemeColors()
        colors.colorBackground = ColorUtils.blendARGB(oldColor.colorBackground, newColor.colorBackground, animatedValue)
        colors.colorLegend = ColorUtils.blendARGB(oldColor.colorLegend, newColor.colorLegend, animatedValue)
        colors.colorGrid = ColorUtils.blendARGB(oldColor.colorGrid, newColor.colorGrid, animatedValue)
        colors.colorPopupLine = ColorUtils.blendARGB(oldColor.colorPopupLine, newColor.colorPopupLine, animatedValue)
        colors.colorFrameSelector =
            ColorUtils.blendARGB(oldColor.colorFrameSelector, newColor.colorFrameSelector, animatedValue)
        colors.colorFogSelector = ColorUtils.blendARGB(oldColor.colorFogSelector, newColor.colorFogSelector, animatedValue)
        colors.colorTitle = ColorUtils.blendARGB(oldColor.colorTitle, newColor.colorTitle, animatedValue)
        colors.colorLabel = ColorUtils.blendARGB(oldColor.colorLabel, newColor.colorLabel, animatedValue)
        return colors
    }

}
