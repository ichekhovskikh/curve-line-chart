package com.zero.chartview.utils

import android.graphics.Paint
import android.graphics.PointF
import com.zero.chartview.model.CurveLine
import com.zero.chartview.model.FloatRange
import com.zero.chartview.popup.PopupLineView
import java.text.DecimalFormat

fun findMinMaxYValueRanged(lines: List<CurveLine>, range: FloatRange): Pair<Float, Float> {
    val points = mutableListOf<PointF>()
    lines.forEach { line -> points.addAll(line.points) }
    points.sortBy { it.x }
    val valueRange = convertPercentToValue(points.map { it.x }, range)
    val (leftBoundary, rightBoundary) = getBoundaryPoints(points, valueRange)
    leftBoundary?.let { points.add(it) }
    rightBoundary?.let { points.add(it) }
    val rangedPoints = points.filter { it.x in valueRange.start..valueRange.endInclusive }
    val minY = rangedPoints.minBy(PointF::y)?.y ?: 0f
    val maxY = rangedPoints.maxBy(PointF::y)?.y ?: 0f
    return minY to maxY
}

fun convertPercentToValue(lines: List<CurveLine>, range: FloatRange): Pair<Float, Float> {
    val points = mutableListOf<PointF>()
    lines.forEach { line -> points.addAll(line.points) }
    val valueRange = convertPercentToValue(points.map { it.x }, range)
    return valueRange.start to valueRange.endInclusive
}

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

fun getYValue(xValue: Float, startPoint: PointF, endPoint: PointF): Float {
    return startPoint.y + (xValue - startPoint.x) * (endPoint.y - startPoint.y) / (endPoint.x - startPoint.x)
}

fun getAbscissas(lines: List<CurveLine>): List<Float> {
    val abscissas = mutableListOf<Float>()
    lines.forEach { line ->
        abscissas.addAll(line.points.map { it.x })
    }
    return abscissas.distinct()
}

fun getBoundaryPoints(valuePoints: List<PointF>, valueRange: FloatRange): Pair<PointF?, PointF?> {
    var left: PointF? = null
    var right: PointF? = null
    for (index in 0 until valuePoints.size) {
        if (!isBoundaryPoint(index, valuePoints, valueRange)) continue
        else if (valuePoints[index].x < valueRange.start) {
            val yValue = getYValue(valueRange.start, valuePoints[index], valuePoints[index + 1])
            left = PointF(valueRange.start, yValue)
        } else if (valuePoints[index].x > valueRange.endInclusive) {
            val yValue = getYValue(valueRange.endInclusive, valuePoints[index - 1], valuePoints[index])
            right = PointF(valueRange.endInclusive, yValue)
        }
    }
    return left to right
}

private fun isBoundaryPoint(index: Int, valuePoints: List<PointF>, valueRange: FloatRange) =
    index < valuePoints.size - 1 && !valueRange.contains(valuePoints[index].x) && valueRange.contains(valuePoints[index + 1].x) ||
            index > 0 && !valueRange.contains(valuePoints[index].x) && valueRange.contains(valuePoints[index - 1].x)

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
    Math.abs(legend) > 1000000000000f -> formatMM(legend)
    Math.abs(legend) > 1000000f -> formatM(legend)
    Math.abs(legend) > 1000f -> formatK(legend)
    Math.abs(legend) < 0.000001f -> formatE6(legend)
    Math.abs(legend) < 0.000000001f -> formatE9(legend)
    else -> String.format("%.3f", legend)
}

private fun formatMM(value: Float) = String.format("%smm", decimalFormat.format(value / 1000000000000f))
private fun formatM(value: Float) = String.format("%sm", decimalFormat.format(value / 1000000f))
private fun formatK(value: Float) = String.format("%sk", decimalFormat.format(value / 1000f))
private fun formatE6(value: Float) = String.format("%.3f×10ˉ⁶", value * 1000000f)
private fun formatE9(value: Float) = String.format("%.3f×10ˉ⁹", value * 1000000000f)

val Paint.textHeight get() = fontMetrics.bottom - fontMetrics.top + fontMetrics.leading