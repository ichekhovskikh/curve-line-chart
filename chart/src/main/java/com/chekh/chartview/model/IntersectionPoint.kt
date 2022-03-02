package com.chekh.chartview.model

import androidx.annotation.ColorInt

/**
 * This class represents data about the point
 * of intersection with the graph: [lineName], [lineColor], [x], [y]
 */
data class IntersectionPoint(
    val lineName: String,
    @ColorInt val lineColor: Int,
    val x: Float,
    val y: Float
)
