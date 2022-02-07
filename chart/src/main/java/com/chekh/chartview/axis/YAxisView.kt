package com.chekh.chartview.axis

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
import com.chekh.chartview.R
import com.chekh.chartview.axis.formatter.AxisFormatter
import com.chekh.chartview.delegate.YAxisDelegate
import com.chekh.chartview.extensions.applyStyledAttributes
import com.chekh.chartview.extensions.getColorCompat
import com.chekh.chartview.extensions.on
import com.chekh.chartview.extensions.takeIfNull
import com.chekh.chartview.model.AxisLine
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

    var legendCount: Int
        get() = delegate.legendCount
        set(value) {
            pendingSavedState.legendCount = value
            delegate.setLegendCount(value)
        }

    var isLegendLinesAvailable: Boolean
        get() = delegate.isLegendLinesAvailable
        set(value) {
            pendingSavedState.isLegendLinesAvailable = value
            delegate.setLegendLinesAvailable(value)
        }

    init {
        val legendPaint = Paint()
        var legendCount = resources.getInteger(R.integer.y_legend_count_default)
        var yLegendLinesVisible = true
        var legendMarginStart = resources.getDimension(R.dimen.y_legend_margin_start_default)
        var legendMarginBottom = resources.getDimension(R.dimen.y_legend_margin_bottom_default)

        applyStyledAttributes(attrs, R.styleable.YAxisView, defStyleAttr, defStyleRes) {
            legendCount = getInteger(
                R.styleable.YAxisView_yLegendCount,
                legendCount
            )
            yLegendLinesVisible = getBoolean(
                R.styleable.YAxisView_yLegendLinesVisible,
                yLegendLinesVisible
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
        }
        delegate = YAxisDelegate(
            legendPaint,
            legendCount,
            yLegendLinesVisible,
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

    internal fun setOnYAxisLinesChangedListener(onYAxisLinesChangedListener: ((yAxisLines: List<AxisLine>) -> Unit)?) {
        delegate.setOnYAxisLinesChangedListener(onYAxisLinesChangedListener)
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
        state.isLegendLinesAvailable?.takeIfNull(pendingSavedState.isLegendLinesAvailable)?.also {
            pendingSavedState.isLegendLinesAvailable = it
        }
        state.textColor?.takeIfNull(pendingSavedState.textColor)?.also {
            pendingSavedState.textColor = it
        }
        state.textSize?.takeIfNull(pendingSavedState.textSize)?.also {
            pendingSavedState.textSize = it
        }
        post {
            delegate.onRestoreInstanceState(
                pendingSavedState.legendCount,
                pendingSavedState.isLegendLinesAvailable,
                pendingSavedState.textColor,
                pendingSavedState.textSize
            )
        }
    }

    @Parcelize
    private data class SavedState(
        var superSavedState: Parcelable? = null,
        var legendCount: Int? = null,
        var isLegendLinesAvailable: Boolean? = null,
        var textColor: Int? = null,
        var textSize: Float? = null
    ) : Parcelable
}
