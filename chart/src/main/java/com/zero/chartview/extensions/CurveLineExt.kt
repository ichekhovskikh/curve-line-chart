package com.zero.chartview.extensions

import android.graphics.PointF
import com.zero.chartview.model.CurveLine
import com.zero.chartview.model.FloatRange
import com.zero.chartview.model.MinMax

val List<CurveLine>.abscissas: List<Float>
    get() = flatMap { line -> line.points.map { it.x } }.distinct()

val List<CurveLine>.ordinates: List<Float>
    get() = flatMap { line -> line.points.map { it.y } }.distinct()

internal fun List<CurveLine>.getMinMaxY(range: FloatRange): MinMax {
    if (isEmpty()) return MinMax(0f, 0f)
    val interpolatedRange = range.interpolateByLineAbscissas(this)
    val points = flatMap { it.points }
        .filter { it.x in interpolatedRange.start..interpolatedRange.endInclusive }
        .toMutableList()

    forEach { line ->
        val (leftBoundary, rightBoundary) = line.points.getAbscissaBoundaries(interpolatedRange)
        leftBoundary?.let(points::add)
        rightBoundary?.let(points::add)
    }
    return MinMax(
        min = points.minOf(PointF::y),
        max = points.maxOf(PointF::y)
    )
}
