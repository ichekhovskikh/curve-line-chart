package com.zero.chartview.selector

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.ColorInt
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.zero.chartview.R
import com.zero.chartview.delegate.ScrollFrameDelegate
import com.zero.chartview.extensions.applyStyledAttributes
import com.zero.chartview.extensions.getColorCompat
import com.zero.chartview.extensions.on
import com.zero.chartview.model.PercentRange

internal class ScrollFrameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val delegate: ScrollFrameDelegate

    val range get() = delegate.range

    var isSmoothScrollEnabled
        get() = delegate.isSmoothScrollEnabled
        set(value) {
            delegate.isSmoothScrollEnabled = value
        }

    val frameMinWidthPercent
        get() = delegate.frameMinWidthPercent

    val frameMaxWidthPercent
        get() = delegate.frameMaxWidthPercent

    @get:ColorInt
    @setparam:ColorInt
    var frameColor: Int
        get() = delegate.framePaint.color
        set(value) {
            if (delegate.framePaint.color != value) {
                delegate.framePaint.color = value
                invalidate()
            }
        }

    @get:ColorInt
    @setparam:ColorInt
    var fogColor: Int
        get() = delegate.fogPaint.color
        set(value) {
            if (delegate.fogPaint.color != value) {
                delegate.fogPaint.color = value
                invalidate()
            }
        }

    init {
        val framePaint = Paint().apply {
            style = Paint.Style.FILL
        }
        val fogPaint = Paint().apply {
            style = Paint.Style.FILL
        }
        val dragIndicatorPaint = Paint().apply {
            style = Paint.Style.FILL
            color = context.getColorCompat(R.color.colorFrameDragIndicator)
        }
        var frameMaxWidthPercent = resources.getFraction(
            R.fraction.frame_max_width_percent_default, 1, 1
        )
        var frameMinWidthPercent = resources.getFraction(
            R.fraction.frame_min_width_percent_default, 1, 1
        )
        var isSmoothScrollEnabled = true
        val frameCornerRadius = resources.getDimension(
            R.dimen.frame_corner_radius_default
        )
        val frameThicknessHorizontal = resources.getDimension(
            R.dimen.frame_thickness_horizontal_default
        )
        val frameThicknessVertical = resources.getDimension(
            R.dimen.frame_thickness_vertical_default
        )
        val dragIndicatorCornerRadius = resources.getDimension(
            R.dimen.frame_drag_indicator_corner_radius_default
        )
        val dragIndicatorWidth = resources.getDimension(
            R.dimen.frame_drag_indicator_width_default
        )
        val dragIndicatorMaxHeight = resources.getDimension(
            R.dimen.frame_drag_indicator_max_height_default
        )

        applyStyledAttributes(attrs, R.styleable.ScrollFrameView, defStyleAttr, defStyleRes) {
            framePaint.color = getColor(
                R.styleable.ScrollFrameView_selectorFrameColor,
                context.getColorCompat(R.color.colorSelectorFrame)
            )
            fogPaint.color = getColor(
                R.styleable.ScrollFrameView_selectorFogColor,
                context.getColorCompat(R.color.colorSelectorFog)
            )
            frameMaxWidthPercent = getDimension(
                R.styleable.ScrollFrameView_selectorFrameMaxWidthPercent,
                frameMaxWidthPercent
            )
            frameMinWidthPercent = getDimension(
                R.styleable.ScrollFrameView_selectorFrameMinWidthPercent,
                frameMinWidthPercent
            )
            isSmoothScrollEnabled = getBoolean(
                R.styleable.ScrollFrameView_smoothScrollEnabled,
                isSmoothScrollEnabled
            )
        }
        delegate = ScrollFrameDelegate(
            framePaint,
            fogPaint,
            dragIndicatorPaint,
            frameCornerRadius,
            frameThicknessHorizontal,
            frameThicknessVertical,
            frameMaxWidthPercent,
            frameMinWidthPercent,
            dragIndicatorCornerRadius,
            dragIndicatorWidth,
            dragIndicatorMaxHeight,
            isSmoothScrollEnabled,
            onUpdate = ::postInvalidateOnAnimation
        )
    }

    fun setRange(start: Float, endInclusive: Float, smoothScroll: Boolean = false) {
        delegate.setRange(PercentRange(start, endInclusive), smoothScroll)
    }

    fun addOnRangeChangedListener(onRangeChangedListener: (start: Float, endInclusive: Float, smoothScroll: Boolean) -> Unit) {
        delegate.addOnRangeChangedListener(onRangeChangedListener)
    }

    fun removeOnRangeChangedListener(onRangeChangedListener: (start: Float, endInclusive: Float, smoothScroll: Boolean) -> Unit) {
        delegate.removeOnRangeChangedListener(onRangeChangedListener)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent) = when (event.actionMasked) {
        MotionEvent.ACTION_DOWN -> {
            parent.requestDisallowInterceptTouchEvent(true)
            delegate.onActionDown(event)
            true
        }
        MotionEvent.ACTION_MOVE -> {
            delegate.onActionMove(event)
            true
        }
        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
            parent.requestDisallowInterceptTouchEvent(false)
            true
        }
        else -> super.onTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        delegate.onMeasure(measuredWidth on measuredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        delegate.drawScrollFrame(canvas)
    }
}
