package com.zero.chartview.model

data class FloatRange(var start: Float, var endInclusive: Float) {
    fun distance() = endInclusive - start
}