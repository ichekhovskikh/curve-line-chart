package com.zero.chartview

interface Themeable {

    fun getThemeColor(): ThemeColor

    fun setThemeColor(colors: ThemeColor)

    data class ThemeColor(
        var colorBackground: Int? = null,
        var colorLegend: Int? = null,
        var colorGrid: Int? = null,
        var colorPopupLine: Int? = null,
        var colorFrameControl: Int? = null,
        var colorFogControl: Int? = null,
        var colorLabel: Int? = null,
        var colorTitle: Int? = null
    )
}