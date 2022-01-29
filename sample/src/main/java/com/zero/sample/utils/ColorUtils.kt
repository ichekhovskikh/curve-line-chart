package com.zero.sample.utils

import com.zero.chartview.Themeable
import com.zero.sample.Stainable

fun convertThemeColorsToChartColors(themeColors: Stainable.ThemeColors): Themeable.ChartColors {
    val chartColors = Themeable.ChartColors()
    chartColors.colorBackground = themeColors.colorBackground
    chartColors.colorLegend = themeColors.colorLegend
    chartColors.colorYLegendLine = themeColors.colorGrid
    chartColors.colorPopupLine = themeColors.colorPopupLine
    chartColors.colorFrameSelector = themeColors.colorFrameSelector
    chartColors.colorFogSelector = themeColors.colorFogSelector
    return chartColors
}

fun convertChartColorsToThemeColors(chartColors: Themeable.ChartColors): Stainable.ThemeColors {
    val themeColors = Stainable.ThemeColors()
    themeColors.colorBackground = chartColors.colorBackground
    themeColors.colorLegend = chartColors.colorLegend
    themeColors.colorGrid = chartColors.colorYLegendLine
    themeColors.colorPopupLine = chartColors.colorPopupLine
    themeColors.colorFrameSelector = chartColors.colorFrameSelector
    themeColors.colorFogSelector = chartColors.colorFogSelector
    return themeColors
}
