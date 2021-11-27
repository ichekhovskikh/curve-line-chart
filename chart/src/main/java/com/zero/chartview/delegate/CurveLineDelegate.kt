package com.zero.chartview.delegate

import android.graphics.*
import com.zero.chartview.anim.AppearanceAnimator
import com.zero.chartview.anim.TensionAnimator
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
        set(value) {
            field = value
            onUpdate?.invoke()
        }

    val lines get() = animatingLines.map { it.curveLine }

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

    private val tensionAnimator = TensionAnimator { _, minY, maxY ->
        this.minY = minY
        this.maxY = maxY
        onUpdate?.invoke()
    }

    private fun removeDisappearingLines() {
        animatingLines.removeAll { !it.isAppearing }
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
    }

    fun addLine(line: CurveLine) {
        val current = lines
        if (current.contains(line)) return

        appearanceAnimator.cancel()
        animatingLines.add(AppearingCurveLine(line))
        appearanceAnimator.start()
    }

    fun removeLine(line: CurveLine) {
        val current = lines
        if (!current.contains(line)) return

        appearanceAnimator.cancel()
        animatingLines.find { it.curveLine == line }?.setDisappearing()
        appearanceAnimator.start()
    }

    fun setYAxis(minY: Float, maxY: Float) {
        if (this.maxY == maxY && this.minY == minY) return
        tensionAnimator.reStart(
            fromMin = this.minY,
            toMin = minY,
            fromMax = this.maxY,
            toMax = maxY
        )
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