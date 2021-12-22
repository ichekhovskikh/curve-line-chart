package com.zero.chartview.axis.formatter

interface AxisFormatter {

    /**
     * @param value the axis value
     * @param zoom the axis zoom
     * @return formatted axis value
     */
    fun format(value: Float, zoom: Float): String
}
