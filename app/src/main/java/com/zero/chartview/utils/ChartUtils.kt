package com.zero.chartview.utils

import android.graphics.PointF
import com.zero.chartview.model.CurveLine
import com.zero.chartview.model.FloatRange
import java.text.DecimalFormat

fun findMaxYValue(lines: List<CurveLine>): Float =
    lines.mapNotNull { it.points.maxBy(PointF::y) }.maxBy(PointF::y)?.y ?: 0F

fun findMinYValue(lines: List<CurveLine>): Float =
    lines.mapNotNull { it.points.minBy(PointF::y) }.minBy(PointF::y)?.y ?: 0F

fun findMaxXValue(lines: List<CurveLine>): Float =
    lines.mapNotNull { it.points.maxBy(PointF::x) }.maxBy(PointF::x)?.x ?: 0F

fun findMinXValue(lines: List<CurveLine>): Float =
    lines.mapNotNull { it.points.minBy(PointF::x) }.minBy(PointF::x)?.x ?: 0F

fun convertPercentToValue(abscissas: List<Float>, range: FloatRange): FloatRange {
    val minValue = abscissas.min()!!
    val maxValue = abscissas.max()!!
    val length = maxValue - minValue
    return FloatRange(minValue + range.start * length, minValue + range.endInclusive * length)
}

fun yPixelToValue(yPixel: Float, windowHeight: Int, minY: Float, maxY: Float): Float {
    val weight = windowHeight.toFloat() / (maxY - minY)
    return (windowHeight - yPixel) / weight + minY
}

fun yValueToPixel(yValue: Float, windowHeight: Int, minY: Float, maxY: Float): Float {
    val weight = windowHeight.toFloat() / (maxY - minY)
    return windowHeight - ((yValue - minY) * weight)
}

fun xPixelToValue(xPixel: Float, windowWidth: Int, minX: Float, maxX: Float): Float {
    val weight = windowWidth.toFloat() / (maxX - minX)
    return xPixel / weight + minX
}

fun xValueToPixel(xValue: Float, windowWidth: Int, minX: Float, maxX: Float): Float {
    val weight = windowWidth.toFloat() / (maxX - minX)
    return (xValue - minX) * weight
}

fun getAbscissas(lines: List<CurveLine>): List<Float> {
    val abscissas = mutableListOf<Float>()
    lines.forEach { line ->
        abscissas.addAll(line.points.map { it.x })
    }
    return abscissas.distinct()
}

fun createCorrespondingLegends(coordinates: List<Float>): Map<Float, String> {
    val correspondingLegends = hashMapOf<Float, String>()
    coordinates.forEach { coordinate ->
        if (!correspondingLegends.containsKey(coordinate)) {
            correspondingLegends[coordinate] = coordinate.toString()
        }
    }
    return correspondingLegends
}

private val decimalFormat = DecimalFormat().apply {
    isDecimalSeparatorAlwaysShown = false
    applyPattern("#.#")
}

fun formatLegend(legend: Float) = when {
    legend > 1000000000000f -> formatMM(legend)
    legend > 1000000f -> formatM(legend)
    legend > 1000f -> formatK(legend)
    legend < 0.000001f -> formatE6(legend)
    legend < 0.000000001f -> formatE9(legend)
    else -> String.format("%.3f", legend)
}

private fun formatMM(value: Float) = String.format("%smm", decimalFormat.format(value / 1000000000000f))
private fun formatM(value: Float) = String.format("%sm", decimalFormat.format(value / 1000000f))
private fun formatK(value: Float) = String.format("%sk", decimalFormat.format(value / 1000f))
private fun formatE6(value: Float) = String.format("%s×10⁶", decimalFormat.format(value * 1000000f))
private fun formatE9(value: Float) = String.format("%s×10⁹", decimalFormat.format(value * 1000000000f))