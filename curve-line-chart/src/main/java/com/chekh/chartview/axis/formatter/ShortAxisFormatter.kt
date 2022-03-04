package com.chekh.chartview.axis.formatter

import java.text.DecimalFormat
import kotlin.math.abs

/**
 * This formatter uses a shortened number format:
 * 1000000000000 -> 1mm
 * 1000000 -> 1m
 * 1000 -> 1k
 * 1 -> 1
 * 0.000001 -> 1e-6
 * 0.000000001 -> 1e-9
 */
class ShortAxisFormatter : AxisFormatter {

    private val decimalFormat = DecimalFormat().apply {
        isDecimalSeparatorAlwaysShown = false
        applyPattern("#.#")
    }

    override fun format(value: Float, zoom: Float) = when {
        abs(value) > MM -> value.formatMM()
        abs(value) > M -> value.formatM()
        abs(value) > K -> value.formatK()
        abs(value) < E6 -> value.formatE6()
        abs(value) < E9 -> value.formatE9()
        else -> value.formatSimple()
    }

    @Suppress("ImplicitDefaultLocale")
    private fun Float.formatSimple() = String.format("%.3f", this)

    @Suppress("ImplicitDefaultLocale")
    private fun Float.formatMM() = String.format("%smm", decimalFormat.format(this / MM))

    @Suppress("ImplicitDefaultLocale")
    private fun Float.formatM() = String.format("%sm", decimalFormat.format(this / M))

    @Suppress("ImplicitDefaultLocale")
    private fun Float.formatK() = String.format("%sk", decimalFormat.format(this / K))

    @Suppress("ImplicitDefaultLocale")
    private fun Float.formatE6() = String.format("%.3fe-6", this / E6)

    @Suppress("ImplicitDefaultLocale")
    private fun Float.formatE9() = String.format("%.3fe-9", this / E9)

    private companion object {
        const val MM = 1000000000000f
        const val M = 1000000f
        const val K = 1000f
        const val E6 = 0.000001f
        const val E9 = 0.000000001f
    }
}
