package com.zero.chartview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.zero.chartview.model.AnimatingCurveLine
import com.zero.chartview.model.CurveLine
import com.zero.chartview.model.FloatRange
import com.zero.chartview.service.AnimationLineService

class GraphicView constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        private const val ANIMATION_DURATION_MS = 300L
    }

    private val animationLineService: AnimationLineService = AnimationLineService(ANIMATION_DURATION_MS, ::invalidate)

    private val paint = Paint()
    private val path = Path()

    private var range: FloatRange = FloatRange(0F, measuredWidth.toFloat())


    fun getMaxY() = animationLineService.maxY

    fun getMinY() = animationLineService.minY

    fun setLines(lines: List<CurveLine>) {
        animationLineService.setLines(lines)
    }

    fun addLine(line: CurveLine) {
        animationLineService.addLine(line)
    }

    fun removeLine(line: CurveLine) {
        animationLineService.removeLine(line)
    }

    fun setRange(range: FloatRange) {
        this.range = range
        invalidate()
    }

    fun getRange() = range

    override fun onDraw(canvas: Canvas) {
        val lines = animationLineService.lines
        lines.forEach { line ->
            path.rewind()
            paint.color = lineTransparencyColor(line)
            val transformPoints = transformAxis(line.curveLine.points)
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

    private fun transformAxis(points: List<PointF>): List<PointF> {
        val transformPoints = mutableListOf<PointF>()
        val coefficientY = measuredHeight.toFloat() / (getMaxY() - getMinY())
        val coefficientX = measuredWidth.toFloat() / (range.endInclusive - range.start)
        points.forEach { point ->
            if(range.contains(point.x)) {
                val x = (point.x - range.start) * coefficientX
                val y = measuredHeight - ((point.y - getMinY()) * coefficientY)
                transformPoints.add(PointF(x, y))
            }
        }
        return transformPoints
    }

    private fun lineTransparencyColor(line: AnimatingCurveLine): Int {
        val color = line.curveLine.color
        return Color.argb(lineTransparency(line), Color.red(color), Color.green(color), Color.blue(color))
    }

    private fun lineTransparency(line: AnimatingCurveLine) =
        (255 * if (line.isAppearing) line.animationValue else 1 - line.animationValue).toInt()
}
