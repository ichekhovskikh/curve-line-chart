package com.zero.chartview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.zero.chartview.extensions.abscissas
import com.zero.chartview.extensions.asValueRange
import com.zero.chartview.extensions.contains
import com.zero.chartview.extensions.getAbscissaBoundaries
import com.zero.chartview.model.AnimatingCurveLine
import com.zero.chartview.model.CurveLine
import com.zero.chartview.model.FloatRange
import com.zero.chartview.model.PercentRange
import com.zero.chartview.service.AnimationLineService
import com.zero.chartview.tools.*

internal class GraphicsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val paint = Paint()
    private val path = Path()

    var range = FloatRange(0F, 1F)
        private set

    var animationLineService = AnimationLineService(::invalidate)
        private set

    init {
        var lineWidth = resources.getDimensionPixelSize(R.dimen.line_width_default)
        context.theme.obtainStyledAttributes(attrs, R.styleable.GraphicsView, defStyleAttr, defStyleRes).apply {
            lineWidth = getDimensionPixelSize(R.styleable.GraphicsView_lineWidth, lineWidth)
            recycle()
        }
        initializePaint(lineWidth)
    }

    private fun initializePaint(lineWidth: Int) {
        paint.apply {
            style = Paint.Style.STROKE
            strokeWidth = lineWidth.toFloat()
            isAntiAlias = true
        }
    }

    fun getLines() = animationLineService.getLines()

    fun setLines(lines: List<CurveLine>) {
        animationLineService.setLines(lines)
    }

    fun addLine(line: CurveLine) {
        animationLineService.addLine(line)
    }

    fun removeLine(line: CurveLine) {
        animationLineService.removeLine(line)
    }

    fun setYAxis(minY: Float, maxY: Float) {
        animationLineService.setYAxis(minY, maxY)
    }

    fun setRange(start: Float, endInclusive: Float) {
        range = PercentRange(start, endInclusive)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val curveLines = animationLineService.getLines()
        if (curveLines.isEmpty()) return

        val abscissas = curveLines.abscissas
        val valueRange = range.asValueRange(abscissas)
        val lines = animationLineService.animationLines
        lines.forEach { line ->
            path.rewind()
            paint.color = getTransparencyColor(line)
            val transformPoints = transformAxis(line.curveLine.points, valueRange)
            transformPoints.forEachIndexed { index, point ->
                if (index == 0) {
                    path.moveTo(point.x, point.y)
                } else {
                    path.lineTo(point.x, point.y)
                }
            }
            canvas.drawPath(path, paint)
        }
    }

    private fun getMaxY() = animationLineService.maxY

    private fun getMinY() = animationLineService.minY

    private fun transformAxis(points: List<PointF>, valueRange: FloatRange): List<PointF> {
        val transformPoints = mutableListOf<PointF>()
        points.forEach { point ->
            if (valueRange.contains(point.x)) {
                val x = xValueToPixel(point.x, measuredWidth, valueRange.start, valueRange.endInclusive)
                val y = yValueToPixel(point.y, measuredHeight, getMinY(), getMaxY())
                transformPoints.add(PointF(x, y))
            }
        }
        addBoundaryPoints(transformPoints, points, valueRange)
        return transformPoints
    }

    private fun addBoundaryPoints(pixelPoints: MutableList<PointF>, valuePoints: List<PointF>, valueRange: FloatRange) {
        val (leftBoundary, rightBoundary) = valuePoints.getAbscissaBoundaries(valueRange)
        leftBoundary?.also {
            val x = xValueToPixel(it.x, measuredWidth, valueRange.start, valueRange.endInclusive)
            val y = yValueToPixel(it.y, measuredHeight, getMinY(), getMaxY())
            pixelPoints.add(0, PointF(x, y))
        }
        rightBoundary?.also {
            val x = xValueToPixel(it.x, measuredWidth, valueRange.start, valueRange.endInclusive)
            val y = yValueToPixel(it.y, measuredHeight, getMinY(), getMaxY())
            pixelPoints.add(PointF(x, y))
        }
    }

    private fun getTransparencyColor(line: AnimatingCurveLine): Int {
        val color = line.curveLine.color
        return Color.argb(lineTransparency(line), Color.red(color), Color.green(color), Color.blue(color))
    }

    private fun lineTransparency(line: AnimatingCurveLine) =
        (255 * if (line.isAppearing) line.animationValue else 1 - line.animationValue).toInt()
}
