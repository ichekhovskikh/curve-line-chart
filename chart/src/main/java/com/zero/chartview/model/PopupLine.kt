package com.zero.chartview.model

internal data class PopupLine(
    val xDrawPixel: Float,
    val intersections: List<Intersection>
) {

    data class Intersection(
        val point: IntersectionPoint,
        val xDrawPixel: Float,
        val yDrawPixel: Float
    )
}

