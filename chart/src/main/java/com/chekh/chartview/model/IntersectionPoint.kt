package com.chekh.chartview.model

import androidx.annotation.ColorInt

data class IntersectionPoint(
    val lineName: String,
    @ColorInt val lineColor: Int,
    val x: Float,
    val y: Float
)
