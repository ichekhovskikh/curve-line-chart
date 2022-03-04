package com.chekh.chartview.extensions

import android.graphics.PointF
import com.chekh.chartview.model.CurveLine
import com.chekh.chartview.model.FloatRange

/**
 * @return a distance between start and end
 */
val FloatRange.distance get() = endInclusive - start

/**
 * checks whether [value] is in the range
 */
fun FloatRange.contains(value: Float) = value in start..endInclusive

/**
 * checks that the range is empty
 */
fun FloatRange.isEmpty() = start == endInclusive

/**
 * checks that the range is not empty
 */
fun FloatRange.isNotEmpty() = !isEmpty()

/**
 * shifts the range by [offset]
 */
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
