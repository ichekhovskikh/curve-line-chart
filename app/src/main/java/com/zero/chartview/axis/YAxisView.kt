package com.zero.chartview.axis

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.View
import com.zero.chartview.App
import com.zero.chartview.R
import com.zero.chartview.model.AnimatingLegendSeries
import com.zero.chartview.service.AnimationLegendService
import com.zero.chartview.utils.textHeight
import com.zero.chartview.utils.yPixelToValue
import com.zero.chartview.utils.yValueToPixel
import javax.inject.Inject

class YAxisView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var legendCount = resources.getInteger(R.integer.legend_line_count_default)
    private var legendMargin = resources.getDimension(R.dimen.legend_label_margin_default)

    private val legendPaint = Paint()
    private val gridPaint = Paint()

    private var legendPositions: List<Float> = emptyList()

    @Inject
    lateinit var animationLegendService: AnimationLegendService
        protected set

    init {
        App.appComponent.inject(this)
        animationLegendService.onInvalidate = ::invalidate
        var gridLineWidth = resources.getDimension(R.dimen.grid_line_width_default)
        var textSize = resources.getDimension(R.dimen.legend_text_size_default)
        context.theme.obtainStyledAttributes(attrs, R.styleable.YAxisView, defStyleAttr, defStyleRes).apply {
            legendCount = getInteger(R.styleable.YAxisView_legendLineCount, legendCount)
            legendMargin = getDimension(R.styleable.YAxisView_legendLabelMargin, legendMargin)
            textSize = getDimension(R.styleable.YAxisView_legendTextSize, textSize)
            gridLineWidth = getDimension(R.styleable.YAxisView_gridLineWidth, gridLineWidth)
            recycle()
        }
        initializePaint(textSize, gridLineWidth)
    }

    private fun initializePaint(textSize: Float, gridLineWidth: Float) {
        legendPaint.textSize = textSize
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
        legendSeries.legends = legendPositions.map { yPixelToValue(it, measuredHeight, legendSeries.minY, legendSeries.maxY) }
    }

    private fun getLegendPositions(availableHeight: Int, legendHeight: Float): List<Float> {
        val legendHeightWithMargin = (availableHeight - legendHeight) / (legendCount - 1)
        return (0 until legendCount).map { index -> legendHeightWithMargin * index }
    }

    override fun onDraw(canvas: Canvas) {
        animationLegendService.legendSeries.forEach { series ->
            gridPaint.color = getTransparencyColor(gridPaint.color, series)
            legendPaint.color = getTransparencyColor(legendPaint.color, series)
            series.legends.forEach { legend ->
                val yPixel = yValueToPixel(legend, measuredHeight, animationLegendService.minY, animationLegendService.maxY)
                canvas.drawLine(0f, yPixel, width.toFloat(), yPixel, gridPaint)
                canvas.drawText(legend.toString(), 0f, yPixel - legendMargin, legendPaint)
            }
        }
    }

    fun onThemeChanged(@ColorInt colorLegend: Int, @ColorInt colorGrid: Int) {
        legendPaint.color = colorLegend
        gridPaint.color = colorGrid
    }

    private fun getTransparencyColor(color: Int, labels: AnimatingLegendSeries): Int {
        return Color.argb(seriesTransparency(labels), Color.red(color), Color.green(color), Color.blue(color))
    }

    private fun seriesTransparency(labels: AnimatingLegendSeries) =
        (255 * if (labels.isAppearing) labels.animationValue else 1 - labels.animationValue).toInt()
}