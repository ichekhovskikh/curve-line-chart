package com.zero.sample.service

import com.zero.sample.Stainable

class ColoringService(private val stainable: Stainable) {

    var lightColors = Stainable.ThemeColor()
        private set

    var darkColors = Stainable.ThemeColor()
        private set

    fun setLightThemeColor(
        colorBackground: Int = lightColors.colorBackground,
        colorLegend: Int = lightColors.colorLegend,
        colorGrid: Int = lightColors.colorGrid,
        colorPopupLine: Int = lightColors.colorPopupLine,
        colorFrameControl: Int = lightColors.colorFrameControl,
        colorFogControl: Int = lightColors.colorFogControl,
        colorTitle: Int = lightColors.colorTitle,
        colorLabel: Int = lightColors.colorLabel
    ) {
        lightColors = Stainable.ThemeColor(
            colorBackground,
            colorLegend,
            colorGrid,
            colorPopupLine,
            colorFrameControl,
            colorFogControl,
            colorTitle,
            colorLabel
        )
    }

    fun setDarkThemeColor(
        colorBackground: Int = darkColors.colorBackground,
        colorLegend: Int = darkColors.colorLegend,
        colorGrid: Int = darkColors.colorGrid,
        colorPopupLine: Int = darkColors.colorPopupLine,
        colorFrameControl: Int = darkColors.colorFrameControl,
        colorFogControl: Int = darkColors.colorFogControl,
        colorTitle: Int = darkColors.colorTitle,
        colorLabel: Int = darkColors.colorLabel
    ) {
        darkColors = Stainable.ThemeColor(
            colorBackground,
            colorLegend,
            colorGrid,
            colorPopupLine,
            colorFrameControl,
            colorFogControl,
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

    private fun setColors(colors: Stainable.ThemeColor) {
        stainable.updateColors(colors)
    }

    enum class ThemeStyle {
        LIGHT,
        DARK
    }
}