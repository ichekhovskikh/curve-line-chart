package com.zero.chartview.delegate

import android.graphics.*
import android.view.MotionEvent
import com.zero.chartview.extensions.distance
import com.zero.chartview.model.*
import com.zero.chartview.tools.xPixelToValue
import com.zero.chartview.tools.xValueToPixel
import kotlin.math.max
import kotlin.math.min

internal class ScrollFrameDelegate(
    private val frameThicknessHorizontal: Float,
    private val frameThicknessVertical: Float,
    private val frameMaxWidthPercent: Float,
    private val frameMinWidthPercent: Float,
    private val additionalTouchWidth: Float,
    var onUpdate: (() -> Unit)? = null
) {

    private val path = Path()
    private val frameInnerContour = RectF()
    private val frameOuterContour = RectF()

    private var downTouchPosition = 0f
    private var activeComponent = ComponentType.NOTHING
    private val onRangeChangedListeners = mutableListOf<(FloatRange) -> Unit>()

    var range = FloatRange(0f, frameMaxWidthPercent)
        private set

    var viewSize = Size()
        set(value) {
            field = value
            updateFrameSize(range)
            onUpdate?.invoke()
        }

    fun setRange(range: FloatRange) {
        if (this.range == range) return
        if (range.distance in frameMinWidthPercent..frameMaxWidthPercent) {
            this.range = range
            updateFrameSize(range)
            onUpdate?.invoke()
            onRangeChanged()
        }
    }

    fun addOnRangeChangedListener(listener: (FloatRange) -> Unit) {
        onRangeChangedListeners.add(listener)
    }

    fun removeOnRangeChangedListener(listener: (FloatRange) -> Unit) {
        onRangeChangedListeners.remove(listener)
    }

    private fun onRangeChanged() {
        onRangeChangedListeners.forEach { it.invoke(range) }
    }

    fun onActionDown(event: MotionEvent, window: Size) {
        val abscissa = event.x
        val startPixel = xValueToPixel(range.start, window.width, 0f, 1f)
        val endInclusivePixel = xValueToPixel(range.endInclusive, window.width, 0f, 1f)

        val leftCurtainEnd = startPixel + frameThicknessVertical
        val rightCurtainStart = endInclusivePixel - frameThicknessVertical

        activeComponent = when {
            abscissa <= leftCurtainEnd + additionalTouchWidth -> {
                setLeftCurtainPosition(abscissa, window)
                ComponentType.LEFT_CURTAIN
            }
            abscissa >= rightCurtainStart - additionalTouchWidth -> {
                setRightCurtainPosition(abscissa, window)
                ComponentType.RIGHT_CURTAIN
            }
            else -> {
                downTouchPosition = abscissa
                ComponentType.FRAME
            }
        }
    }

    fun onActionMove(event: MotionEvent, window: Size) {
        when (activeComponent) {
            ComponentType.LEFT_CURTAIN -> setLeftCurtainPosition(event.x, window)
            ComponentType.RIGHT_CURTAIN -> setRightCurtainPosition(event.x, window)
            ComponentType.FRAME -> moveFrame(event.x, window)
            ComponentType.NOTHING -> Unit
        }
    }

    private fun setLeftCurtainPosition(abscissa: Float, window: Size) {
        val start = xPixelToValue(abscissa, window.width, 0f, 1f)
        setRange(range.copy(start = start))
    }

    private fun setRightCurtainPosition(abscissa: Float, window: Size) {
        val endInclusive = xPixelToValue(abscissa, window.width, 0f, 1f)
        setRange(range.copy(endInclusive = endInclusive))
    }

    private fun moveFrame(abscissa: Float, window: Size) {
        val incrementPixel = abscissa - downTouchPosition
        val increment = xPixelToValue(incrementPixel, window.width, 0f, 1f)
        val start = range.start + increment
        val endInclusive = range.endInclusive + increment
        if (start >= 0f && endInclusive <= 1f) {
            downTouchPosition = abscissa
            setRange(FloatRange(start, endInclusive))
        }
    }

    fun drawScrollFrame(canvas: Canvas, framePaint: Paint, fogPaint: Paint, window: Size) {
        path.rewind()
        path.moveTo(frameOuterContour.left, frameOuterContour.top)
        path.addRect(frameOuterContour, Path.Direction.CW)
        path.moveTo(frameInnerContour.left, frameInnerContour.top)
        path.addRect(frameInnerContour, Path.Direction.CCW)
        canvas.drawPath(path, framePaint)

        canvas.drawRect(0f, 0f, frameOuterContour.left, window.height.toFloat(), fogPaint)
        canvas.drawRect(frameOuterContour.right, 0f, window.width.toFloat(), window.height.toFloat(), fogPaint)
    }

    private fun updateFrameSize(range: FloatRange) {
        val startPixel = xValueToPixel(range.start, viewSize.width, 0f, 1f)
        val endInclusivePixel = xValueToPixel(range.endInclusive, viewSize.width, 0f, 1f)

        frameOuterContour.set(
            max(startPixel - frameThicknessVertical / 2, 0f),
            0f,
            min(endInclusivePixel + frameThicknessVertical / 2, viewSize.width.toFloat()),
            viewSize.height.toFloat()
        )
        frameInnerContour.set(
            frameOuterContour.left + frameThicknessVertical,
            frameOuterContour.top + frameThicknessHorizontal,
            frameOuterContour.right - frameThicknessVertical,
            frameOuterContour.bottom - frameThicknessHorizontal
        )
    }

    private enum class ComponentType {
        NOTHING, FRAME, LEFT_CURTAIN, RIGHT_CURTAIN
    }
}