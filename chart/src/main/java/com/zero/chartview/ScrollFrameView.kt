package com.zero.chartview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.zero.chartview.model.FloatRange
import com.zero.chartview.utils.xPixelToValue
import com.zero.chartview.utils.xValueToPixel

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
    private var downTouchPosition = 0f

    private val framePaint = Paint()
    private val fogPaint = Paint()
    private val path = Path()
    private val frameInnerContour = RectF()
    private val frameOuterContour = RectF()

    private var activeComponent = ComponentType.NOTHING
    private val rangeChangedInvokers = mutableListOf<(FloatRange) -> Unit>()

    var range = FloatRange(0f, frameMaxWidthPercent)
        private set


    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.ScrollFrameView, defStyleAttr, defStyleRes).apply {
            frameThicknessHorizontal =
                getDimension(R.styleable.ScrollFrameView_frameThicknessHorizontal, frameThicknessHorizontal)
            frameThicknessVertical =
                getDimension(R.styleable.ScrollFrameView_frameThicknessVertical, frameThicknessVertical)
            frameMaxWidthPercent = getDimension(R.styleable.ScrollFrameView_frameMaxWidthPercent, frameMaxWidthPercent)
            frameMinWidthPercent = getDimension(R.styleable.ScrollFrameView_frameMinWidthPercent, frameMinWidthPercent)
            recycle()
        }
        initializePaint()
    }

    private fun initializePaint() {
        fogPaint.style = Paint.Style.FILL
        framePaint.style = Paint.Style.FILL
        fogPaint.color = resources.getColor(R.color.colorFogSelector)
        framePaint.color = resources.getColor(R.color.colorFrameSelector)
    }

    fun setRange(start: Float, endInclusive: Float) {
        val length = endInclusive - start
        if (length in frameMinWidthPercent..frameMaxWidthPercent) {
            range = FloatRange(Math.max(start, 0f), Math.min(endInclusive, 1f))
            invalidate()
            rangeChanged()
        }
    }

    fun addRangeChangedInvoker(invoker: (FloatRange) -> Unit) {
        rangeChangedInvokers.add(invoker)
    }

    fun removeRangeChangedInvoker(invoker: (FloatRange) -> Unit) {
        rangeChangedInvokers.remove(invoker)
    }

    private fun rangeChanged() {
        rangeChangedInvokers.forEach { it.invoke(range) }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean =
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(true)
                onActionDown(event)
                true
            }
            MotionEvent.ACTION_MOVE -> {
                onActionMove(event)
                true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent.requestDisallowInterceptTouchEvent(false)
                true
            }
            else -> super.onTouchEvent(event)
        }

    private fun onActionDown(event: MotionEvent) {
        val abscissa = event.x
        val startPixel = xValueToPixel(range.start, measuredWidth, 0f, 1f)
        val endInclusivePixel = xValueToPixel(range.endInclusive, measuredWidth, 0f, 1f)

        val leftCurtainEnd = startPixel + frameThicknessVertical
        val rightCurtainStart = endInclusivePixel - frameThicknessVertical

        activeComponent = when {
            abscissa <= leftCurtainEnd + additionalTouchWidth -> {
                setLeftCurtainPosition(abscissa)
                ComponentType.LEFT_CURTAIN
            }
            abscissa >= rightCurtainStart - additionalTouchWidth -> {
                setRightCurtainPosition(abscissa)
                ComponentType.RIGHT_CURTAIN
            }
            else -> {
                downTouchPosition = abscissa
                ComponentType.FRAME
            }
        }
    }

    private fun onActionMove(event: MotionEvent) {
        when (activeComponent) {
            ComponentType.LEFT_CURTAIN -> setLeftCurtainPosition(event.x)
            ComponentType.RIGHT_CURTAIN -> setRightCurtainPosition(event.x)
            ComponentType.FRAME -> moveFrame(event.x)
            ComponentType.NOTHING -> {
                //Nothing
            }
        }
    }

    private fun setLeftCurtainPosition(abscissa: Float) {
        val start = xPixelToValue(abscissa, measuredWidth, 0f, 1f)
        setRange(Math.max(start, 0f), range.endInclusive)
    }

    private fun setRightCurtainPosition(abscissa: Float) {
        val endInclusive = xPixelToValue(abscissa, measuredWidth, 0f, 1f)
        setRange(range.start, Math.min(endInclusive, 1f))
    }

    private fun moveFrame(abscissa: Float) {
        val incrementPixel = abscissa - downTouchPosition
        val increment = xPixelToValue(incrementPixel, measuredWidth, 0f, 1f)
        val start = range.start + increment
        val endInclusive = range.endInclusive + increment
        if (start >= 0f && endInclusive <= 1f) {
            downTouchPosition = abscissa
            setRange(start, endInclusive)
        }
    }

    override fun onDraw(canvas: Canvas) {
        setFrameSize()
        path.rewind()
        path.moveTo(frameOuterContour.left, frameOuterContour.top)
        path.addRect(frameOuterContour, Path.Direction.CW)
        path.moveTo(frameInnerContour.left, frameInnerContour.top)
        path.addRect(frameInnerContour, Path.Direction.CCW)
        canvas.drawPath(path, framePaint)

        canvas.drawRect(0f, 0f, frameOuterContour.left, measuredHeight.toFloat(), fogPaint)
        canvas.drawRect(frameOuterContour.right, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), fogPaint)
    }

    private fun setFrameSize() {
        val startPixel = xValueToPixel(range.start, measuredWidth, 0f, 1f)
        val endInclusivePixel = xValueToPixel(range.endInclusive, measuredWidth, 0f, 1f)

        frameOuterContour.set(
            Math.max(startPixel - frameThicknessVertical / 2, 0f),
            0f,
            Math.min(endInclusivePixel + frameThicknessVertical / 2, measuredWidth.toFloat()),
            measuredHeight.toFloat()
        )
        frameInnerContour.set(
            frameOuterContour.left + frameThicknessVertical,
            frameOuterContour.top + frameThicknessHorizontal,
            frameOuterContour.right - frameThicknessVertical,
            frameOuterContour.bottom - frameThicknessHorizontal
        )
    }

    fun setFrameSelectorColor(@ColorInt frameSelectorColor: Int) {
        if (frameSelectorColor != framePaint.color) {
            framePaint.color = frameSelectorColor
        }
    }

    fun setFogSelectorColor(@ColorInt fogSelectorColor: Int) {
        if (fogSelectorColor != fogPaint.color) {
            fogPaint.color = fogSelectorColor
        }
    }

    enum class ComponentType {
        NOTHING, FRAME, LEFT_CURTAIN, RIGHT_CURTAIN
    }
}