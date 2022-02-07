package com.chekh.chartview.extensions

import android.graphics.PointF
import com.chekh.chartview.model.FloatRange
import com.chekh.chartview.model.LeftRight
import com.chekh.chartview.tools.getOrdinate

internal fun List<PointF>.getAbscissaBoundaries(interpolatedRange: FloatRange): LeftRight {
    var left: PointF? = null
    var right: PointF? = null
    val sortedPoints = sortedBy { it.x }
    sortedPoints.forEachIndexed { index, _ ->
        when {
            sortedPoints.isFirstLeftOverRangePoint(index, interpolatedRange) -> {
                val yValue = interpolatedRange.start.getOrdinate(
                    sortedPoints[index],
                    sortedPoints[index + 1]
                )
                left = PointF(interpolatedRange.start, yValue)
            }
            sortedPoints.isFirstRightOverRangePoint(index, interpolatedRange) -> {
                val yValue = interpolatedRange.endInclusive.getOrdinate(
                    sortedPoints[index - 1],
                    sortedPoints[index]
                )
                right = PointF(interpolatedRange.endInclusive, yValue)
            }
        }
    }
    return LeftRight(left, right)
}

private fun List<PointF>.isFirstLeftOverRangePoint(index: Int, abscissaRange: FloatRange) =
    index + 1 < size && get(index).x < abscissaRange.start && get(index + 1).x >= abscissaRange.start

private fun List<PointF>.isFirstRightOverRangePoint(index: Int, abscissaRange: FloatRange) =
    index > 0 && get(index).x > abscissaRange.endInclusive && get(index - 1).x <= abscissaRange.endInclusive
