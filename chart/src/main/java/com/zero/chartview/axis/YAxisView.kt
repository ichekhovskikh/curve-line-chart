package com.zero.chartview.axis

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorInt
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

    private val delegate: YAxisDelegate

    var axisFormatter: AxisFormatter
        get() = delegate.axisFormatter
        set(value) {
            delegate.axisFormatter = value
        }

    @get:ColorInt
    @setparam:ColorInt
    var textColor: Int
        get() = delegate.legendPaint.color
        set(value) {
            delegate.legendPaint.color = value
        }

    @get:ColorInt
    @setparam:ColorInt
    var lineColor: Int
        get() = delegate.linePaint.color
        set(value) {
            delegate.linePaint.color = value
        }

    var legendCount: Int
        get() = delegate.getLegendCount()
        set(value) = delegate.setLegendCount(value)

    init {
        val legendPaint = Paint()
        val linePaint = Paint().apply {
            style = Paint.Style.STROKE
        }
        var legendCount = resources.getInteger(R.integer.legend_line_count_default)
        var legendMarginStart = resources.getDimension(R.dimen.start_legend_margin_default)
        var legendMarginBottom = resources.getDimension(R.dimen.bottom_legend_margin_default)

        applyStyledAttributes(attrs, R.styleable.YAxisView, defStyleAttr, defStyleRes) {
            legendCount = getInteger(
                R.styleable.YAxisView_yLegendCount,
                legendCount
            )
            legendMarginStart = getDimension(
                R.styleable.YAxisView_yLegendMarginStart,
                legendMarginStart
            )
            legendMarginBottom = getDimension(
                R.styleable.YAxisView_yLegendMarginBottom,
                legendMarginBottom
            )
            legendPaint.color = getColor(
                R.styleable.YAxisView_yLegendTextColor,
                context.getColorCompat(R.color.colorYLegendText)
            )
            legendPaint.textSize = getDimension(
                R.styleable.YAxisView_yLegendTextSize,
                resources.getDimension(R.dimen.legend_text_size_default)
            )
            linePaint.color = getColor(
                R.styleable.YAxisView_yLegendLineColor,
                context.getColorCompat(R.color.colorYLegendLine)
            )
            linePaint.strokeWidth = getDimension(
                R.styleable.YAxisView_yLegendLineWidth,
                resources.getDimension(R.dimen.grid_line_width_default)
            )
        }
        delegate = YAxisDelegate(
            legendCount,
            legendMarginStart,
            legendMarginBottom,
            legendPaint,
            linePaint,
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
        delegate.onMeasure(measuredWidth on measuredHeight)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        delegate.onLayout()
    }

    override fun onDraw(canvas: Canvas) {
        delegate.drawLegends(canvas)
    }
}
