package com.zero.chartview.axis

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorInt
import android.util.AttributeSet
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.Px
import androidx.annotation.StyleRes
import com.zero.chartview.R
import com.zero.chartview.axis.formatter.AxisFormatter
import com.zero.chartview.delegate.YAxisDelegate
import com.zero.chartview.extensions.applyStyledAttributes
import com.zero.chartview.extensions.getColorCompat
import com.zero.chartview.extensions.on

internal class YAxisView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val delegate: YAxisDelegate

    var axisFormatter: AxisFormatter
        get() = delegate.axisFormatter
        set(value) {
            if (delegate.axisFormatter != value) {
                delegate.axisFormatter = value
                invalidate()
            }
        }

    @get:ColorInt
    @setparam:ColorInt
    var textColor: Int
        get() = delegate.legendPaint.color
        set(value) {
            if (delegate.legendPaint.color != value) {
                delegate.legendPaint.color = value
                invalidate()
            }
        }

    @get:ColorInt
    @setparam:ColorInt
    var lineColor: Int
        get() = delegate.linePaint.color
        set(value) {
            if (delegate.linePaint.color != value) {
                delegate.linePaint.color = value
                invalidate()
            }
        }

    @get:Px
    @setparam:Px
    var textSize: Float
        get() = delegate.legendPaint.textSize
        set(value) {
            if (delegate.legendPaint.textSize != value) {
                delegate.legendPaint.textSize = value
                invalidate()
            }
        }

    @get:Px
    @setparam:Px
    var lineWidth: Float
        get() = delegate.linePaint.strokeWidth
        set(value) {
            if (delegate.linePaint.strokeWidth != value) {
                delegate.linePaint.strokeWidth = value
                invalidate()
            }
        }

    var legendCount: Int
        get() = delegate.legendCount
        set(value) {
            delegate.legendCount = value
        }

    init {
        val legendPaint = Paint()
        val linePaint = Paint().apply {
            style = Paint.Style.STROKE
        }
        var legendCount = resources.getInteger(R.integer.y_legend_count_default)
        var legendMarginStart = resources.getDimension(R.dimen.y_legend_margin_start_default)
        var legendMarginBottom = resources.getDimension(R.dimen.y_legend_margin_bottom_default)

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
                resources.getDimension(R.dimen.y_legend_text_size_default)
            )
            linePaint.color = getColor(
                R.styleable.YAxisView_yLegendLineColor,
                context.getColorCompat(R.color.colorYLegendLine)
            )
            linePaint.strokeWidth = getDimension(
                R.styleable.YAxisView_yLegendLineWidth,
                resources.getDimension(R.dimen.y_legend_line_width_default)
            )
        }
        delegate = YAxisDelegate(
            legendPaint,
            linePaint,
            legendCount,
            legendMarginStart,
            legendMarginBottom,
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

    override fun onDraw(canvas: Canvas) {
        delegate.drawLegends(canvas)
    }
}
