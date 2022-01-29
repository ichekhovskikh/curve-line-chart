package com.zero.chartview

interface Themeable {

    fun getChartColors(): ChartColors

    fun setChartColors(colors: ChartColors)

    data class ChartColors(
        var colorBackground: Int = 0,
        var colorLegend: Int = 0,
        var colorYLegendLine: Int = 0,
        var colorPopupLine: Int = 0,
        var colorFrameSelector: Int = 0,
        var colorFogSelector: Int = 0
    )
}
