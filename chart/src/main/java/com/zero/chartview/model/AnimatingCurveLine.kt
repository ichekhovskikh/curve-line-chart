package com.zero.chartview.model

import android.graphics.PointF

internal data class AnimatingCurveLine(
    val curveLine: CurveLine,
    var isAppearing: Boolean,
    var animationValue: Float,
    var interpolatedPoints: List<PointF> = curveLine.points
)


internal object AppearingCurveLine {

    operator fun invoke(curveLine: CurveLine) = AnimatingCurveLine(
        curveLine = curveLine,
        isAppearing = true,
        animationValue = 0f
    )
}
