package com.zero.chartview.model

data class FloatRange(val start: Float, val endInclusive: Float)

object PercentRange {

    operator fun invoke(start: Float, endInclusive: Float) = FloatRange(
        start = start.coerceAtLeast(0f),
        endInclusive = endInclusive.coerceAtMost(1f)
    )
}

object ZeroRange {

    operator fun invoke() = FloatRange(0f, 0f)
}