package com.zero.chartview

interface Themeable {

    fun getThemeColor(): ThemeColor

    fun setThemeColor(colors: ThemeColor)

    data class ThemeColor(
        var colorBackground: Int = 0,
        var colorLegend: Int = 0,
        var colorGrid: Int = 0,
        var colorPopupLine: Int = 0,
        var colorFrameControl: Int = 0,
        var colorFogControl: Int = 0
    )
}