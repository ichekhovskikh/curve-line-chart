package com.zero.chartview.tools

import java.text.DecimalFormat
import kotlin.math.abs

fun createCorrespondingLegends(coordinates: List<Float>): Map<Float, String> {
    val correspondingLegends = hashMapOf<Float, String>()
    coordinates.forEach { coordinate ->
        if (!correspondingLegends.containsKey(coordinate)) {
            correspondingLegends[coordinate] = formatLegend(coordinate)
        }
    }
    return correspondingLegends
}

private val decimalFormat = DecimalFormat().apply {
    isDecimalSeparatorAlwaysShown = false
    applyPattern("#.#")
}

fun formatLegend(legend: Float) = when {
    abs(legend) > 1000000000000f -> formatMM(legend)
    abs(legend) > 1000000f -> formatM(legend)
    abs(legend) > 1000f -> formatK(legend)
    abs(legend) < 0.000001f -> formatE6(legend)
    abs(legend) < 0.000000001f -> formatE9(legend)
    else -> String.format("%.3f", legend)
}

private fun formatMM(value: Float) =
    String.format("%smm", decimalFormat.format(value / 1000000000000f))

private fun formatM(value: Float) = String.format("%sm", decimalFormat.format(value / 1000000f))
private fun formatK(value: Float) = String.format("%sk", decimalFormat.format(value / 1000f))
private fun formatE6(value: Float) = String.format("%.3f×10ˉ⁶", value * 1000000f)
private fun formatE9(value: Float) = String.format("%.3f×10ˉ⁹", value * 1000000000f)
