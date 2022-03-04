package com.chekh.chartview.delegate

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.annotation.Px
import com.chekh.chartview.anim.AxisAnimator
import com.chekh.chartview.axis.formatter.AxisFormatter
import com.chekh.chartview.axis.formatter.DefaultAxisFormatter
import com.chekh.chartview.extensions.toAxisLines
import com.chekh.chartview.extensions.distance
import com.chekh.chartview.extensions.alphaColor
import com.chekh.chartview.extensions.interpolateByValues
import com.chekh.chartview.extensions.isEqualsOrNull
import com.chekh.chartview.model.BinaryRange
import com.chekh.chartview.model.FloatRange
import com.chekh.chartview.model.AxisLine
import com.chekh.chartview.model.Size
import com.chekh.chartview.model.XLegend
import com.chekh.chartview.model.XLegend.Companion.VISIBLE
import com.chekh.chartview.tools.abscissaToPx
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.min

@Suppress("TooManyFunctions")
internal class XAxisDelegate(
    internal val legendPaint: Paint,
    legendCount: Int,
    isLegendLinesAvailable: Boolean,
    @Px private val textMarginTop: Float,
    private val textMarginHorizontalPercent: Float,
    private val onUpdate: () -> Unit
) {

    private val legendPath = Path()
    private var viewSize = Size()
    private var abscissas = emptyList<Float>()
    private var legends = emptyList<XLegend>()

    internal var axisFormatter: AxisFormatter = DefaultAxisFormatter()

    internal var legendCount: Int = legendCount
        private set

    internal var isLegendLinesAvailable: Boolean = isLegendLinesAvailable
        private set

    internal var range = BinaryRange()
        private set

    @get:Px
    internal val legendTextHeightUsed
        get() = (legendPaint.textSize + textMarginTop).toInt()

    private val maxStep
        get() = (abscissas.last() - abscissas.first()) / legendCount.times(2)

    @get:Px
    private val maxLegendWidth
        get() = (viewSize.width.toFloat() / legendCount) * (1 - textMarginHorizontalPercent)

    private var onXAxisLinesChangedListener: ((xAxisLines: List<AxisLine>) -> Unit)? = null

    private val axisAnimator = AxisAnimator { start, end, _, _ ->
        range = FloatRange(start, end)
        onXLegendsChanged()
        onUpdate()
    }

    fun setLegendCount(legendCount: Int) {
        if (this.legendCount == legendCount) return
        this.legendCount = legendCount
        onXLegendsChanged()
        onUpdate()
    }

    fun setLegendLinesAvailable(isAvailable: Boolean) {
        if (isLegendLinesAvailable == isAvailable) return
        isLegendLinesAvailable = isAvailable

        val xAxisLines = legends.takeIf { isAvailable }?.toAxisLines().orEmpty()
        onXAxisLinesChangedListener?.invoke(xAxisLines)
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

    internal fun setOnXAxisLinesChangedListener(onXAxisLinesChangedListener: ((xAxisLines: List<AxisLine>) -> Unit)?) {
        this.onXAxisLinesChangedListener = onXAxisLinesChangedListener
    }

    fun onMeasure(viewSize: Size) {
        this.viewSize = viewSize
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
            val pixel = position.abscissaToPx(
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

        val xAxisLines = legends.takeIf { isLegendLinesAvailable }?.toAxisLines().orEmpty()
        onXAxisLinesChangedListener?.invoke(xAxisLines)
    }

    fun onRestoreInstanceState(
        range: FloatRange?,
        legendCount: Int?,
        isLegendLinesAvailable: Boolean?,
        textColor: Int?,
        textSize: Float?
    ) {
        @Suppress("ComplexCondition")
        if (this.range == range &&
            this.legendCount == legendCount &&
            this.isLegendLinesAvailable == isLegendLinesAvailable &&
            legendPaint.color == textColor &&
            legendPaint.strokeWidth == textSize
        ) return

        textColor?.let(legendPaint::setColor)
        textSize?.let(legendPaint::setStrokeWidth)
        if (legendCount.isEqualsOrNull(this.legendCount) &&
            range.isEqualsOrNull(this.range) &&
            isLegendLinesAvailable.isEqualsOrNull(this.isLegendLinesAvailable)
        ) {
            onUpdate()
        } else {
            legendCount?.let { this.legendCount = it }
            isLegendLinesAvailable?.let { this.isLegendLinesAvailable = it }
            range?.let { this.range = it }
            onXLegendsChanged()
            onUpdate()
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

    @Suppress("MagicNumber")
    private fun alpha(exponent: Float) = (255 * abs(floor(exponent) - exponent)).toInt()

    private fun Float.toLegendText() = axisFormatter
        .format(this, zoom = 1f / range.distance)
        .takeIf { it.isNotBlank() }
}
