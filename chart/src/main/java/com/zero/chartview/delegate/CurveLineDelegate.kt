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

    val lines get() = animatingLines.map { it.curveLine }

    private var onYAxisChangedListener: ((minY: Float, maxY: Float) -> Unit)? = null

    private val valueRange get() = range.asValueRange(lines.abscissas)

    private val path = Path()

    private val animatingLines = mutableListOf<AnimatingCurveLine>()

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
        updateAxis(newLines = lines)
    }

    fun addLine(line: CurveLine) {
        val current = lines
        if (current.contains(line)) return

        appearanceAnimator.cancel()
        animatingLines.add(AppearingCurveLine(line))
        appearanceAnimator.start()
        updateAxis(newLines = lines)
    }

    fun removeLine(line: CurveLine) {
        val current = lines
        if (!current.contains(line)) return

        appearanceAnimator.cancel()
        animatingLines.find { it.curveLine == line }?.setDisappearing()
        appearanceAnimator.start()
        updateAxis(newLines = lines)
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

    fun drawLines(canvas: Canvas, paint: Paint, window: Size) {
        if (animatingLines.isEmpty()) return
        val valueRange = valueRange

        animatingLines.forEach { line ->
            path.rewind()
            paint.color = line.animatingColor
            val transformPoints = transformAxis(line.curveLine.points, valueRange, window)
            transformPoints.forEachIndexed { index, point ->
                when (index) {
                    0 -> path.moveTo(point.x, point.y)
                    else -> path.lineTo(point.x, point.y)
                }
            }
            canvas.drawPath(path, paint)
        }
    }

    private fun transformAxis(
        points: List<PointF>,
        valueRange: FloatRange,
        window: Size
    ): List<PointF> {
        val transformedPoints = mutableListOf<PointF>()
        points.forEach { point ->
            if (valueRange.contains(point.x)) {
                transformedPoints.add(point.toPixelPoint(valueRange, window))
            }
        }
        return transformedPoints.apply { addAbscissaBoundaries(points, valueRange, window) }
    }

    private fun MutableList<PointF>.addAbscissaBoundaries(
        valuePoints: List<PointF>,
        valueRange: FloatRange,
        window: Size
    ) {
        val (leftBoundary, rightBoundary) = valuePoints.getAbscissaBoundaries(valueRange)
        leftBoundary?.let { add(0, it.toPixelPoint(valueRange, window)) }
        rightBoundary?.let { add(it.toPixelPoint(valueRange, window)) }
    }

    private fun PointF.toPixelPoint(valueRange: FloatRange, window: Size): PointF {
        val x = xValueToPixel(x, window.width, valueRange.start, valueRange.endInclusive)
        val y = yValueToPixel(y, window.height, minY, maxY)
        return PointF(x, y)
    }
}