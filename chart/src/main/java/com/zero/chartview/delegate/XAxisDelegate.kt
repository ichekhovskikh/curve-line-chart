package com.zero.chartview.delegate

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.zero.chartview.anim.AxisAnimator
import com.zero.chartview.axis.formatter.AxisFormatter
import com.zero.chartview.axis.formatter.DefaultAxisFormatter
import com.zero.chartview.extensions.alphaColor
import com.zero.chartview.extensions.distance
import com.zero.chartview.extensions.interpolateByValues
import com.zero.chartview.model.*
import com.zero.chartview.model.Size
import com.zero.chartview.model.XLegend
import com.zero.chartview.model.XLegend.Companion.VISIBLE
import com.zero.chartview.tools.xValueToPixel
import java.lang.Float.min
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log2
import kotlin.math.pow

internal class XAxisDelegate(
    internal val legendPaint: Paint,
    legendCount: Int,
    private val textMarginTop: Float,
    private val textMarginHorizontalPercent: Float,
    private val onUpdate: () -> Unit
) {

    private val legendPath = Path()
    private var viewSize = Size()
    private var abscissas: List<Float> = emptyList()
    private var legends: List<XLegend> = emptyList()

    internal var axisFormatter: AxisFormatter = DefaultAxisFormatter()

    internal var legendCount: Int = legendCount
        set(value) {
            if (field == value) return
            field = value
            onXLegendsChanged()
            onUpdate()
        }

    internal var range: FloatRange = FloatRange(0f, 1f)
        private set

    internal val legendTextHeightUsed
        get() = (legendPaint.textSize + textMarginTop).toInt()

    private val maxStep
        get() = (abscissas.last() - abscissas.first()) / legendCount.times(2)

    private val maxLegendWidth
        get() = (viewSize.width.toFloat() / legendCount) * (1 - textMarginHorizontalPercent)

    private val axisAnimator = AxisAnimator { start, end, _, _ ->
        range = FloatRange(start, end)
        onXLegendsChanged()
        onUpdate()
    }

    fun setAbscissas(abscissas: List<Float>) {
        this.abscissas = abscissas.sorted()
        onXLegendsChanged()
        onUpdate()
    }

    fun setRange(range: FloatRange, smoothScroll: Boolean) {
        if (smoothScroll) {
            axisAnimator.reStart(this.range, range)
        } else {
            axisAnimator.cancel()
            this.range = range
            onXLegendsChanged()
            onUpdate()
        }
    }

    fun onMeasure(viewSize: Size) {
        this.viewSize = viewSize
    }

    fun onLayout() {
        onXLegendsChanged()
    }

    private fun onXLegendsChanged() {
        if (abscissas.isEmpty() || range.distance == 0f) {
            legends = emptyList()
            return
        }
        val interpolatedRange = range.interpolateByValues(abscissas)
        val exponent = exponent(interpolatedRange)
        val step = calculateStep(exponent)
        val positions = calculateLegendPositions(step)

        legends = positions.mapIndexedNotNull { index, position ->
            if (position <= abscissas.first() + step.div(2)) return@mapIndexedNotNull null
            val maxLegendWidth = maxLegendWidth
            val maxLegendOffset = maxLegendWidth / 2
            val pixel = xValueToPixel(
                position,
                viewSize.width,
                interpolatedRange.start,
                interpolatedRange.endInclusive
            )
            if (pixel + maxLegendOffset < 0 || pixel - maxLegendOffset > viewSize.width) {
                return@mapIndexedNotNull null
            }
            val legendText = position.toLegendText() ?: return@mapIndexedNotNull null
            val textOffset = min(legendPaint.measureText(legendText), maxLegendWidth) / 2
            XLegend(
                label = legendText,
                left = pixel - textOffset,
                right = pixel + textOffset,
                vertical = viewSize.height.toFloat(),
                alpha = if (index % 2 != 0) alpha(exponent) else VISIBLE
            )
        }
    }

    fun drawLegends(canvas: Canvas) {
        canvas.drawXLegends(legendPaint)
    }

    private fun Canvas.drawXLegends(legendPaint: Paint) {
        legends.forEach { legend ->
            legendPaint.color = legend.alphaColor(legendPaint.color)
            legendPath.apply {
                reset()
                addRect(
                    legend.left,
                    legend.vertical,
                    legend.right,
                    legend.vertical,
                    Path.Direction.CW
                )
            }
            drawTextOnPath(legend.label, legendPath, 0f, 0f, legendPaint)
        }
    }

    private fun exponent(interpolatedRange: FloatRange) =
        log2((abscissas.last() - abscissas.first()) / interpolatedRange.distance)

    private fun calculateStep(exponent: Float) = maxStep / 2f.pow(floor(exponent))

    private fun calculateLegendPositions(step: Float): List<Float> {
        val positions = mutableListOf<Float>()
        val first = abscissas.first() - maxStep
        val last = abscissas.last() - step.div(2)
        var nextPosition = first
        var multiplier = 0

        while (nextPosition <= last) {
            positions.add(nextPosition)
            nextPosition = first + step * ++multiplier
        }
        return positions
    }

    private fun alpha(exponent: Float) = (255 * abs(floor(exponent) - exponent)).toInt()

    private fun Float.toLegendText() = axisFormatter
        .format(this, zoom = 1f / range.distance)
        .takeIf { it.isNotBlank() }
}
