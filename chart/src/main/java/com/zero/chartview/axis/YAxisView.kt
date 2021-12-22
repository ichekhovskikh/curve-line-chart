package com.zero.chartview.axis

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.View
import com.zero.chartview.R
import com.zero.chartview.axis.formatter.AxisFormatter
import com.zero.chartview.delegate.YAxisDelegate
import com.zero.chartview.extensions.applyStyledAttributes
import com.zero.chartview.extensions.getColorCompat
import com.zero.chartview.extensions.on

internal class YAxisView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val legendPaint = Paint()

    private val gridPaint = Paint().apply {
        style = Paint.Style.STROKE
    }

    private val delegate: YAxisDelegate

    var axisFormatter: AxisFormatter
        get() = delegate.axisFormatter
        set(value) {
            delegate.axisFormatter = value
        }

    @get:ColorInt
    @setparam:ColorInt
    var legendColor: Int
        get() = legendPaint.color
        set(value) {
            legendPaint.color = value
        }

    @get:ColorInt
    @setparam:ColorInt
    var gridColor: Int
        get() = gridPaint.color
        set(value) {
            gridPaint.color = value
        }

    var legendCount: Int
        get() = delegate.getLegendCount()
        set(value) = delegate.setLegendCount(value)

    init {
        var legendCount = resources.getInteger(R.integer.legend_line_count_default)
        var startLegendMargin = resources.getDimension(R.dimen.start_legend_margin_default)
        var bottomLegendMargin = resources.getDimension(R.dimen.bottom_legend_margin_default)

        applyStyledAttributes(attrs, R.styleable.YAxisView, defStyleAttr, defStyleRes) {
            legendCount = getInteger(
                R.styleable.YAxisView_legendLineCount,
                legendCount
            )
            startLegendMargin = getDimension(
                R.styleable.YAxisView_startLegendMargin,
                startLegendMargin
            )
            bottomLegendMargin = getDimension(
                R.styleable.YAxisView_bottomLegendMargin,
                bottomLegendMargin
            )
            legendPaint.color = getColor(
                R.styleable.YAxisView_legendTextColor,
                context.getColorCompat(R.color.colorLegend)
            )
            legendPaint.textSize = getDimension(
                R.styleable.YAxisView_legendTextSize,
                resources.getDimension(R.dimen.legend_text_size_default)
            )
            gridPaint.color = getColor(
                R.styleable.YAxisView_gridColor,
                context.getColorCompat(R.color.colorGrid)
            )
            gridPaint.strokeWidth = getDimension(
                R.styleable.YAxisView_gridLineWidth,
                resources.getDimension(R.dimen.grid_line_width_default)
            )
        }
        delegate = YAxisDelegate(
            legendCount,
            startLegendMargin,
            bottomLegendMargin,
            onUpdate = ::postInvalidateOnAnimation
        )
    }

    fun setOrdinates(ordinates: List<Float>) {
        delegate.setOrdinates(ordinates)
    }

    fun setYAxis(minY: Float, maxY: Float) {
        delegate.setYAxis(minY, maxY)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        delegate.onMeasure(measuredWidth on measuredHeight, legendPaint.textSize)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        delegate.onLayout()
    }

    override fun onDraw(canvas: Canvas) {
        delegate.drawLegends(canvas, legendPaint, gridPaint)
    }
}
