package com.zero.chartview.model

import android.graphics.PointF
import androidx.annotation.ColorInt

data class CurveLine(
    val name: String,
    @ColorInt val color: Int,
    val points: List<PointF>
)
