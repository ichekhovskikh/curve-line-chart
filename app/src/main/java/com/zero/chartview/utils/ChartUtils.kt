package com.zero.chartview.utils

import android.graphics.PointF
import com.zero.chartview.model.CurveLine

fun findMaxYValue(lines: List<CurveLine>): Float =
    lines.mapNotNull { it.points.maxBy(PointF::y) }.maxBy(PointF::y)?.y ?: 0F

fun findMinYValue(lines: List<CurveLine>): Float =
    lines.mapNotNull { it.points.minBy(PointF::y) }.minBy(PointF::y)?.y ?: 0F

fun findMaxXValue(lines: List<CurveLine>): Float =
    lines.mapNotNull { it.points.maxBy(PointF::x) }.maxBy(PointF::x)?.x ?: 0F

fun findMinXValue(lines: List<CurveLine>): Float =
    lines.mapNotNull { it.points.minBy(PointF::x) }.minBy(PointF::x)?.x ?: 0F

fun yPixelToValue(yPixel: Float, windowHeight: Int, minY: Float, maxY: Float): Float {
    val coefficient = windowHeight.toFloat() / (maxY - minY)
    return (windowHeight - yPixel) / coefficient + minY
}

fun yValueToPixel(yValue: Float, windowHeight: Int, minY: Float, maxY: Float): Float {
    val coefficient = windowHeight.toFloat() / (maxY - minY)
    return windowHeight - ((yValue - minY) * coefficient)
}

fun xPixelToValue(xPixel: Float, windowWidth: Int, minX: Float, maxX: Float): Float {
    val coefficient = windowWidth.toFloat() / (maxX - minX)
    return xPixel / coefficient + minX
}

fun xValueToPixel(xValue: Float, windowWidth: Int, minX: Float, maxX: Float): Float {
    val coefficient = windowWidth.toFloat() / (maxX - minX)
    return (xValue - minX) * coefficient
}