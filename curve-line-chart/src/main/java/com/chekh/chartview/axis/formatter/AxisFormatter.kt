package com.chekh.chartview.axis.formatter

/**
 * This formatter is intended for formatting axis values
 */
interface AxisFormatter {

    /**
     * Formats the [value] into a string
     * @param value the axis value
     * @param zoom the axis zoom
     * @return formatted axis value
     */
    fun format(value: Float, zoom: Float): String
}
