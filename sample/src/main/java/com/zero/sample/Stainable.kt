package com.zero.sample

interface Stainable {

    fun getCurrentColors(): ThemeColors

    fun setThemeColors(colors: ThemeColors)

    data class ThemeColors(
        var colorBackground: Int = 0,
        var colorLegend: Int = 0,
        var colorGrid: Int = 0,
        var colorPopupLine: Int = 0,
        var colorFrameSelector: Int = 0,
        var colorFogSelector: Int = 0,
        var colorTitle: Int = 0,
        var colorLabel: Int = 0
    )
}