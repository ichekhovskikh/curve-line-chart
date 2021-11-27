package com.zero.chartview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.zero.chartview.delegate.ScrollFrameDelegate
import com.zero.chartview.extensions.applyStyledAttributes
import com.zero.chartview.extensions.on
import com.zero.chartview.model.FloatRange
import com.zero.chartview.model.PercentRange
import com.zero.chartview.model.Size

class ScrollFrameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var frameThicknessHorizontal: Float = resources.getDimension(R.dimen.frame_thickness_horizontal_default)
    private var frameThicknessVertical: Float = resources.getDimension(R.dimen.frame_thickness_vertical_default)
    private var frameMaxWidthPercent: Float = resources.getFraction(R.fraction.frame_max_width_percent_default, 1, 1)
    private var frameMinWidthPercent: Float = resources.getFraction(R.fraction.frame_min_width_percent_default, 1, 1)
    private var additionalTouchWidth: Float = resources.getDimension(R.dimen.additional_curtain_touch_width)

    private val framePaint = Paint()
    private val fogPaint = Paint()

    private var window = Size()

    val range get() = delegate.range

    private val delegate: ScrollFrameDelegate

    init {
        applyStyledAttributes(attrs, R.styleable.ScrollFrameView, defStyleAttr, defStyleRes) {
            frameThicknessHorizontal = getDimension(R.styleable.ScrollFrameView_frameThicknessHorizontal, frameThicknessHorizontal)
            frameThicknessVertical = getDimension(R.styleable.ScrollFrameView_frameThicknessVertical, frameThicknessVertical)
            frameMaxWidthPercent = getDimension(R.styleable.ScrollFrameView_frameMaxWidthPercent, frameMaxWidthPercent)
            frameMinWidthPercent = getDimension(R.styleable.ScrollFrameView_frameMinWidthPercent, frameMinWidthPercent)
        }
        delegate = ScrollFrameDelegate(
            frameThicknessHorizontal,
            frameThicknessVertical,
            frameMaxWidthPercent,
            frameMinWidthPercent,
            additionalTouchWidth,
            onUpdate = ::invalidate
        )
        setupPaint()
    }

    private fun setupPaint() {
        fogPaint.style = Paint.Style.FILL
        framePaint.style = Paint.Style.FILL
        fogPaint.color = resources.getColor(R.color.colorFogSelector)
        framePaint.color = resources.getColor(R.color.colorFrameSelector)
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

    fun setRange(start: Float, endInclusive: Float) {
        delegate.setRange(PercentRange(start, endInclusive))
    }

    fun addOnRangeChangedListener(listener: (FloatRange) -> Unit) {
        delegate.addOnRangeChangedListener(listener)
    }

    fun removeOnRangeChangedListener(listener: (FloatRange) -> Unit) {
        delegate.removeOnRangeChangedListener(listener)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent) = when (event.actionMasked) {
        MotionEvent.ACTION_DOWN -> {
            parent.requestDisallowInterceptTouchEvent(true)
            delegate.onActionDown(event, measuredWidth on measuredHeight)
            true
        }
        MotionEvent.ACTION_MOVE -> {
            delegate.onActionMove(event, measuredWidth on measuredHeight)
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
        delegate.viewSize = measuredWidth on measuredHeight
    }

    override fun onDraw(canvas: Canvas) {
        delegate.drawScrollFrame(canvas, framePaint, fogPaint, window)
    }
}