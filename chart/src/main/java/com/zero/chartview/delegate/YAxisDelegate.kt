package com.zero.chartview.delegate

import android.graphics.Canvas
import android.graphics.Paint
import com.zero.chartview.anim.TensionAnimator
import com.zero.chartview.axis.formatter.AxisFormatter
import com.zero.chartview.axis.formatter.DefaultAxisFormatter
import com.zero.chartview.extensions.animatingColor
import com.zero.chartview.extensions.orZero
import com.zero.chartview.extensions.setDisappearing
import com.zero.chartview.model.AnimatingYLegend
import com.zero.chartview.model.AnimatingYLegendSeries
import com.zero.chartview.model.AppearingYLegendSeries
import com.zero.chartview.model.Size
import com.zero.chartview.tools.yPixelToValue
import com.zero.chartview.tools.yValueToPixel
import kotlin.math.max

internal class YAxisDelegate(
    internal val legendPaint: Paint,
    internal val linePaint: Paint,
    legendCount: Int,
    private val legendMarginStart: Float,
    private val legendMarginBottom: Float,
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
        set(value) {
            if (field == value) return
            field = value
            onLegendPositionsChanged()
            series.find { it.isAppearing }?.let { setYAxis(it.minY, it.maxY) }
        }

    private val tensionAnimator = TensionAnimator { tension, minY, maxY ->
        series.forEach { series ->
            this.minY = minY
            this.maxY = maxY

            series.animationValue = if (series.isAppearing) {
                val appearingTension = tension * APPEARING_TENSION_SCALE + APPEARING_TENSION_OFFSET
                max(series.animationValue, appearingTension)
            } else {
                tension
            }
            series.legends.forEach { legend ->
                legend.interpolatedPosition = yValueToPixel(
                    legend.position,
                    viewSize.height,
                    minY,
                    maxY
                )
            }
        }
        onUpdate()
    }.doOnEnd(::removeDisappearingLegendSeries)

    private fun removeDisappearingLegendSeries() {
        series.removeAll { !it.isAppearing }
    }

    fun setOrdinates(ordinates: List<Float>) {
        yAxisDistance = ordinates.maxOrNull().orZero - ordinates.minOrNull().orZero
    }

    fun setYAxis(minY: Float, maxY: Float) {
        val hasSeries = series.any {
            it.isAppearing && it.legends.size == legendCount && it.maxY == maxY && it.minY == minY
        }
        if (hasSeries) return

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
    }

    fun onMeasure(viewSize: Size) {
        this.viewSize = viewSize
    }

    fun onLayout() {
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

    fun drawLegends(canvas: Canvas) {
        canvas.drawYLegends(legendPaint, linePaint)
    }

    fun Canvas.drawYLegends(legendPaint: Paint, linePaint: Paint) {
        series.forEach { series ->
            linePaint.color = series.animatingColor(linePaint.color)
            legendPaint.color = series.animatingColor(legendPaint.color)
            series.legends.forEach { legend ->
                val yPixel = legend.interpolatedPosition
                drawLine(0f, yPixel, viewSize.width.toFloat(), yPixel, linePaint)
                drawText(
                    legend.label,
                    legendMarginStart,
                    yPixel - legendMarginBottom,
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
        val zoom =
            if (yAxisDistance == 0f || absoluteDistance == 0f) 1f else yAxisDistance / absoluteDistance
        val absolutePosition = yPixelToValue(it, viewSize.height, absoluteMinY, absoluteMaxY)
        AnimatingYLegend(
            position = absolutePosition,
            label = axisFormatter.format(absolutePosition, zoom),
            interpolatedPosition = yValueToPixel(
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
