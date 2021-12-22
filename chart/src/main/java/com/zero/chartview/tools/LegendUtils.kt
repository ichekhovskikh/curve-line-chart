package com.zero.chartview.tools

import com.zero.chartview.axis.formatter.ShortAxisFormatter

fun createCorrespondingLegends(coordinates: List<Float>): Map<Float, String> {
    val correspondingLegends = hashMapOf<Float, String>()
    val axisFormatter = ShortAxisFormatter()
    coordinates.forEach { coordinate ->
        if (!correspondingLegends.containsKey(coordinate)) {
            correspondingLegends[coordinate] = axisFormatter.format(coordinate, 1f)
        }
    }
    return correspondingLegends
}
