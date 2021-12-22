package com.zero.chartview.axis.formatter

class DefaultAxisFormatter : AxisFormatter {
    override fun format(value: Float, zoom: Float) = value.toString()
}
