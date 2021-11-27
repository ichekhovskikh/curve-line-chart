package com.zero.chartview.tools

import android.graphics.PointF

internal fun yPixelToValue(yPixel: Float, windowHeight: Int, minY: Float, maxY: Float): Float {
    val weight = windowHeight.toFloat() / (maxY - minY)
    return (windowHeight - yPixel) / weight + minY
}

internal fun yValueToPixel(yValue: Float, windowHeight: Int, minY: Float, maxY: Float): Float {
    val weight = windowHeight.toFloat() / (maxY - minY)
    return windowHeight - ((yValue - minY) * weight)
}

internal fun xPixelToValue(xPixel: Float, windowWidth: Int, minX: Float, maxX: Float): Float {
    val weight = windowWidth.toFloat() / (maxX - minX)
    return xPixel / weight + minX
}

internal fun xValueToPixel(xValue: Float, windowWidth: Int, minX: Float, maxX: Float): Float {
    val weight = windowWidth.toFloat() / (maxX - minX)
    return (xValue - minX) * weight
}

internal fun getYValue(xValue: Float, startPoint: PointF, endPoint: PointF): Float {
    return startPoint.y + (xValue - startPoint.x) * (endPoint.y - startPoint.y) / (endPoint.x - startPoint.x)
}
