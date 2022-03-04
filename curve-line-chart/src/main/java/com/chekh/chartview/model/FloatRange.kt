package com.chekh.chartview.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * The class is a range of numbers from [start] to [endInclusive] inclusive
 */
@Parcelize
data class FloatRange(val start: Float, val endInclusive: Float): Parcelable

object PercentRange {

    /**
     * Creates a range of numbers from the maximum of the of the [start] or 0
     * to the minimum of the [endInclusive] or 1 inclusive
     */
    operator fun invoke(start: Float, endInclusive: Float) = FloatRange(
        start = start.coerceAtLeast(0f),
        endInclusive = endInclusive.coerceAtMost(1f)
    )
}

object ZeroRange {

    /**
     * Creates a range of numbers with one zero
     */
    operator fun invoke() = FloatRange(0f, 0f)
}

object BinaryRange {

    /**
     * Creates a range of numbers from 0 to 1 inclusive
     */
    operator fun invoke() = FloatRange(0f, 1f)
}
