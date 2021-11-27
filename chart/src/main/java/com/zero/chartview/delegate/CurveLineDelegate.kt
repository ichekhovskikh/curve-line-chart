package com.zero.chartview.delegate

import android.graphics.*
import com.zero.chartview.anim.AppearanceAnimator
import com.zero.chartview.anim.AxisAnimator
import com.zero.chartview.extensions.*
import com.zero.chartview.model.*
import com.zero.chartview.model.AnimatingCurveLine
import com.zero.chartview.model.AppearingCurveLine
import com.zero.chartview.model.Size
import com.zero.chartview.tools.xValueToPixel
import com.zero.chartview.tools.yValueToPixel

internal class CurveLineDelegate(var onUpdate: (() -> Unit)? = null) {

    var maxY = 0f
        private set

    var minY = 0f
        private set

    var range = FloatRange(0f, 1f)
        private set

    var viewSize = Size()
        set(value) {
            field = value
            onUpdate?.invoke()
        }

    val lines get() = animatingLines.map { it.curveLine }

    private val path = Path()

    private val animatingLines = mutableListOf<AnimatingCurveLine>()

    private var onYAxisChangedListener: ((minY: Float, maxY: Float) -> Unit)? = null

    private val appearanceAnimator = AppearanceAnimator { value ->
        animatingLines.forEach { line ->
            if (line.animationValue <= value) {
                line.animationValue = value
                onUpdate?.invoke()
            }
        }
    }.doOnEnd(::removeDisappearingLines)

    private val axisAnimator = AxisAnimator { startX, endX, startY, endY ->
        this.minY = startY
        this.maxY = endY
        this.range = FloatRange(startX, endX)

        val interpolatedRange = range.interpolateByValues(lines.abscissas)
        animatingLines.forEach { line ->
            line.interpolatedPoints = transformAxis(line.curveLine.points, interpolatedRange)
        }
        onUpdate?.invoke()
    }

    private fun removeDisappearingLines() {
        animatingLines.removeAll { !it.isAppearing }
    }

    fun setRange(range: FloatRange, smoothScroll: Boolean = false) {
        if (this.range == range) return
        if (!smoothScroll) {
            this.range = range
        }
        updateAxis(newRange = range)
    }

    fun setLines(newLines: List<CurveLine>) {
        val current = lines
        if (newLines == current) return

        appearanceAnimator.cancel()
        val appearing = newLines.minus(current)
        appearing.forEach { line -> animatingLines.add(AppearingCurveLine(line)) }

        val disappearing = current.minus(newLines)
        disappearing.forEach { line ->
            animatingLines.find { it.curveLine == line }?.setDisappearing()
        }
        appearanceAnimator.start()
        updateAxis(newLines = newLines)
    }

    fun addLine(line: CurveLine) {
        val current = lines
        if (current.contains(line)) return

        appearanceAnimator.cancel()
        animatingLines.add(AppearingCurveLine(line))
        appearanceAnimator.start()
        updateAxis(newLines = current + line)
    }

    fun removeLine(line: CurveLine) {
        val current = lines
        if (!current.contains(line)) return

        appearanceAnimator.cancel()
        animatingLines.find { it.curveLine == line }?.setDisappearing()
        appearanceAnimator.start()
        updateAxis(newLines = current - line)
    }

    private fun updateAxis(
        newLines: List<CurveLine> = lines,
        newRange: FloatRange = range
    ) {
        val (minY, maxY) = newLines.getMinMaxY(newRange)
        if (this.maxY != maxY || this.minY != minY) {
            onYAxisChangedListener?.invoke(minY, maxY)
        }
        axisAnimator.reStart(
            fromXRange = this.range,
            toXRange = newRange,
            fromYRange = FloatRange(this.minY, this.maxY),
            toYRange = FloatRange(minY, maxY)
        )
    }

    fun setOnYAxisChangedListener(onYAxisChangedListener: ((minY: Float, maxY: Float) -> Unit)?) {
        this.onYAxisChangedListener = onYAxisChangedListener
    }

    fun drawLines(canvas: Canvas, paint: Paint) {
        animatingLines.forEach { line ->
            path.rewind()
            paint.color = line.animatingColor
            line.interpolatedPoints.forEachIndexed { index, point ->
                when (index) {
                    0 -> path.moveTo(point.x, point.y)
                    else -> path.lineTo(point.x, point.y)
                }
            }
            canvas.drawPath(path, paint)
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
        val y = yValueToPixel(y, viewSize.height, minY, maxY)
        return PointF(x, y)
    }
}