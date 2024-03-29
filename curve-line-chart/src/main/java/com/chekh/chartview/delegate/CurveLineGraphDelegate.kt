package com.chekh.chartview.delegate

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.view.MotionEvent
import android.view.VelocityTracker
import androidx.annotation.Px
import com.chekh.chartview.anim.AppearanceAnimator
import com.chekh.chartview.anim.AxisAnimator
import com.chekh.chartview.extensions.animatingColor
import com.chekh.chartview.extensions.getAbscissaBoundaries
import com.chekh.chartview.extensions.getMinMaxY
import com.chekh.chartview.extensions.interpolateByValues
import com.chekh.chartview.extensions.contains
import com.chekh.chartview.extensions.abscissas
import com.chekh.chartview.extensions.distance
import com.chekh.chartview.extensions.setAppearing
import com.chekh.chartview.extensions.setDisappearing
import com.chekh.chartview.model.CurveLine
import com.chekh.chartview.model.BinaryRange
import com.chekh.chartview.model.FloatRange
import com.chekh.chartview.model.AnimatingCurveLine
import com.chekh.chartview.model.AppearingCurveLine
import com.chekh.chartview.model.Size
import com.chekh.chartview.tools.pxToAbscissa
import com.chekh.chartview.tools.abscissaToPx
import com.chekh.chartview.tools.ordinateToPx

@Suppress("TooManyFunctions")
internal class CurveLineGraphDelegate(
    internal val paint: Paint,
    private val onUpdate: () -> Unit
) {

    private var currentMaxY = 0f
    private var currentMinY = 0f
    private var isAnimateYAxis = true
    private var maxYAfterAnimate = 0f
    private var minYAfterAnimate = 0f
    private var viewSize = Size()

    internal var range = BinaryRange()
        private set

    internal val linesAfterAnimate
        get() = animatingLines
            .filter { it.isAppearing }
            .map { it.curveLine }

    private val path = Path()
    private var currentLines = emptyList<CurveLine>()
    private val animatingLines = mutableListOf<AnimatingCurveLine>()

    private val onLinesChangedListeners = mutableListOf<(List<CurveLine>) -> Unit>()

    private val onRangeChangedListeners =
        mutableListOf<(start: Float, endInclusive: Float, smoothScroll: Boolean) -> Unit>()

    private val onYAxisChangedListeners =
        mutableListOf<(minY: Float, maxY: Float, smoothScroll: Boolean) -> Unit>()

    @Px
    private var lastMotionX = 0f
    private var velocityTracker: VelocityTracker? = null

    private val appearanceAnimator = AppearanceAnimator { value ->
        animatingLines.forEach { line ->
            if (line.animationValue <= value) {
                line.animationValue = value
                onUpdate()
            }
        }
    }.doOnEnd(::removeDisappearingLines)

    private val axisAnimator = AxisAnimator { startX, endX, startY, endY ->
        this.currentMinY = startY
        this.currentMaxY = endY
        animatingLines.forEach { line ->
            line.drawPixelPoints = transformAxis(line.curveLine.points, FloatRange(startX, endX))
        }
        onUpdate()
    }

    private fun removeDisappearingLines() {
        animatingLines.removeAll { !it.isAppearing && it.animationValue == 1f }
    }

    fun setRange(range: FloatRange, smoothScroll: Boolean = false) {
        if (this.range == range) return
        updateAxis(newRange = range, smoothScroll = smoothScroll)
    }

    fun setLines(newLines: List<CurveLine>) {
        val current = linesAfterAnimate
        if (newLines == current) return

        appearanceAnimator.cancel()
        val appearing = newLines.minus(current.toSet())
        appearing.forEach { line ->
            animatingLines
                .find { it.curveLine == line }?.setAppearing()
                ?: animatingLines.add(AppearingCurveLine(line))
        }

        val disappearing = current.minus(newLines.toSet())
        disappearing.forEach { line ->
            animatingLines.find { it.curveLine == line }?.setDisappearing()
        }
        appearanceAnimator.start()
        updateAxis(newLines)
        onLinesChanged(newLines)
    }

    fun addLine(line: CurveLine) {
        val current = linesAfterAnimate
        if (current.contains(line)) return

        appearanceAnimator.cancel()
        animatingLines
            .find { it.curveLine == line }?.setAppearing()
            ?: animatingLines.add(AppearingCurveLine(line))
        appearanceAnimator.start()
        val newLines = current + line
        updateAxis(newLines)
        onLinesChanged(newLines)
    }

    fun removeLine(line: CurveLine) {
        val current = linesAfterAnimate
        if (!current.contains(line)) return

        appearanceAnimator.cancel()
        animatingLines.find { it.curveLine == line }?.setDisappearing()
        appearanceAnimator.start()
        val newLines = current - line
        updateAxis(newLines)
        onLinesChanged(newLines)
    }

    private fun updateAxis(
        newLines: List<CurveLine> = currentLines,
        newRange: FloatRange = range,
        smoothScroll: Boolean = false,
        isAnimate: Boolean = true
    ) {
        val (minY, maxY) = newLines.getMinMaxY(newRange)
        if (this.maxYAfterAnimate != maxY || this.minYAfterAnimate != minY || isAnimateYAxis != isAnimate) {
            maxYAfterAnimate = maxY
            minYAfterAnimate = minY
            isAnimateYAxis = isAnimate
            onYAxisChanged(minY, maxY, isAnimate)
        }
        val currentRange = range
        if (isAnimate) {
            if (!smoothScroll) {
                range = newRange
            }
            axisAnimator.reStart(
                fromXRange = range.interpolateByValues(currentLines.abscissas),
                toXRange = newRange.interpolateByValues(newLines.abscissas),
                fromYRange = FloatRange(this.currentMinY, this.currentMaxY),
                toYRange = FloatRange(minY, maxY)
            )
            range = newRange
            currentLines = newLines
        } else {
            appearanceAnimator.cancel()
            axisAnimator.cancel()
            currentMinY = minYAfterAnimate
            currentMaxY = maxYAfterAnimate
            range = newRange
            currentLines = newLines
            val interpolatedRange = range.interpolateByValues(currentLines.abscissas)
            animatingLines.forEach { line ->
                line.animationValue = 1f
                line.drawPixelPoints = transformAxis(
                    line.curveLine.points,
                    interpolatedRange
                )
            }
            onUpdate()
        }
        if (newRange != currentRange) {
            onRangeChanged(newRange, smoothScroll)
        }
    }

    fun addOnLinesChangedListener(onLinesChangedListener: (List<CurveLine>) -> Unit) {
        onLinesChangedListeners.add(onLinesChangedListener)
    }

    fun removeOnLinesChangedListener(onLinesChangedListener: (List<CurveLine>) -> Unit) {
        onLinesChangedListeners.remove(onLinesChangedListener)
    }

    fun addOnRangeChangedListener(
        onRangeChangedListener: (start: Float, endInclusive: Float, smoothScroll: Boolean) -> Unit
    ) {
        onRangeChangedListeners.add(onRangeChangedListener)
    }

    fun removeOnRangeChangedListener(
        onRangeChangedListener: (start: Float, endInclusive: Float, smoothScroll: Boolean) -> Unit
    ) {
        onRangeChangedListeners.remove(onRangeChangedListener)
    }

    fun addOnYAxisChangedListener(
        onYAxisChangedListener: ((minY: Float, maxY: Float, smoothScroll: Boolean) -> Unit)
    ) {
        onYAxisChangedListeners.add(onYAxisChangedListener)
    }

    fun removeOnYAxisChangedListener(
        onYAxisChangedListener: ((minY: Float, maxY: Float, smoothScroll: Boolean) -> Unit)
    ) {
        onYAxisChangedListeners.remove(onYAxisChangedListener)
    }

    private fun onLinesChanged(lines: List<CurveLine>) {
        onLinesChangedListeners.forEach { it(lines) }
    }

    private fun onRangeChanged(range: FloatRange, smoothScroll: Boolean) {
        onRangeChangedListeners.forEach { it(range.start, range.endInclusive, smoothScroll) }
    }

    private fun onYAxisChanged(minY: Float, maxY: Float, smoothScroll: Boolean) {
        onYAxisChangedListeners.forEach { it(minY, maxY, smoothScroll) }
    }

    fun onTouchEvent(event: MotionEvent) = when (event.actionMasked) {
        MotionEvent.ACTION_DOWN -> {
            lastMotionX = event.x
            velocityTracker = VelocityTracker.obtain()
            velocityTracker?.addMovement(event)
            true
        }
        MotionEvent.ACTION_MOVE -> {
            scrollGraph(event.x)
            velocityTracker?.addMovement(event)
            true
        }
        MotionEvent.ACTION_CANCEL -> {
            velocityTracker?.recycle()
            velocityTracker = null
            true
        }
        MotionEvent.ACTION_UP -> {
            velocityTracker?.apply {
                addMovement(event)
                computeCurrentVelocity(VELOCITY_UNITS)
                scrollGraph(lastMotionX + xVelocity, smoothScroll = true)
            }
            velocityTracker?.recycle()
            velocityTracker = null
            true
        }
        else -> false
    }

    private fun scrollGraph(motionX: Float, smoothScroll: Boolean = false) {
        val graphWidth = (viewSize.width.toFloat() / range.distance).toInt()
        val increment = (lastMotionX - motionX).pxToAbscissa(graphWidth, 0f, 1f)
        val (start, endInclusive) = when {
            range.start + increment < 0 -> 0f to range.endInclusive - range.start
            range.endInclusive + increment > 1 -> range.start + 1 - range.endInclusive to 1f
            else -> range.start + increment to range.endInclusive + increment
        }
        lastMotionX = motionX
        setRange(FloatRange(start, endInclusive), smoothScroll)
    }

    fun onMeasure(size: Size) {
        viewSize = size
    }

    fun onRestoreInstanceState(range: FloatRange?, lineWidth: Float?) {
        if (this.range == range && paint.strokeWidth == lineWidth) return

        lineWidth?.let(paint::setStrokeWidth)
        val isAnimate = range == this.range
        updateAxis(newRange = range ?: this.range, isAnimate = isAnimate)
    }

    fun drawLines(canvas: Canvas) {
        canvas.drawCurveLines(paint)
    }

    private fun Canvas.drawCurveLines(paint: Paint) {
        animatingLines.forEach { line ->
            path.rewind()
            paint.color = line.animatingColor
            line.drawPixelPoints.forEachIndexed { index, point ->
                when (index) {
                    0 -> path.moveTo(point.x, point.y)
                    else -> path.lineTo(point.x, point.y)
                }
            }
            drawPath(path, paint)
        }
    }

    private fun transformAxis(points: List<PointF>, interpolatedRange: FloatRange): List<PointF> {
        val interpolatedPoints = mutableListOf<PointF>()
        points.forEach { point ->
            if (interpolatedRange.contains(point.x)) {
                interpolatedPoints.add(point.toPixelPoint(interpolatedRange))
            }
        }
        return interpolatedPoints.apply { addAbscissaBoundaries(points, interpolatedRange) }
    }

    private fun MutableList<PointF>.addAbscissaBoundaries(
        valuePoints: List<PointF>,
        interpolatedRange: FloatRange
    ) {
        val (leftBoundary, rightBoundary) = valuePoints.getAbscissaBoundaries(interpolatedRange)
        leftBoundary?.let { add(0, it.toPixelPoint(interpolatedRange)) }
        rightBoundary?.let { add(it.toPixelPoint(interpolatedRange)) }
    }

    private fun PointF.toPixelPoint(interpolatedRange: FloatRange): PointF {
        val x = x.abscissaToPx(
            viewSize.width,
            interpolatedRange.start,
            interpolatedRange.endInclusive
        )
        val y = y.ordinateToPx(
            viewSize.height,
            currentMinY,
            currentMaxY
        )
        return PointF(x, y)
    }

    private companion object {
        const val VELOCITY_UNITS = 200
    }
}
