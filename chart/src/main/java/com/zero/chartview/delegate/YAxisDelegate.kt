package com.zero.chartview.delegate

import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.Px
import com.zero.chartview.anim.TensionAnimator
import com.zero.chartview.axis.formatter.AxisFormatter
import com.zero.chartview.axis.formatter.DefaultAxisFormatter
import com.zero.chartview.extensions.*
import com.zero.chartview.extensions.animatingColor
import com.zero.chartview.extensions.isEqualsOrNull
import com.zero.chartview.extensions.orZero
import com.zero.chartview.extensions.setDisappearing
import com.zero.chartview.model.AnimatingYLegend
import com.zero.chartview.model.AnimatingYLegendSeries
import com.zero.chartview.model.AppearingYLegendSeries
import com.zero.chartview.model.AxisLine
import com.zero.chartview.model.Size
import com.zero.chartview.tools.yPixelToValue
import com.zero.chartview.tools.yValueToPixel
import kotlin.math.max

internal class YAxisDelegate(
    internal val legendPaint: Paint,
    legendCount: Int,
    isLegendLinesAvailable: Boolean,
    @Px private val legendMarginStart: Float,
    @Px private val legendMarginBottom: Float,
    private val onUpdate: () -> Unit
) {

    private var minY = 0f
    private var maxY = 0f
    private var yAxisDistance = 0f
    private var viewSize = Size()
    private var legendPositions = emptyList<Float>()
    private var series = mutableListOf<AnimatingYLegendSeries>()

    internal var axisFormatter: AxisFormatter = DefaultAxisFormatter()

    internal var legendCount: Int = legendCount
        private set

    internal var isLegendLinesAvailable: Boolean = isLegendLinesAvailable
        private set

    private var onYAxisLinesChangedListener: ((yAxisLines: List<AxisLine>) -> Unit)? = null

    private val tensionAnimator = TensionAnimator { tension, minY, maxY ->
        this.minY = minY
        this.maxY = maxY

        series.forEach { series ->
            series.animationValue = if (series.isAppearing) {
                val appearingTension = tension * APPEARING_TENSION_SCALE + APPEARING_TENSION_OFFSET
                max(series.animationValue, appearingTension)
            } else {
                tension
            }
            series.legends.forEach { legend ->
                legend.yDrawPixel = yValueToPixel(
                    legend.position,
                    viewSize.height,
                    minY,
                    maxY
                )
            }
        }
        val yAxisLines = series.takeIf { this.isLegendLinesAvailable }?.toAxisLines().orEmpty()
        onYAxisLinesChangedListener?.invoke(yAxisLines)
        onUpdate()
    }.doOnEnd(::removeDisappearingLegendSeries)

    private fun removeDisappearingLegendSeries() {
        series.removeAll { !it.isAppearing }
    }

    fun setLegendCount(legendCount: Int) {
        if (this.legendCount == legendCount) return
        this.legendCount = legendCount
        onLegendPositionsChanged()
        series.find { it.isAppearing }?.let { setYAxis(it.minY, it.maxY) }
    }

    fun setLegendLinesAvailable(isAvailable: Boolean) {
        if (isLegendLinesAvailable == isAvailable) return
        isLegendLinesAvailable = isAvailable

        val yAxisLines = series.takeIf { isAvailable }?.toAxisLines().orEmpty()
        onYAxisLinesChangedListener?.invoke(yAxisLines)
    }

    fun setOrdinates(ordinates: List<Float>) {
        yAxisDistance = ordinates.maxOrNull().orZero - ordinates.minOrNull().orZero
    }

    fun setYAxis(minY: Float, maxY: Float, smoothScroll: Boolean = true) {
        val hasSeries = series.any {
            it.isAppearing && it.legends.size == legendCount &&
                    it.maxY == maxY && it.minY == minY &&
                    (smoothScroll || it.animationValue == 1f)
        }
        if (hasSeries) return

        if (smoothScroll) {
            series.forEach { it.setDisappearing() }
            series.add(
                AppearingYLegendSeries(
                    minY = minY,
                    maxY = maxY,
                    legends = legendPositions.toLegends(
                        localMinY = this.minY,
                        localMaxY = this.maxY,
                        absoluteMinY = minY,
                        absoluteMaxY = maxY
                    )
                )
            )
            tensionAnimator.reStart(
                fromTension = FROM_TENSION,
                toTension = TO_TENSION,
                fromMin = this.minY,
                toMin = minY,
                fromMax = this.maxY,
                toMax = maxY
            )
        } else {
            tensionAnimator.cancel()
            this.minY = minY
            this.maxY = maxY
            series.clear()
            series.add(
                AnimatingYLegendSeries(
                    minY = minY,
                    maxY = maxY,
                    isAppearing = true,
                    animationValue = 1f,
                    legends = legendPositions.toLegends(
                        localMinY = minY,
                        localMaxY = maxY,
                        absoluteMinY = minY,
                        absoluteMaxY = maxY
                    )
                )
            )
            val yAxisLines = series.takeIf { isLegendLinesAvailable }?.toAxisLines().orEmpty()
            onYAxisLinesChangedListener?.invoke(yAxisLines)
            onUpdate()
        }
    }

    internal fun setOnYAxisLinesChangedListener(onYAxisLinesChangedListener: ((yAxisLines: List<AxisLine>) -> Unit)?) {
        this.onYAxisLinesChangedListener = onYAxisLinesChangedListener
    }

    fun onMeasure(viewSize: Size) {
        this.viewSize = viewSize
        onLegendPositionsChanged()
        series.setLegendPositions(legendPositions)
    }

    private fun onLegendPositionsChanged() {
        val availableHeight = viewSize.height
        val legendHeight = legendPaint.textSize + legendMarginBottom
        val legendHeightWithMargin = (availableHeight - legendHeight) / (legendCount - 1)
        legendPositions = (0 until legendCount).map { index ->
            legendHeightWithMargin * index
        }
    }

    fun onRestoreInstanceState(
        legendCount: Int?,
        isLegendLinesAvailable: Boolean?,
        textColor: Int?,
        textSize: Float?
    ) {
        if (this.legendCount == legendCount &&
            this.isLegendLinesAvailable == isLegendLinesAvailable &&
            legendPaint.color == textColor &&
            legendPaint.strokeWidth == textSize
        ) return

        textColor?.let(legendPaint::setColor)
        textSize?.let(legendPaint::setStrokeWidth)
        if (legendCount.isEqualsOrNull(this.legendCount) &&
            isLegendLinesAvailable.isEqualsOrNull(this.isLegendLinesAvailable)
        ) {
            onUpdate()
        } else {
            legendCount?.let { this.legendCount = it }
            isLegendLinesAvailable?.let { this.isLegendLinesAvailable = it }
            onLegendPositionsChanged()
            setYAxis(minY, maxY, smoothScroll = false)
        }
    }

    fun drawLegends(canvas: Canvas) {
        canvas.drawYLegends(legendPaint)
    }

    private fun Canvas.drawYLegends(legendPaint: Paint) {
        series.forEach { series ->
            legendPaint.color = series.animatingColor(legendPaint.color)
            series.legends.forEach { legend ->
                drawText(
                    legend.label,
                    legendMarginStart,
                    legend.yDrawPixel - legendMarginBottom,
                    legendPaint
                )
            }
        }
    }

    private fun List<AnimatingYLegendSeries>.setLegendPositions(
        legendPositions: List<Float>
    ) = forEach {
        it.legends = legendPositions.toLegends(absoluteMinY = it.minY, absoluteMaxY = it.maxY)
    }

    private fun List<Float>.toLegends(
        localMinY: Float = minY,
        localMaxY: Float = maxY,
        absoluteMinY: Float = minY,
        absoluteMaxY: Float = maxY
    ) = map {
        val absoluteDistance = absoluteMaxY - absoluteMinY
        val zoom = if (yAxisDistance == 0f || absoluteDistance == 0f) 1f else yAxisDistance / absoluteDistance
        val absolutePosition = yPixelToValue(it, viewSize.height, absoluteMinY, absoluteMaxY)
        AnimatingYLegend(
            position = absolutePosition,
            label = axisFormatter.format(absolutePosition, zoom),
            yDrawPixel = yValueToPixel(
                absolutePosition,
                viewSize.height,
                localMinY,
                localMaxY
            )
        )
    }

    private companion object {
        const val APPEARING_TENSION_SCALE = 0.2f
        const val APPEARING_TENSION_OFFSET = 0.8f
        const val FROM_TENSION = 0f
        const val TO_TENSION = 1f
    }
}
