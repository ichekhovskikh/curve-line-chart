package com.chekh.chartview.model

import android.graphics.PointF
import androidx.annotation.ColorInt

/**
 * This class represents data about the curve of the line: [name], [color], [points]
 */
data class CurveLine(
    val name: String,
    @ColorInt val color: Int,
    val points: List<PointF>
)
