package com.zero.chartview.selector

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.annotation.ColorInt
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

    private val framePaint = Paint().apply {
        style = Paint.Style.FILL
    }

    private val fogPaint = Paint().apply {
        style = Paint.Style.FILL
    }

    private val dragIndicatorPaint = Paint().apply {
        style = Paint.Style.FILL
        color = context.getColorCompat(R.color.colorFrameDragIndicator)
    }

    val range get() = delegate.range

    var isSmoothScrollEnabled
        get() = delegate.isSmoothScrollEnabled
        set(value) {
            delegate.isSmoothScrollEnabled = value
        }

    private val delegate: ScrollFrameDelegate

    init {
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
                R.styleable.ScrollFrameView_colorFrameSelector,
                context.getColorCompat(R.color.colorFrameSelector)
            )
            fogPaint.color = getColor(
                R.styleable.ScrollFrameView_colorFogSelector,
                context.getColorCompat(R.color.colorFogSelector)
            )
            frameMaxWidthPercent = getDimension(
                R.styleable.ScrollFrameView_frameMaxWidthPercent,
                frameMaxWidthPercent
            )
            frameMinWidthPercent = getDimension(
                R.styleable.ScrollFrameView_frameMinWidthPercent,
                frameMinWidthPercent
            )
            isSmoothScrollEnabled = getBoolean(
                R.styleable.ScrollFrameView_smoothScrollEnabled,
                true
            )
        }
        delegate = ScrollFrameDelegate(
            frameCornerRadius,
            frameThicknessHorizontal,
            frameThicknessVertical,
            frameMaxWidthPercent,
            frameMinWidthPercent,
            dragIndicatorCornerRadius,
            dragIndicatorWidth,
            dragIndicatorMaxHeight,
            isSmoothScrollEnabled,
            onUpdate = ::invalidate
        )
    }

    fun setFrameSelectorColor(@ColorInt frameSelectorColor: Int) {
        if (frameSelectorColor != framePaint.color) {
            framePaint.color = frameSelectorColor
            invalidate()
        }
    }

    fun setFogSelectorColor(@ColorInt fogSelectorColor: Int) {
        if (fogSelectorColor != fogPaint.color) {
            fogPaint.color = fogSelectorColor
            invalidate()
        }
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
        delegate.drawScrollFrame(canvas, framePaint, fogPaint, dragIndicatorPaint)
    }
}