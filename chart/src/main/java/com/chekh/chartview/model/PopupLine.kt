package com.chekh.chartview.model

import androidx.annotation.Px

internal data class PopupLine(
    @Px val xDrawPixel: Float,
    val intersections: List<Intersection>
) {

    data class Intersection(
        val point: IntersectionPoint,
        @Px val xDrawPixel: Float,
        @Px val yDrawPixel: Float
    )
}

