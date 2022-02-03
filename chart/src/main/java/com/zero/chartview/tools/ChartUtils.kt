package com.zero.chartview.tools

import android.graphics.PointF
import androidx.annotation.Px

internal fun yPixelToValue(
    @Px yPixel: Float,
    @Px windowHeight: Int,
    minY: Float,
    maxY: Float
): Float {
    val weight = windowHeight.toFloat() / (maxY - minY)
    return (windowHeight - yPixel) / weight + minY
}

@Px
internal fun yValueToPixel(
    yValue: Float,
    @Px windowHeight: Int,
    minY: Float,
    maxY: Float
): Float {
    val weight = windowHeight.toFloat() / (maxY - minY)
    return windowHeight - ((yValue - minY) * weight)
}

internal fun xPixelToValue(
    @Px xPixel: Float,
    @Px windowWidth: Int,
    minX: Float,
    maxX: Float
): Float {
    val weight = windowWidth.toFloat() / (maxX - minX)
    return xPixel / weight + minX
}

@Px
internal fun xValueToPixel(
    xValue: Float,
    @Px windowWidth: Int,
    minX: Float,
    maxX: Float
): Float {
    val weight = windowWidth.toFloat() / (maxX - minX)
    return (xValue - minX) * weight
}

internal fun getYValue(
    @Px xValue: Float,
    startPoint: PointF,
    endPoint: PointF
): Float {
    return startPoint.y + (xValue - startPoint.x) * (endPoint.y - startPoint.y) / (endPoint.x - startPoint.x)
}
