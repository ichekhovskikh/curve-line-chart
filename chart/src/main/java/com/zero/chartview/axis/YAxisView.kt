package com.zero.chartview.axis

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Parcelable
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
import com.zero.chartview.extensions.takeIfNull
import kotlinx.parcelize.Parcelize

internal class YAxisView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val delegate: YAxisDelegate

    private val pendingSavedState = SavedState()

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
                pendingSavedState.textColor = value
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
                pendingSavedState.lineColor = value
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
                pendingSavedState.textSize = value
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
                pendingSavedState.lineWidth = value
                delegate.linePaint.strokeWidth = value
                invalidate()
            }
        }

    var legendCount: Int
        get() = delegate.legendCount
        set(value) {
            pendingSavedState.legendCount = value
            delegate.setLegendCount(value)
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

    fun setYAxis(minY: Float, maxY: Float, smoothScroll: Boolean) {
        delegate.setYAxis(minY, maxY, smoothScroll)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        delegate.onMeasure(measuredWidth on measuredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        delegate.drawLegends(canvas)
    }

    override fun onSaveInstanceState(): Parcelable = pendingSavedState.apply {
        superSavedState = super.onSaveInstanceState()
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superSavedState)

        state.legendCount?.takeIfNull(pendingSavedState.legendCount)?.also {
            pendingSavedState.legendCount = it
        }
        state.textColor?.takeIfNull(pendingSavedState.textColor)?.also {
            pendingSavedState.textColor = it
        }
        state.lineColor?.takeIfNull(pendingSavedState.lineColor)?.also {
            pendingSavedState.lineColor = it
        }
        state.textSize?.takeIfNull(pendingSavedState.textSize)?.also {
            pendingSavedState.textSize = it
        }
        state.lineWidth?.takeIfNull(pendingSavedState.lineWidth)?.also {
            pendingSavedState.lineWidth = it
        }
        post {
            delegate.onRestoreInstanceState(
                pendingSavedState.legendCount,
                pendingSavedState.textColor,
                pendingSavedState.lineColor,
                pendingSavedState.textSize,
                pendingSavedState.lineWidth
            )
        }
    }

    @Parcelize
    private data class SavedState(
        var superSavedState: Parcelable? = null,
        var legendCount: Int? = null,
        var textColor: Int? = null,
        var lineColor: Int? = null,
        var textSize: Float? = null,
        var lineWidth: Float? = null
    ) : Parcelable
}
