package com.chekh.chartview.tools

import android.graphics.PointF
import androidx.annotation.Px

internal fun Float.pxToOrdinate(
    @Px windowHeight: Int,
    minY: Float,
    maxY: Float
): Float {
    val weight = windowHeight.toFloat() / (maxY - minY)
    return (windowHeight - this) / weight + minY
}

@Px
internal fun Float.ordinateToPx(
    @Px windowHeight: Int,
    minY: Float,
    maxY: Float
): Float {
    val weight = windowHeight.toFloat() / (maxY - minY)
    return windowHeight - ((this - minY) * weight)
}

internal fun Float.pxToAbscissa(
    @Px windowWidth: Int,
    minX: Float,
    maxX: Float
): Float {
    val weight = windowWidth.toFloat() / (maxX - minX)
    return this / weight + minX
}

@Px
internal fun Float.abscissaToPx(
    @Px windowWidth: Int,
    minX: Float,
    maxX: Float
): Float {
    val weight = windowWidth.toFloat() / (maxX - minX)
    return (this - minX) * weight
}

internal fun Float.getOrdinate(
    startPoint: PointF,
    endPoint: PointF
): Float {
    return startPoint.y + (this - startPoint.x) * (endPoint.y - startPoint.y) / (endPoint.x - startPoint.x)
}
