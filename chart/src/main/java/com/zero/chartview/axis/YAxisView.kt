package com.zero.chartview.axis

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.View
import com.zero.chartview.R
import com.zero.chartview.extensions.animatingColor
import com.zero.chartview.model.AnimatingLegendSeries
import com.zero.chartview.delegate.AnimationLegendService
import com.zero.chartview.tools.formatLegend
import com.zero.chartview.extensions.textHeight
import com.zero.chartview.tools.yPixelToValue
import com.zero.chartview.tools.yValueToPixel

internal class YAxisView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var legendCount = resources.getInteger(R.integer.legend_line_count_default)
    private var legendMargin = resources.getDimension(R.dimen.ordinate_legend_label_margin_default)

    private val legendPaint = Paint()
    private val gridPaint = Paint()

    private var legendPositions: List<Float> = emptyList()

    var animationLegendService = AnimationLegendService(onUpdate = ::invalidate)
        private set

    init {
        var gridLineWidth = resources.getDimension(R.dimen.grid_line_width_default)
        var textSize = resources.getDimension(R.dimen.legend_text_size_default)
        context.theme.obtainStyledAttributes(attrs, R.styleable.YAxisView, defStyleAttr, defStyleRes).apply {
            legendCount = getInteger(R.styleable.YAxisView_legendLineCount, legendCount)
            legendMargin = getDimension(R.styleable.YAxisView_ordinateLegendLabelMargin, legendMargin)
            textSize = getDimension(R.styleable.YAxisView_legendTextSize, textSize)
            gridLineWidth = getDimension(R.styleable.YAxisView_gridLineWidth, gridLineWidth)
            recycle()
        }
        initializePaint(textSize, gridLineWidth)
    }

    private fun initializePaint(textSize: Float, gridLineWidth: Float) {
        legendPaint.textSize = textSize
        legendPaint.color = resources.getColor(R.color.colorLegend)
        gridPaint.color = resources.getColor(R.color.colorGrid)
        gridPaint.style = Paint.Style.STROKE
        gridPaint.strokeWidth = gridLineWidth
    }

    fun setYAxis(minY: Float, maxY: Float) {
        animationLegendService.legendSeries.forEach {
            if (it.isAppearing) {
                it.isAppearing = false
                it.animationValue = 0f
            }
        }
        animationLegendService.legendSeries.add(createLegendSeries(minY, maxY, legendPositions))
        animationLegendService.setYAxis(minY, maxY)
    }

    private fun createLegendSeries(minY: Float, maxY: Float, labelPositions: List<Float>): AnimatingLegendSeries {
        val legends = labelPositions.map { yPixelToValue(it, measuredHeight, minY, maxY) }
        return AnimatingLegendSeries(legends, minY, maxY, true, 0f)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val legendHeight = legendPaint.textHeight + legendMargin
        legendPositions = getLegendPositions(measuredHeight, legendHeight)
        animationLegendService.legendSeries.forEach { updateLegendSeries(it, legendPositions) }
    }

    private fun updateLegendSeries(legendSeries: AnimatingLegendSeries, legendPositions: List<Float>) {
        legendSeries.legends = legendPositions.map {
            yPixelToValue(it, measuredHeight, legendSeries.minY, legendSeries.maxY)
        }
    }

    private fun getLegendPositions(availableHeight: Int, legendHeight: Float): List<Float> {
        val legendHeightWithMargin = (availableHeight - legendHeight) / (legendCount - 1)
        return (0 until legendCount).map { index -> legendHeightWithMargin * index }
    }

    override fun onDraw(canvas: Canvas) {
        animationLegendService.legendSeries.forEach { series ->
            gridPaint.color = series.animatingColor(gridPaint.color)
            legendPaint.color = series.animatingColor(legendPaint.color)
            series.legends.forEach { legend ->
                val yPixel =
                    yValueToPixel(legend, measuredHeight, animationLegendService.minY, animationLegendService.maxY)
                canvas.drawLine(0f, yPixel, width.toFloat(), yPixel, gridPaint)
                canvas.drawText(formatLegend(legend), 0f, yPixel - legendMargin, legendPaint)
            }
        }
    }

    fun setGridColor(@ColorInt gridColor: Int) {
        if (gridColor != gridPaint.color) {
            gridPaint.color = gridColor
        }
    }

    fun setLegendColor(@ColorInt legendColor: Int) {
        if (legendColor != legendPaint.color) {
            legendPaint.color = legendColor
        }
    }
}