package com.zero.chartview

interface Themeable {
    var lightTheme: ThemeColor
    var darkTheme: ThemeColor
    var currentTheme: ThemeColor

    fun setLightThemeColor(
        colorBackground: Int,
        colorLegend: Int,
        colorTitle: Int,
        colorLabel: Int,
        colorGrid: Int
    ) {
        lightTheme =
            ThemeColor(colorBackground, colorLegend, colorTitle, colorLabel, colorGrid)
    }

    fun setDarkThemeColor(
        colorBackground: Int,
        colorLegend: Int,
        colorTitle: Int,
        colorLabel: Int,
        colorGrid: Int
    ) {
        darkTheme =
            ThemeColor(colorBackground, colorLegend, colorTitle, colorLabel, colorGrid)
    }

    fun setLightThemeColor(colorInactiveControl: Int, colorActiveControl: Int) {
        lightTheme = ThemeColor(colorInactiveControl = colorInactiveControl, colorActiveControl = colorActiveControl)
    }

    fun setDarkThemeColor(colorInactiveControl: Int, colorActiveControl: Int) {
        darkTheme = ThemeColor(colorInactiveControl = colorInactiveControl, colorActiveControl = colorActiveControl)
    }

    fun updateTheme(theme: ThemeStyle) {
        if (theme == ThemeStyle.LIGHT) {
            setTheme(lightTheme)
        } else {
            setTheme(darkTheme)
        }
    }

    fun setTheme(colors: ThemeColor)

    enum class ThemeStyle {
        LIGHT,
        DARK
    }

    data class ThemeColor(
        var colorBackground: Int = 0,
        var colorLegend: Int = 0,
        var colorTitle: Int = 0,
        var colorLabel: Int = 0,
        var colorGrid: Int = 0,
        var colorInactiveControl: Int = 0,
        var colorActiveControl: Int = 0
    )
}