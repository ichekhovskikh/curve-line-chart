package com.zero.chartview.model

internal data class AnimatingYLegendSeries(
    val minY: Float,
    val maxY: Float,
    var legends: List<AnimatingYLegend>,
    var isAppearing: Boolean,
    var animationValue: Float
)

internal data class AnimatingYLegend(
    val position: Float,
    val label: String,
    var interpolatedPosition: Float
)

internal object AppearingYLegendSeries {

    operator fun invoke(
        minY: Float,
        maxY: Float,
        legends: List<AnimatingYLegend>
    ) = AnimatingYLegendSeries(
        minY = minY,
        maxY = maxY,
        legends = legends,
        isAppearing = true,
        animationValue = 0f
    )
}
