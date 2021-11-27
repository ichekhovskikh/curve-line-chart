package com.zero.chartview.extensions

import android.graphics.PointF
import com.zero.chartview.model.CurveLine
import com.zero.chartview.model.FloatRange
import com.zero.chartview.model.MinMax

val List<CurveLine>.abscissas: List<Float>
    get() = flatMap { line -> line.points.map { it.x } }.distinct()

internal fun List<CurveLine>.getMinMaxY(range: FloatRange): MinMax {
    val points = flatMap { it.points }.toMutableList()
    val valueRange = range.asPointAbscissaRange(points)
    val (leftBoundary, rightBoundary) = points.getAbscissaBoundaries(valueRange)
    leftBoundary?.let(points::add)
    rightBoundary?.let(points::add)
    val pointsIntoRange = points.filter {
        it.x in valueRange.start..valueRange.endInclusive
    }
    return MinMax(
        min = pointsIntoRange.minBy(PointF::y)?.y.orZero,
        max = pointsIntoRange.maxBy(PointF::y)?.y.orZero
    )
}
