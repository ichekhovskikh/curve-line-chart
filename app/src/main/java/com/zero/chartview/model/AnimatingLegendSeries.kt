package com.zero.chartview.model

internal data class AnimatingLegendSeries(
    var legends: List<Float>,
    val minY: Float,
    val maxY: Float,
    var isAppearing: Boolean,
    var animationValue: Float
)