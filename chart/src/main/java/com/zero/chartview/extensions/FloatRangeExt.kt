package com.zero.chartview.extensions

import android.graphics.PointF
import com.zero.chartview.model.CurveLine
import com.zero.chartview.model.FloatRange

val FloatRange.distance get() = endInclusive - start

fun FloatRange.contains(value: Float) = value in start..endInclusive

fun FloatRange.isEmpty() = start == endInclusive

fun FloatRange.offset(offset: Float) = FloatRange(start - offset, endInclusive - offset)

/**
 * Convert range of percents to range of values
 */
internal fun FloatRange.interpolateByValues(values: List<Float>): FloatRange {
    if (start < 0 || endInclusive > 1 || start > endInclusive) {
        throw IllegalArgumentException("It's not PercentRange")
    }
    val minValue = values.minOrNull().orZero
    val maxValue = values.maxOrNull().orZero
    val length = maxValue - minValue
    return FloatRange(minValue + start * length, minValue + endInclusive * length)
}

/**
 * Convert range of percents to range of abscissas
 */
internal fun FloatRange.interpolateByLineAbscissas(lines: List<CurveLine>): FloatRange =
    interpolateByPointAbscissas(lines.flatMap { it.points })

/**
 * Convert range of percents to range of abscissas
 */
internal fun FloatRange.interpolateByPointAbscissas(points: List<PointF>): FloatRange =
    interpolateByValues(points.map { it.x })
