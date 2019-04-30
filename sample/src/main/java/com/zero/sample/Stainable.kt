package com.zero.sample

interface Stainable {

    var currentColors: ThemeColor

    fun updateColors(colors: ThemeColor)

    data class ThemeColor(
        var colorBackground: Int = 0,
        var colorLegend: Int = 0,
        var colorGrid: Int = 0,
        var colorPopupLine: Int = 0,
        var colorFrameControl: Int = 0,
        var colorFogControl: Int = 0,
        var colorTitle: Int = 0,
        var colorLabel: Int = 0
    )
}