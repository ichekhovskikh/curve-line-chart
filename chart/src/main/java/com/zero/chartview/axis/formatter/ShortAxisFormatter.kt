package com.zero.chartview.axis.formatter

import java.text.DecimalFormat
import kotlin.math.abs

class ShortAxisFormatter : AxisFormatter {

    private val decimalFormat = DecimalFormat().apply {
        isDecimalSeparatorAlwaysShown = false
        applyPattern("#.#")
    }

    override fun format(value: Float) = when {
        abs(value) > 1000000000000f -> value.formatMM()
        abs(value) > 1000000f -> value.formatM()
        abs(value) > 1000f -> value.formatK()
        abs(value) < 0.000001f -> value.formatE6()
        abs(value) < 0.000000001f -> value.formatE9()
        else -> value.formatSimple()
    }

    private fun Float.formatSimple() = String.format("%.3f", this)
    private fun Float.formatMM() = String.format("%smm", decimalFormat.format(this / 1000000000000f))
    private fun Float.formatM() = String.format("%sm", decimalFormat.format(this / 1000000f))
    private fun Float.formatK() = String.format("%sk", decimalFormat.format(this / 1000f))
    private fun Float.formatE6() = String.format("%.3fe-6", this * 1000000f)
    private fun Float.formatE9() = String.format("%.3fe-9", this * 1000000000f)
}
