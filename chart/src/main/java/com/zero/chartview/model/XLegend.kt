package com.zero.chartview.model

internal data class XLegend(
    val label: String,
    val left: Float,
    val right: Float,
    val vertical: Float,
    val alpha: Int
) {
    companion object {
        const val VISIBLE = 255
    }
}
