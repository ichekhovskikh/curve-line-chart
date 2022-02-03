package com.zero.chartview.delegate

import android.graphics.*
import android.view.MotionEvent
import android.view.VelocityTracker
import androidx.annotation.Px
import com.zero.chartview.anim.AppearanceAnimator
import com.zero.chartview.anim.AxisAnimator
import com.zero.chartview.extensions.*
import com.zero.chartview.model.*
import com.zero.chartview.model.AnimatingCurveLine
import com.zero.chartview.model.AppearingCurveLine
import com.zero.chartview.model.Size
import com.zero.chartview.tools.xPixelToValue
import com.zero.chartview.tools.xValueToPixel
import com.zero.chartview.tools.yValueToPixel

internal class CurveLineGraphDelegate(
    internal val paint: Paint,
    private val onUpdate: () -> Unit
) {

    private var currentMaxY = 0f
    private var currentMinY = 0f
    private var maxYAfterAnimate = 0f
    private var minYAfterAnimate = 0f
    private var viewSize = Size()

    internal var range = BinaryRange()
        private set

    internal val linesAfterAnimate get() = animatingLines
        .filter { it.isAppearing }
        .map { it.curveLine }

    private val path = Path()
    private var currentLines = emptyList<CurveLine>()
    private val animatingLines = mutableListOf<AnimatingCurveLine>()

    private var onYAxisChangedListener: ((minY: Float, maxY: Float) -> Unit)? = null
    private val onLinesChangedListeners = mutableListOf<(List<CurveLine>) -> Unit>()
    private val onRangeChangedListeners = mutableListOf<(start: Float, endInclusive: Float, smoothScroll: Boolean) -> Unit>()

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
        val interpolatedRange = FloatRange(startX, endX)
        animatingLines.forEach { line ->
            line.interpolatedPoints = transformAxis(line.curveLine.points, interpolatedRange)
        }
        onUpdate()
    }

    private fun removeDisappearingLines() {
        animatingLines.removeAll { !it.isAppearing && it.animationValue == 1f }
    }

    fun setRange(range: FloatRange, smoothScroll: Boolean = false) {
        if (this.range == range) return
        if (!smoothScroll) {
            this.range = range
        }
        updateAxis(newRange = range)
        onRangeChanged(range, smoothScroll)
    }

    fun setLines(newLines: List<CurveLine>) {
        val current = linesAfterAnimate
        if (newLines == current) return

        appearanceAnimator.cancel()
        val appearing = newLines.minus(current)
        appearing.forEach { line -> animatingLines.add(AppearingCurveLine(line)) }

        val disappearing = current.minus(newLines)
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
        animatingLines.add(AppearingCurveLine(line))
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
        newRange: FloatRange = range
    ) {
        val (minY, maxY) = newLines.getMinMaxY(newRange)
        if (this.maxYAfterAnimate != maxY || this.minYAfterAnimate != minY) {
            maxYAfterAnimate = maxY
            minYAfterAnimate = minY
            onYAxisChangedListener?.invoke(minY, maxY)
        }
        axisAnimator.reStart(
            fromXRange = range.interpolateByValues(currentLines.abscissas),
            toXRange = newRange.interpolateByValues(newLines.abscissas),
            fromYRange = FloatRange(this.currentMinY, this.currentMaxY),
            toYRange = FloatRange(minY, maxY)
        )
        range = newRange
        currentLines = newLines
    }

    fun addOnLinesChangedListener(onLinesChangedListener: (List<CurveLine>) -> Unit) {
        onLinesChangedListeners.add(onLinesChangedListener)
    }

    fun removeOnLinesChangedListener(onLinesChangedListener: (List<CurveLine>) -> Unit) {
        onLinesChangedListeners.remove(onLinesChangedListener)
    }

    fun addOnRangeChangedListener(onRangeChangedListener: (start: Float, endInclusive: Float, smoothScroll: Boolean) -> Unit) {
        onRangeChangedListeners.add(onRangeChangedListener)
    }

    fun removeOnRangeChangedListener(onRangeChangedListener: (start: Float, endInclusive: Float, smoothScroll: Boolean) -> Unit) {
        onRangeChangedListeners.remove(onRangeChangedListener)
    }

    internal fun setOnYAxisChangedListener(onYAxisChangedListener: ((minY: Float, maxY: Float) -> Unit)?) {
        this.onYAxisChangedListener = onYAxisChangedListener
    }

    private fun onLinesChanged(lines: List<CurveLine>) {
        onLinesChangedListeners.forEach { it(lines) }
    }

    private fun onRangeChanged(range: FloatRange, smoothScroll: Boolean) {
        onRangeChangedListeners.forEach { it(range.start, range.endInclusive, smoothScroll) }
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

    private fun scrollGraph(abscissa: Float, smoothScroll: Boolean = false) {
        val graphWidth = (viewSize.width.toFloat() / range.distance).toInt()
        val increment = xPixelToValue(lastMotionX - abscissa, graphWidth, 0f, 1f)
        val (start, endInclusive) = when {
            range.start + increment < 0 -> 0f to range.endInclusive - range.start
            range.endInclusive + increment > 1 -> range.start + 1 - range.endInclusive to 1f
            else -> range.start + increment to range.endInclusive + increment
        }
        lastMotionX = abscissa
        setRange(FloatRange(start, endInclusive), smoothScroll)
    }

    fun onMeasure(size: Size) {
        viewSize = size
    }

    fun drawLines(canvas: Canvas) {
        canvas.drawCurveLines(paint)
    }

    private fun Canvas.drawCurveLines(paint: Paint) {
        animatingLines.forEach { line ->
            path.rewind()
            paint.color = line.animatingColor
            line.interpolatedPoints.forEachIndexed { index, point ->
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
        val x = xValueToPixel(x, viewSize.width, interpolatedRange.start, interpolatedRange.endInclusive)
        val y = yValueToPixel(y, viewSize.height, currentMinY, currentMaxY)
        return PointF(x, y)
    }

    private companion object {
        const val VELOCITY_UNITS = 200
    }
}
