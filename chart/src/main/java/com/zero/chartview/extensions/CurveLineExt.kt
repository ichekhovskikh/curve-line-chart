package com.zero.chartview.extensions

import android.graphics.PointF
import com.zero.chartview.model.CurveLine
import com.zero.chartview.model.FloatRange
import com.zero.chartview.model.MinMax

val List<CurveLine>.abscissas: List<Float>
    get() = flatMap { line -> line.points.map { it.x } }.distinct()

internal fun List<CurveLine>.getMinMaxY(range: FloatRange): MinMax {
    val points = flatMap { it.points }.toMutableList()
    val interpolatedRange = range.interpolatePointAbscissas(points)
    val (leftBoundary, rightBoundary) = points.getAbscissaBoundaries(interpolatedRange)
    leftBoundary?.let(points::add)
    rightBoundary?.let(points::add)
    val pointsIntoRange = points.filter {
        it.x in interpolatedRange.start..interpolatedRange.endInclusive
    }
    return MinMax(
        min = pointsIntoRange.minBy(PointF::y)?.y.orZero,
        max = pointsIntoRange.maxBy(PointF::y)?.y.orZero
    )
}
