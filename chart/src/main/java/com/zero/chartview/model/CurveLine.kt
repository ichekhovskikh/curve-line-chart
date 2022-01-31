package com.zero.chartview.model

import android.graphics.PointF

data class CurveLine(
    val name: String,
    val color: Int,
    val points: List<PointF>
)
