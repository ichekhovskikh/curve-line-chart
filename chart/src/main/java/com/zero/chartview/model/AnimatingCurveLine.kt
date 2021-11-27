package com.zero.chartview.model

internal data class AnimatingCurveLine(
    val curveLine: CurveLine,
    var isAppearing: Boolean,
    var animationValue: Float
)


internal object AppearingCurveLine {

    operator fun invoke(curveLine: CurveLine) = AnimatingCurveLine(
        curveLine = curveLine,
        isAppearing = true,
        animationValue = 0f
    )
}
