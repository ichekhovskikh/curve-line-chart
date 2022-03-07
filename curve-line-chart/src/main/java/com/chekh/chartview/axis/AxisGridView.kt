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
import com.chekh.chartview.delegate.AxisGridDelegate
import com.chekh.chartview.extensions.applyStyledAttributes
import com.chekh.chartview.extensions.getColorCompat
import com.chekh.chartview.extensions.on
import com.chekh.chartview.extensions.takeIfNull
import com.chekh.chartview.model.AxisLine
import kotlinx.parcelize.Parcelize

/**
 * This view is a representation of the grid values of the graph axis
 */
internal class AxisGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val delegate: AxisGridDelegate

    private val pendingSavedState = SavedState()

    /**
     * Color of the axis lines
     */
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

    /**
     * Width of the axis lines
     */
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

    init {
        val linePaint = Paint().apply {
            style = Paint.Style.STROKE
        }
        applyStyledAttributes(attrs, R.styleable.YAxisView, defStyleAttr, defStyleRes) {
            linePaint.color = getColor(
                R.styleable.AxisGridView_axisLineColor,
                context.getColorCompat(R.color.colorAxisLine)
            )
            linePaint.strokeWidth = getDimension(
                R.styleable.AxisGridView_axisLineWidth,
                resources.getDimension(R.dimen.axis_line_width_default)
            )
        }
        delegate = AxisGridDelegate(
            linePaint,
            onUpdate = ::postInvalidateOnAnimation
        )
    }

    fun setXAxisLines(xAxisLines: List<AxisLine>) {
        delegate.setXAxisLines(xAxisLines)
    }

    fun setYAxisLines(yAxisLines: List<AxisLine>) {
        delegate.setYAxisLines(yAxisLines)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        delegate.onMeasure(measuredWidth on measuredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        delegate.drawChartGrid(canvas)
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

        state.lineColor?.takeIfNull(pendingSavedState.lineColor)?.also {
            pendingSavedState.lineColor = it
        }
        state.lineWidth?.takeIfNull(pendingSavedState.lineWidth)?.also {
            pendingSavedState.lineWidth = it
        }
        post {
            delegate.onRestoreInstanceState(
                pendingSavedState.lineColor,
                pendingSavedState.lineWidth
            )
        }
    }

    @Parcelize
    private data class SavedState(
        var superSavedState: Parcelable? = null,
        var lineColor: Int? = null,
        var lineWidth: Float? = null
    ) : Parcelable
}
