package com.zero.chartview.delegate

import android.graphics.*
import android.view.MotionEvent
import com.zero.chartview.anim.AxisAnimator
import com.zero.chartview.extensions.distance
import com.zero.chartview.extensions.offset
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
    var isSmoothScrollEnabled: Boolean,
    private val onUpdate: () -> Unit
) {

    private val path = Path()
    private val frameInnerContour = RectF()
    private val frameOuterContour = RectF()
    private val leftDragIndicatorContour = RectF()
    private val rightDragIndicatorContour = RectF()
    private val touchPadding = frameThicknessVertical / 2

    private var downTouchPosition = 0f
    private var activeComponent = ComponentType.NOTHING
    private val onRangeChangedListeners = mutableListOf<(start: Float, endInclusive: Float, smoothScroll: Boolean) -> Unit>()

    var range = FloatRange(0f, frameMaxWidthPercent)
        private set

    private var viewSize = Size()

    private val axisAnimator = AxisAnimator(ANIMATION_DURATION_MS) { start, end, _, _ ->
        updateFrameCounters(FloatRange(start, end))
    }

    fun setRange(range: FloatRange, smoothScroll: Boolean = false) {
        if (this.range == range) return
        if (range.distance !in frameMinWidthPercent..frameMaxWidthPercent) return
        if (smoothScroll) {
            axisAnimator.reStart(this.range, range)
        } else {
            axisAnimator.cancel()
            updateFrameCounters(range)
        }
        this.range = range
        onRangeChanged(range, smoothScroll)
    }

    fun addOnRangeChangedListener(onRangeChangedListener: (start: Float, endInclusive: Float, smoothScroll: Boolean) -> Unit) {
        onRangeChangedListeners.add(onRangeChangedListener)
    }

    fun removeOnRangeChangedListener(onRangeChangedListener: (start: Float, endInclusive: Float, smoothScroll: Boolean) -> Unit) {
        onRangeChangedListeners.remove(onRangeChangedListener)
    }

    private fun onRangeChanged(range: FloatRange, smoothScroll: Boolean) {
        onRangeChangedListeners.forEach { it.invoke(range.start, range.endInclusive, smoothScroll) }
    }

    fun onActionDown(event: MotionEvent) {
        val abscissa = event.x
        downTouchPosition = abscissa

        val startPixel = xValueToPixel(range.start, viewSize.width, 0f, 1f)
        val endInclusivePixel = xValueToPixel(range.endInclusive, viewSize.width, 0f, 1f)

        val leftCurtainStart = startPixel - touchPadding
        val leftCurtainEnd = startPixel + frameThicknessVertical + touchPadding
        val rightCurtainStart = endInclusivePixel - frameThicknessVertical - touchPadding
        val rightCurtainEnd = endInclusivePixel + touchPadding

        activeComponent = when (abscissa) {
            in leftCurtainStart..leftCurtainEnd -> {
                setLeftCurtainPosition(abscissa - 0.5f * frameThicknessVertical)
                ComponentType.LEFT_CURTAIN
            }
            in rightCurtainStart..rightCurtainEnd -> {
                setRightCurtainPosition(abscissa + 0.5f * frameThicknessVertical)
                ComponentType.RIGHT_CURTAIN
            }
            !in leftCurtainStart..rightCurtainEnd -> {
                frameCenterByTouchPosition()
                ComponentType.FRAME
            }
            else -> {
                ComponentType.FRAME
            }
        }
    }

    fun onActionMove(event: MotionEvent) {
        when (activeComponent) {
            ComponentType.LEFT_CURTAIN -> setLeftCurtainPosition(event.x - 0.5f * frameThicknessVertical)
            ComponentType.RIGHT_CURTAIN -> setRightCurtainPosition(event.x + 0.5f * frameThicknessVertical)
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
        val (start, endInclusive) = when {
            range.start + increment < 0 -> 0f to range.endInclusive - range.start
            range.endInclusive + increment > 1 -> range.start + 1 - range.endInclusive to 1f
            else -> range.start + increment to range.endInclusive + increment
        }
        downTouchPosition = abscissa
        setRange(FloatRange(start, endInclusive))
    }

    private fun frameCenterByTouchPosition() {
        val touchAsPercent = downTouchPosition.pxToPercent()
        val halfRangeDistance = range.distance / 2

        val newRange = when {
            touchAsPercent < halfRangeDistance -> {
                range.offset(range.start)
            }
            1f - touchAsPercent < halfRangeDistance -> {
                range.offset(range.endInclusive - 1f)
            }
            else -> {
                range.offset(range.start + halfRangeDistance - touchAsPercent)
            }
        }
        setRange(newRange, smoothScroll = isSmoothScrollEnabled)
    }

    fun onMeasure(size: Size) {
        viewSize = size
        updateFrameCounters(range)
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
            viewSize.height.toFloat(),
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
            max(startPixel, 0f),
            0f,
            min(endInclusivePixel, viewSize.width.toFloat()),
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
        onUpdate.invoke()
    }

    private fun Float.pxToPercent() = xPixelToValue(this, viewSize.width, 0f, 1f)

    private enum class ComponentType {
        NOTHING, FRAME, LEFT_CURTAIN, RIGHT_CURTAIN
    }

    private companion object {
        const val ANIMATION_DURATION_MS = 150L
    }
}
