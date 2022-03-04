package com.chekh.chartview.tools

import android.graphics.PointF
import androidx.annotation.Px

/**
 * Convert [this] pixel to ordinate value of the graph
 */
internal fun Float.pxToOrdinate(
    @Px windowHeight: Int,
    minY: Float,
    maxY: Float
): Float {
    val weight = windowHeight.toFloat() / (maxY - minY)
    return (windowHeight - this) / weight + minY
}

/**
 * Convert [this] ordinate value of the graph to pixel
 */
@Px
internal fun Float.ordinateToPx(
    @Px windowHeight: Int,
    minY: Float,
    maxY: Float
): Float {
    val weight = windowHeight.toFloat() / (maxY - minY)
    return windowHeight - ((this - minY) * weight)
}

/**
 * Convert [this] pixel to abscissa value of the graph
 */
internal fun Float.pxToAbscissa(
    @Px windowWidth: Int,
    minX: Float,
    maxX: Float
): Float {
    val weight = windowWidth.toFloat() / (maxX - minX)
    return this / weight + minX
}

/**
 * Convert [this] abscissa value of the graph to pixel
 */
@Px
internal fun Float.abscissaToPx(
    @Px windowWidth: Int,
    minX: Float,
    maxX: Float
): Float {
    val weight = windowWidth.toFloat() / (maxX - minX)
    return (this - minX) * weight
}

/**
 * Get interpolated ordinate by two points [startPoint], [endPoint]
 * of the line and [this] abscissa
 */
internal fun Float.getOrdinate(
    startPoint: PointF,
    endPoint: PointF
): Float {
    return startPoint.y + (this - startPoint.x) * (endPoint.y - startPoint.y) / (endPoint.x - startPoint.x)
}
