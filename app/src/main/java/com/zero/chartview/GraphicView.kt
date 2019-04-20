package com.zero.chartview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.zero.chartview.model.AnimatingCurveLine
import com.zero.chartview.model.CurveLine
import com.zero.chartview.model.FloatRange
import com.zero.chartview.service.AnimationLineService
import com.zero.chartview.utils.findMaxXValue
import com.zero.chartview.utils.findMinXValue
import javax.inject.Inject

class GraphicView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val paint: Paint
    private val path: Path

    var range: FloatRange private set

    @Inject
    lateinit var animationLineService: AnimationLineService
        protected set

    init {
        App.appComponent.inject(this)
        animationLineService.onInvalidate = ::invalidate
        range = FloatRange(0F, 0F)
        path = Path()
        paint = Paint()
    }

    fun getLines() = animationLineService.lines.map { it.curveLine }

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

    fun setYAxis(minY: Float, maxY: Float) {
        animationLineService.setYAxis(minY, maxY)
    }

    fun setRange(start: Float, endInclusive: Float) {
        range.start = start
        range.endInclusive = endInclusive
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        initializeRangeIfRequired()
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

    private fun initializeRangeIfRequired() {
        if (range.isEmpty()) {
            val curveLines = animationLineService.lines.map { it.curveLine }
            setRange(findMinXValue(curveLines), findMaxXValue(curveLines))
        }
    }

    private fun transformAxis(points: List<PointF>): List<PointF> {
        val transformPoints = mutableListOf<PointF>()
        val coefficientY = measuredHeight.toFloat() / (getMaxY() - getMinY())
        val coefficientX = measuredWidth.toFloat() / (range.endInclusive - range.start)
        points.forEach { point ->
            if (range.contains(point.x)) {
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
