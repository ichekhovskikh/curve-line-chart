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
    private val frameCornerRadius: Float,
    private val frameThicknessHorizontal: Float,
    private val frameThicknessVertical: Float,
    private val frameMaxWidthPercent: Float,
    private val frameMinWidthPercent: Float,
    private val dragIndicatorCornerRadius: Float,
    private val dragIndicatorWidth: Float,
    private val dragIndicatorMaxHeight: Float,
    var onUpdate: (() -> Unit)? = null
) {

    private val path = Path()
    private val frameInnerContour = RectF()
    private val frameOuterContour = RectF()
    private val leftDragIndicatorContour = RectF()
    private val rightDragIndicatorContour = RectF()

    private var downTouchPosition = 0f
    private var activeComponent = ComponentType.NOTHING
    private val onRangeChangedListeners = mutableListOf<(FloatRange) -> Unit>()

    var range = FloatRange(0f, frameMaxWidthPercent)
        private set

    var viewSize = Size()
        set(value) {
            field = value
            updateFrameCounters(range)
            onUpdate?.invoke()
        }

    fun setRange(range: FloatRange) {
        if (this.range == range) return
        if (range.distance in frameMinWidthPercent..frameMaxWidthPercent) {
            this.range = range
            updateFrameCounters(range)
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

    fun onActionDown(event: MotionEvent) {
        val abscissa = event.x
        val startPixel = xValueToPixel(range.start, viewSize.width, 0f, 1f)
        val endInclusivePixel = xValueToPixel(range.endInclusive, viewSize.width, 0f, 1f)

        // the additional half width of the curtain is used by the curtain
        // to prevent frame shift mistakes instead of the curtain shift
        val leftCurtainStart = startPixel - frameThicknessVertical
        val leftCurtainEnd = startPixel + frameThicknessVertical
        val rightCurtainStart = endInclusivePixel - frameThicknessVertical
        val rightCurtainEnd = endInclusivePixel + frameThicknessVertical

        activeComponent = when (abscissa) {
            in leftCurtainStart..leftCurtainEnd -> {
                setLeftCurtainPosition(abscissa)
                ComponentType.LEFT_CURTAIN
            }
            in rightCurtainStart..rightCurtainEnd -> {
                setRightCurtainPosition(abscissa)
                ComponentType.RIGHT_CURTAIN
            }
            else -> {
                // todo move left and right curtain (offset range)
                downTouchPosition = abscissa
                ComponentType.FRAME
            }
        }
    }

    fun onActionMove(event: MotionEvent) {
        when (activeComponent) {
            ComponentType.LEFT_CURTAIN -> setLeftCurtainPosition(event.x)
            ComponentType.RIGHT_CURTAIN -> setRightCurtainPosition(event.x)
            ComponentType.FRAME -> moveFrame(event.x)
            ComponentType.NOTHING -> Unit
        }
    }

    private fun setLeftCurtainPosition(abscissa: Float) {
        setRange(range.copy(start = abscissa.pxToPercent()))
    }

    private fun setRightCurtainPosition(abscissa: Float) {
        setRange(range.copy(endInclusive = abscissa.pxToPercent()))
    }

    private fun moveFrame(abscissa: Float) {
        val increment = (abscissa - downTouchPosition).pxToPercent()
        val start = range.start + increment
        val endInclusive = range.endInclusive + increment
        if (start >= 0f && endInclusive <= 1f) {
            downTouchPosition = abscissa
            setRange(FloatRange(start, endInclusive))
        }
    }

    fun drawScrollFrame(
        canvas: Canvas,
        framePaint: Paint,
        fogPaint: Paint,
        dragIndicatorPaint: Paint
    ) {
        path.rewind()
        canvas.drawFog(fogPaint)
        canvas.drawFrame(framePaint)
        canvas.drawLeftDragIndicator(dragIndicatorPaint)
        canvas.drawRightDragIndicator(dragIndicatorPaint)
    }

    private fun Canvas.drawFog(paint: Paint) {
        drawRect(
            0f,
            0f,
            frameInnerContour.left,
            viewSize.height.toFloat(),
            paint
        )
        drawRect(
            frameInnerContour.right,
            0f,
            viewSize.width.toFloat(),
            viewSize.height.toFloat(),
            paint
        )
        drawRect(
            frameInnerContour.left,
            0f,
            frameInnerContour.right,
            frameInnerContour.top,
            paint
        )
        drawRect(
            frameInnerContour.left,
            frameInnerContour.bottom,
            frameInnerContour.right,
            0f,
            paint
        )
    }

    private fun Canvas.drawFrame(paint: Paint) {
        path.moveTo(frameOuterContour.left, frameOuterContour.top)
        path.addRoundRect(
            frameOuterContour,
            frameCornerRadius,
            frameCornerRadius,
            Path.Direction.CW
        )
        path.moveTo(frameInnerContour.left, frameInnerContour.top)
        path.addRect(frameInnerContour, Path.Direction.CCW)
        drawPath(path, paint)
    }

    private fun Canvas.drawLeftDragIndicator(paint: Paint) {
        drawRoundRect(
            leftDragIndicatorContour,
            dragIndicatorCornerRadius,
            dragIndicatorCornerRadius,
            paint
        )
    }

    private fun Canvas.drawRightDragIndicator(paint: Paint) {
        drawRoundRect(
            rightDragIndicatorContour,
            dragIndicatorCornerRadius,
            dragIndicatorCornerRadius,
            paint
        )
    }

    private fun updateFrameCounters(range: FloatRange) {
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

        val frameCurtainWidth = frameInnerContour.left - frameOuterContour.left
        val dragIndicatorHorizontalOffset = (frameCurtainWidth - dragIndicatorWidth) / 2
        val dragIndicatorHeight = min(frameInnerContour.height() / 3, dragIndicatorMaxHeight)
        val dragIndicatorTop = (frameOuterContour.bottom - dragIndicatorHeight) / 2
        val dragIndicatorBottom = dragIndicatorTop + dragIndicatorHeight
        leftDragIndicatorContour.set(
            frameOuterContour.left + dragIndicatorHorizontalOffset,
            dragIndicatorTop,
            frameInnerContour.left - dragIndicatorHorizontalOffset,
            dragIndicatorBottom
        )
        rightDragIndicatorContour.set(
            frameInnerContour.right + dragIndicatorHorizontalOffset,
            dragIndicatorTop,
            frameOuterContour.right - dragIndicatorHorizontalOffset,
            dragIndicatorBottom
        )
    }

    private fun Float.pxToPercent() = xPixelToValue(this, viewSize.width, 0f, 1f)

    private enum class ComponentType {
        NOTHING, FRAME, LEFT_CURTAIN, RIGHT_CURTAIN
    }
}