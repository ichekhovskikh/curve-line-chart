package com.zero.chartview.popup

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorInt
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.Px
import com.zero.chartview.R
import com.zero.chartview.delegate.PopupLineDelegate
import com.zero.chartview.extensions.*
import com.zero.chartview.extensions.applyStyledAttributes
import com.zero.chartview.extensions.getColorCompat
import com.zero.chartview.extensions.on
import com.zero.chartview.model.CurveLine
import com.zero.chartview.model.PercentRange

internal class PopupLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val delegate: PopupLineDelegate

    var popupView: PopupView? = null
        set(value) {
            field = value
            when (value) {
                null -> delegate.setOnIntersectionsChangedListener(null)
                else -> delegate.setOnIntersectionsChangedListener { x, intersections ->
                    popupView?.isInvisible = intersections.isEmpty()
                    popupView?.bind(x, intersections)
                }
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

    @get:ColorInt
    @setparam:ColorInt
    var pointInnerColor: Int
        get() = delegate.pointInnerPaint.color
        set(value) {
            if (delegate.pointInnerPaint.color != value) {
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
                delegate.linePaint.strokeWidth = value
                invalidate()
            }
        }

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
}
