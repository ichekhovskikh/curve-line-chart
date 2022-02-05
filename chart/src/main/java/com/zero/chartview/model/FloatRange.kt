package com.zero.chartview.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FloatRange(val start: Float, val endInclusive: Float): Parcelable

object PercentRange {

    operator fun invoke(start: Float, endInclusive: Float) = FloatRange(
        start = start.coerceAtLeast(0f),
        endInclusive = endInclusive.coerceAtMost(1f)
    )
}

object ZeroRange {

    operator fun invoke() = FloatRange(0f, 0f)
}

object BinaryRange {

    operator fun invoke() = FloatRange(0f, 1f)
}
