package com.zero.sample.service

import com.zero.sample.Stainable

class ColoringService(private val stainable: Stainable) {

    var lightColors = Stainable.ThemeColors()
        private set

    var darkColors = Stainable.ThemeColors()
        private set

    fun setLightThemeColor(
        colorBackground: Int = lightColors.colorBackground,
        colorLegend: Int = lightColors.colorLegend,
        colorGrid: Int = lightColors.colorGrid,
        colorPopupLine: Int = lightColors.colorPopupLine,
        colorFrameSelector: Int = lightColors.colorFrameSelector,
        colorFogSelector: Int = lightColors.colorFogSelector,
        colorTitle: Int = lightColors.colorTitle,
        colorLabel: Int = lightColors.colorLabel
    ) {
        lightColors = Stainable.ThemeColors(
            colorBackground,
            colorLegend,
            colorGrid,
            colorPopupLine,
            colorFrameSelector,
            colorFogSelector,
            colorTitle,
            colorLabel
        )
    }

    fun setDarkThemeColor(
        colorBackground: Int = darkColors.colorBackground,
        colorLegend: Int = darkColors.colorLegend,
        colorGrid: Int = darkColors.colorGrid,
        colorPopupLine: Int = darkColors.colorPopupLine,
        colorFrameSelector: Int = darkColors.colorFrameSelector,
        colorFogSelector: Int = darkColors.colorFogSelector,
        colorTitle: Int = darkColors.colorTitle,
        colorLabel: Int = darkColors.colorLabel
    ) {
        darkColors = Stainable.ThemeColors(
            colorBackground,
            colorLegend,
            colorGrid,
            colorPopupLine,
            colorFrameSelector,
            colorFogSelector,
            colorTitle,
            colorLabel
        )
    }

    fun updateColors(theme: ThemeStyle) {
        if (theme == ThemeStyle.LIGHT) {
            setColors(lightColors)
        } else {
            setColors(darkColors)
        }
    }

    private fun setColors(colors: Stainable.ThemeColors) {
        stainable.setThemeColors(colors)
    }

    enum class ThemeStyle {
        LIGHT,
        DARK
    }
}