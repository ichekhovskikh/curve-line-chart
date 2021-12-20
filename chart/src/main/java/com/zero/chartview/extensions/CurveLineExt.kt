package com.zero.chartview.extensions

import android.graphics.PointF
import com.zero.chartview.model.CurveLine
import com.zero.chartview.model.FloatRange
import com.zero.chartview.model.MinMax

val List<CurveLine>.abscissas: List<Float>
    get() = flatMap { line -> line.points.map { it.x } }.distinct()

internal fun List<CurveLine>.getMinMaxY(range: FloatRange): MinMax {
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
        min = points.minBy(PointF::y)?.y.orZero,
        max = points.maxBy(PointF::y)?.y.orZero
    )
}
