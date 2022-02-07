package com.chekh.chartview.axis.formatter

class DefaultAxisFormatter : AxisFormatter {
    override fun format(value: Float, zoom: Float) = value.toString()
}
