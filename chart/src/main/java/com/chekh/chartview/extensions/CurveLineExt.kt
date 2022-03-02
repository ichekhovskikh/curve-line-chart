package com.chekh.chartview.extensions

import android.graphics.PointF
import com.chekh.chartview.model.IntersectionPoint
import com.chekh.chartview.model.CurveLine
import com.chekh.chartview.model.FloatRange
import com.chekh.chartview.model.MinMax
import kotlin.math.abs

/**
 * @return all the abscesses of the curve line
 */
val List<CurveLine>.abscissas: List<Float>
    get() = flatMap { line -> line.points.map { it.x } }.distinct()

/**
 * @return all the ordinates of the curve line
 */
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

internal fun List<CurveLine>.getIntersections(
    abscissa: Float,
    delta: Float
): List<IntersectionPoint> {
    val points = mutableListOf<IntersectionPoint>()
    var minDistance = delta
    var nearestX: Float? = null
    forEach { line ->
        line.points.forEach { point ->
            val distance = abs(abscissa - point.x)
            if (distance <= minDistance) {
                points.add(IntersectionPoint(line.name, line.color, point.x, point.y))
                nearestX = point.x
                minDistance = distance
            }
        }
    }
    return points.filter { it.x == nearestX }
}
