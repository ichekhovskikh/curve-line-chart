package com.zero.chartview

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.zero.chartview.model.FloatRange
import com.zero.chartview.utils.xPixelToValue
import com.zero.chartview.utils.xValueToPixel

class SelectorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var frameThicknessHorizontal: Float = resources.getDimension(R.dimen.frame_thickness_horizontal_default)
    private var frameThicknessVertical: Float = resources.getDimension(R.dimen.frame_thickness_vertical_default)
    private var frameMaxWidthPercent: Float = resources.getDimension(R.dimen.frame_max_width_percent_default)
    private var frameMinWidthPercent: Float = resources.getDimension(R.dimen.frame_min_width_percent_default)
    private var additionalTouchWidth: Float = resources.getDimension(R.dimen.additional_curtain_touch_width)
    private var downTouchPosition = 0f

    private val framePaint = Paint()
    private val fogPaint = Paint()

    private var activeComponent = ComponentType.NOTHING
    private lateinit var range: MutableLiveData<FloatRange>

    init {
        range.value = FloatRange(0f, 1f)
        context.theme.obtainStyledAttributes(attrs, R.styleable.SelectorView, defStyleAttr, defStyleRes).apply {
            frameThicknessHorizontal =
                getDimension(R.styleable.SelectorView_frameThicknessHorizontal, frameThicknessHorizontal)
            frameThicknessVertical =
                getDimension(R.styleable.SelectorView_frameThicknessVertical, frameThicknessVertical)
            frameMaxWidthPercent = getDimension(R.styleable.SelectorView_frameMaxWidthPercent, frameMaxWidthPercent)
            frameMinWidthPercent = getDimension(R.styleable.SelectorView_frameMinWidthPercent, frameMinWidthPercent)
            recycle()
        }
    }

    fun setRange(start: Float, endInclusive: Float) {
        val length = endInclusive - start
        if (length in frameMinWidthPercent..frameMaxWidthPercent) {
            range.value = FloatRange(Math.max(start, 0f), Math.min(endInclusive, 1f))
            invalidate()
        }
    }

    fun getRange(): LiveData<FloatRange> = range

    override fun onDraw(canvas: Canvas) {
        //TODO draw
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
        val startPixel = xValueToPixel(range.value!!.start, measuredWidth, 0f, 1f)
        val endInclusivePixel = xValueToPixel(range.value!!.endInclusive, measuredWidth, 0f, 1f)

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
        val startPixel = abscissa + frameThicknessVertical / 2
        val start = xPixelToValue(startPixel, measuredWidth, 0f, 1f)
        setRange(start, range.value!!.endInclusive)
    }

    private fun setRightCurtainPosition(abscissa: Float) {
        val endInclusivePixel = abscissa - frameThicknessVertical / 2
        val endInclusive = xPixelToValue(endInclusivePixel, measuredWidth, 0f, 1f)
        setRange(range.value!!.start, endInclusive)
    }

    private fun moveFrame(abscissa: Float) {
        val incrementPixel = abscissa - downTouchPosition
        val increment = xPixelToValue(incrementPixel, measuredWidth, 0f, 1f)
        setRange(range.value!!.start + increment, range.value!!.endInclusive + increment)
    }

    fun setFrameControlColor(@ColorInt frameControlColor: Int) {
        framePaint.color = frameControlColor
        invalidate()
    }

    fun setFogControlColor(@ColorInt fogControlColor: Int) {
        fogPaint.color = fogControlColor
        invalidate()
    }

    fun setThemeColor(@ColorInt frameControlColor: Int, @ColorInt fogControlColor: Int) {
        framePaint.color = frameControlColor
        fogPaint.color = fogControlColor
        invalidate()
    }

    enum class ComponentType {
        NOTHING, FRAME, LEFT_CURTAIN, RIGHT_CURTAIN
    }
}