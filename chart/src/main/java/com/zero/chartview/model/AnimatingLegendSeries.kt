package com.zero.chartview.model

internal data class AnimatingLegendSeries(
    val minY: Float,
    val maxY: Float,
    var legends: List<AnimatingLegend>,
    var isAppearing: Boolean,
    var animationValue: Float
)

internal data class AnimatingLegend(
    val position: Float,
    val label: String,
    var interpolatedPosition: Float
)

internal object AppearingLegendSeries {

    operator fun invoke(
        minY: Float,
        maxY: Float,
        legends: List<AnimatingLegend>
    ) = AnimatingLegendSeries(
        minY = minY,
        maxY = maxY,
        legends = legends,
        isAppearing = true,
        animationValue = 0f
    )
}
