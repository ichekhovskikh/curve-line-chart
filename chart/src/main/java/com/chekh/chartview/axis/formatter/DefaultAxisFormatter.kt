package com.chekh.chartview.axis.formatter

/**
 * There is default axis formatter.
 * Converts a value to a string without changes
 */
class DefaultAxisFormatter : AxisFormatter {
    override fun format(value: Float, zoom: Float) = value.toString()
}
