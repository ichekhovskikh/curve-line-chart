package com.zero.chartview.extensions

import android.graphics.PointF
import com.zero.chartview.model.CurveLine
import com.zero.chartview.model.FloatRange

val FloatRange.distance get() = endInclusive - start

fun FloatRange.contains(value: Float) = value in start..endInclusive

fun FloatRange.isEmpty() = start == endInclusive

/**
 * Convert range of percents to range of values
 */
fun FloatRange.asValueRange(values: List<Float>): FloatRange {
    if (start < 0 || endInclusive > 1 || start > endInclusive) {
        throw IllegalArgumentException("It's not PercentRange")
    }
    val minValue = values.min().orZero
    val maxValue = values.max().orZero
    val length = maxValue - minValue
    return FloatRange(minValue + start * length, minValue + endInclusive * length)
}

/**
 * Convert range of percents to range of abscissas
 */
internal fun FloatRange.asLineAbscissaRange(lines: List<CurveLine>): FloatRange =
    asPointAbscissaRange(lines.flatMap { it.points })

/**
 * Convert range of percents to range of abscissas
 */
internal fun FloatRange.asPointAbscissaRange(points: List<PointF>): FloatRange =
    asValueRange(points.map { it.x })
