package com.zero.chartview.axis

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.StyleRes
import com.zero.chartview.R
import com.zero.chartview.axis.formatter.AxisFormatter
import com.zero.chartview.delegate.XAxisDelegate
import com.zero.chartview.extensions.*
import com.zero.chartview.extensions.applyStyledAttributes
import com.zero.chartview.extensions.getColorCompat
import com.zero.chartview.model.AxisLine
import com.zero.chartview.model.FloatRange
import com.zero.chartview.model.PercentRange
import kotlinx.parcelize.Parcelize

internal class XAxisView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val delegate: XAxisDelegate

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

    val range get() = delegate.range

    @get:Px
    internal val legendTextHeightUsed
        get() = delegate.legendTextHeightUsed

    init {
        val legendPaint = Paint()
        var textMarginTop = resources.getDimension(R.dimen.x_legend_margin_top_default)
        var legendCount = resources.getInteger(R.integer.x_legend_count_default)
        var xLegendLinesVisible = false
        var textMarginHorizontalPercent = resources.getFraction(
            R.fraction.x_legend_margin_horizontal_percent_default,
            1,
            1
        )
        applyStyledAttributes(attrs, R.styleable.XAxisView, defStyleAttr, defStyleRes) {
            legendCount = getInteger(R.styleable.XAxisView_xLegendCount, legendCount)
            xLegendLinesVisible = getBoolean(
                R.styleable.XAxisView_xLegendLinesVisible,
                xLegendLinesVisible
            )
            legendPaint.textSize = getDimension(
                R.styleable.XAxisView_xLegendTextSize,
                resources.getDimension(R.dimen.x_legend_text_size_default)
            )
            legendPaint.color = getColor(
                R.styleable.XAxisView_xLegendTextColor,
                context.getColorCompat(R.color.colorXLegendText)
            )
            textMarginTop = getDimension(R.styleable.XAxisView_xLegendMarginTop, textMarginTop)
            textMarginHorizontalPercent = getFraction(
                R.styleable.XAxisView_xLegendMarginHorizontalPercent,
                1,
                1,
                textMarginHorizontalPercent
            )
        }
        delegate = XAxisDelegate(
            legendPaint,
            legendCount,
            xLegendLinesVisible,
            textMarginTop,
            textMarginHorizontalPercent,
            onUpdate = ::postInvalidateOnAnimation
        )
    }

    fun setAbscissas(abscissas: List<Float>) {
        delegate.setAbscissas(abscissas)
    }

    fun setRange(start: Float, endInclusive: Float, smoothScroll: Boolean) {
        val range = PercentRange(start, endInclusive)
        pendingSavedState.range = range
        delegate.setRange(range, smoothScroll)
    }

    internal fun setOnXAxisLinesChangedListener(onXAxisLinesChangedListener: ((xAxisLines: List<AxisLine>) -> Unit)?) {
        delegate.setOnXAxisLinesChangedListener(onXAxisLinesChangedListener)
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
        range = delegate.range
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superSavedState)

        state.range?.takeIfNull(pendingSavedState.range)?.also {
            pendingSavedState.range = it
        }
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
                pendingSavedState.range,
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
        var range: FloatRange? = null,
        var legendCount: Int? = null,
        var isLegendLinesAvailable: Boolean? = null,
        var textColor: Int? = null,
        var textSize: Float? = null
    ) : Parcelable
}
