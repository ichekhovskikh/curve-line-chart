package com.zero.chartview.popup

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import androidx.annotation.ColorInt
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.zero.chartview.R
import com.zero.chartview.axis.formatter.ShortAxisFormatter
import com.zero.chartview.extensions.interpolateByLineAbscissas
import com.zero.chartview.extensions.getMinMaxY
import com.zero.chartview.model.CurveLine
import com.zero.chartview.model.FloatRange
import com.zero.chartview.model.PercentRange
import com.zero.chartview.tools.*
import kotlin.math.abs

internal class PopupLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var pointInnerRadius = resources.getDimension(R.dimen.point_inner_radius)
    private var pointOuterRadius = resources.getDimension(R.dimen.point_outer_radius)
    private var pointColor = resources.getColor(R.color.colorBackground)

    private val linePaint = Paint()
    private val pointPaint = Paint()

    private val axisFormatter = ShortAxisFormatter()

    private var range = FloatRange(0F, 1F)

    private lateinit var lines: List<CurveLine>
    private lateinit var correspondingLegends: Map<Float, String>

    var popupWindow: PopupWindow? = null

    private var epsilonXPercent = resources.getFraction(R.fraction.epsilon_x_tracking_touch, 1, 1)
    private var dyStopTrackingTouch = resources.getDimension(R.dimen.dy_stop_tracking_touch)
    private var touchX: Float? = null
    private var touchY: Float = 0f

    init {
        linePaint.color = resources.getColor(R.color.colorPopupLine)
        linePaint.strokeWidth = resources.getDimension(R.dimen.popup_line_width)
        pointPaint.style = Paint.Style.FILL
    }

    fun setRange(start: Float, endInclusive: Float) {
        range = PercentRange(start, endInclusive)
        popupWindow?.visibility = GONE
        touchX = null
        postInvalidateOnAnimation()
    }

    fun setLines(lines: List<CurveLine>) {
        this.lines = lines
        popupWindow?.setLines(lines)
        popupWindow?.visibility = GONE
        touchX = null
        postInvalidateOnAnimation()
    }

    fun setCorrespondingLegends(correspondingLegends: Map<Float, String>) {
        this.correspondingLegends = correspondingLegends
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchX = event.x
                touchY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                popupWindow?.visibility = GONE
                touchX = null
            }
        }
        postInvalidateOnAnimation()
        return false
    }

    override fun onDraw(canvas: Canvas) {
        val x = touchX ?: return
        val (startValue, endValue) = range.interpolateByLineAbscissas(lines)
        val (minY, maxY) = lines.getMinMaxY(range)
        val intersectionPoints = getIntersectionPoint(x, startValue, endValue)
        if (intersectionPoints.isNotEmpty()) {
            val intersectionXValue = intersectionPoints.first().x
            val xDrawPixel = xValueToPixel(intersectionXValue, measuredWidth, startValue, endValue)
            canvas.drawLine(xDrawPixel, 0F, xDrawPixel, height.toFloat(), linePaint)
            intersectionPoints.forEach {
                val yDrawPixel = yValueToPixel(it.y, measuredHeight, minY, maxY)
                drawIntersectionPoint(canvas, xDrawPixel, yDrawPixel, it.color)
            }
            popupWindow?.fill(touchX, intersectionPoints)
        } else {
            popupWindow?.visibility = GONE
        }
    }

    private fun drawIntersectionPoint(canvas: Canvas, x: Float, y: Float, outerColor: Int) {
        pointPaint.color = outerColor
        canvas.drawCircle(x, y, pointOuterRadius, pointPaint)
        pointPaint.color = pointColor
        canvas.drawCircle(x, y, pointInnerRadius, pointPaint)
    }

    private fun getIntersectionPoint(xPixel: Float, minX: Float, maxX: Float): List<ChartPoint> {
        val xValue = xPixelToValue(xPixel, measuredWidth, minX, maxX)
        val points = mutableListOf<ChartPoint>()
        var minDistance = (maxX - minX) * epsilonXPercent
        var nearestX: Float? = null
        lines.forEach { line ->
            line.points.forEach { point ->
                points.add(ChartPoint(line.name, line.color, point.x, point.y, getIntersectionLegend(point)))
                val distance = abs(xValue - point.x)
                if (distance <= minDistance) {
                    nearestX = point.x
                    minDistance = distance
                }
            }
        }
        return points.filter { it.x == nearestX }
    }

    private fun getIntersectionLegend(point: PointF) =
        if (::correspondingLegends.isInitialized) correspondingLegends[point.x] ?: ""
        else axisFormatter.format(point.x, 1f)

    fun setPointColor(@ColorInt pointColor: Int) {
        if (pointColor != this.pointColor) {
            this.pointColor = pointColor
        }
    }

    fun setPopupLineColor(@ColorInt popupLineColor: Int) {
        if (popupLineColor != linePaint.color) {
            linePaint.color = popupLineColor
        }
    }

    data class ChartPoint(
        var name: String,
        var color: Int,
        var x: Float,
        var y: Float,
        var correspondingLegend: String
    )
}
