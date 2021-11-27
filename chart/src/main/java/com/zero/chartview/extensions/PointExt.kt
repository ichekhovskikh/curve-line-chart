package com.zero.chartview.extensions

import android.graphics.PointF
import com.zero.chartview.model.FloatRange
import com.zero.chartview.model.LeftRight
import com.zero.chartview.tools.getYValue

internal fun List<PointF>.getAbscissaBoundaries(valueRange: FloatRange): LeftRight {
    var left: PointF? = null
    var right: PointF? = null
    val sortedPoints = sortedBy { it.x }
    sortedPoints.forEachIndexed { index, point ->
        when {
            sortedPoints.isFirstLeftOverRangePoint(index, valueRange) -> {
                val yValue = getYValue(
                    valueRange.start,
                    sortedPoints[index],
                    sortedPoints[index + 1]
                )
                left = PointF(valueRange.start, yValue)
            }
            sortedPoints.isFirstRightOverRangePoint(index, valueRange) -> {
                val yValue = getYValue(
                    valueRange.endInclusive,
                    sortedPoints[index - 1],
                    sortedPoints[index]
                )
                right = PointF(valueRange.endInclusive, yValue)
            }
        }
    }
    return LeftRight(left, right)
}

private fun List<PointF>.isFirstLeftOverRangePoint(index: Int, abscissaRange: FloatRange) =
    index + 1 < size && !abscissaRange.contains(get(index).x) && abscissaRange.contains(get(index + 1).x)

private fun List<PointF>.isFirstRightOverRangePoint(index: Int, abscissaRange: FloatRange) =
    index > 0 && !abscissaRange.contains(get(index).x) && abscissaRange.contains(get(index - 1).x)