package com.zero.chartview.model

internal data class AnimatingLegendSeries(
    var legends: List<Float>,
    var minY: Float,
    var maxY: Float,
    var isAppearing: Boolean,
    var animationValue: Float
)