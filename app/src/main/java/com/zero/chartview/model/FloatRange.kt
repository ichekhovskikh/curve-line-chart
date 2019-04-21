package com.zero.chartview.model

data class FloatRange(var start: Float, var endInclusive: Float) {
    fun contains(value: Float) = value in start..endInclusive
    fun isEmpty() = start == 0F && endInclusive == 0F
}