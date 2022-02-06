package com.zero.chartview.popup

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Parcelable
import androidx.annotation.ColorInt
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.Px
import androidx.annotation.StyleRes
import com.zero.chartview.R
import com.zero.chartview.delegate.PopupLineDelegate
import com.zero.chartview.extensions.*
import com.zero.chartview.extensions.applyStyledAttributes
import com.zero.chartview.extensions.getColorCompat
import com.zero.chartview.extensions.on
import com.zero.chartview.model.CurveLine
import com.zero.chartview.model.FloatRange
import com.zero.chartview.model.PercentRange
import kotlinx.parcelize.Parcelize

internal class PopupLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val delegate: PopupLineDelegate

    private val pendingSavedState = SavedState()

    var popupView: PopupView? = null
        set(value) {
            field = value
            when (value) {
                null -> delegate.setOnIntersectionsChangedListener(null)
                else -> delegate.setOnIntersectionsChangedListener { x, intersections ->
                    popupView?.bind(x, intersections)
                    popupView?.isInvisible = intersections.isEmpty()
                }
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

    @get:ColorInt
    @setparam:ColorInt
    var pointInnerColor: Int
        get() = delegate.pointInnerPaint.color
        set(value) {
            if (delegate.pointInnerPaint.color != value) {
                pendingSavedState.pointInnerColor = value
                delegate.pointInnerPaint.color = value
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

    @get:Px
    internal val paddingVerticalUsed
        get() = delegate.paddingVerticalUsed

    init {
        val linePaint = Paint()
        val pointInnerPaint = Paint().apply {
            style = Paint.Style.FILL
        }
        val pointOuterPaint = Paint().apply {
            style = Paint.Style.FILL
        }
        val pointInnerRadius = resources.getDimension(R.dimen.point_inner_radius)
        val pointOuterRadius = resources.getDimension(R.dimen.point_outer_radius)
        val deltaTrackingTouchPercent = resources.getFraction(
            R.fraction.delta_tracking_touch_percent,
            1,
            1
        )
        applyStyledAttributes(attrs, R.styleable.CurveLineChartView, defStyleAttr, defStyleRes) {
            linePaint.color = getColor(
                R.styleable.CurveLineChartView_popupLineColor,
                context.getColorCompat(R.color.colorPopupLine)
            )
            pointInnerPaint.color = getColor(
                R.styleable.CurveLineChartView_popupLinePointInnerColor,
                context.getColorCompat(R.color.colorPopupLinePointInner)
            )
            linePaint.strokeWidth = getDimensionPixelSize(
                R.styleable.CurveLineChartView_popupLineWidth,
                resources.getDimensionPixelSize(R.dimen.popup_line_width)
            ).toFloat()
        }
        delegate = PopupLineDelegate(
            linePaint,
            pointInnerPaint,
            pointOuterPaint,
            pointInnerRadius,
            pointOuterRadius,
            deltaTrackingTouchPercent,
            onUpdate = ::postInvalidateOnAnimation
        )
    }

    fun setRange(start: Float, endInclusive: Float) {
        delegate.setRange(PercentRange(start, endInclusive))
    }

    fun setLines(lines: List<CurveLine>) {
        delegate.setLines(lines)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent) = when {
        delegate.onTouchEvent(event) -> true
        else -> super.onTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        delegate.onMeasure(measuredWidth on measuredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        delegate.drawPopupLine(canvas)
    }

    override fun onSaveInstanceState(): Parcelable = pendingSavedState.apply {
        superSavedState = super.onSaveInstanceState()
        range = delegate.range
        touchX = delegate.touchX
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superSavedState)

        state.touchX?.takeIfNull(pendingSavedState.touchX)?.also {
            pendingSavedState.touchX = it
        }
        state.range?.takeIfNull(pendingSavedState.range)?.also {
            pendingSavedState.range = it
        }
        state.lineColor?.takeIfNull(pendingSavedState.lineColor)?.also {
            pendingSavedState.lineColor = it
        }
        state.pointInnerColor?.takeIfNull(pendingSavedState.pointInnerColor)?.also {
            pendingSavedState.pointInnerColor = it
        }
        state.lineWidth?.takeIfNull(pendingSavedState.lineWidth)?.also {
            pendingSavedState.lineWidth = it
        }
        post {
            delegate.onRestoreInstanceState(
                pendingSavedState.touchX,
                pendingSavedState.range,
                pendingSavedState.lineColor,
                pendingSavedState.pointInnerColor,
                pendingSavedState.lineWidth
            )
        }
    }

    @Parcelize
    private data class SavedState(
        var superSavedState: Parcelable? = null,
        var touchX: Float? = null,
        var range: FloatRange? = null,
        var lineColor: Int? = null,
        var pointInnerColor: Int? = null,
        var lineWidth: Float? = null
    ) : Parcelable
}
