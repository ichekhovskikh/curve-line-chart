package com.chekh.chartview.delegate

import android.graphics.Canvas
import android.graphics.Paint
import android.view.MotionEvent
import androidx.annotation.Px
import com.chekh.chartview.extensions.getIntersections
import com.chekh.chartview.extensions.getMinMaxY
import com.chekh.chartview.extensions.interpolateByLineAbscissas
import com.chekh.chartview.extensions.isEqualsOrNull
import com.chekh.chartview.model.CurveLine
import com.chekh.chartview.model.PopupLine
import com.chekh.chartview.model.BinaryRange
import com.chekh.chartview.model.FloatRange
import com.chekh.chartview.model.IntersectionPoint
import com.chekh.chartview.model.Size
import com.chekh.chartview.tools.pxToAbscissa
import com.chekh.chartview.tools.abscissaToPx
import com.chekh.chartview.tools.ordinateToPx

@Suppress("LongParameterList", "TooManyFunctions")
internal class PopupLineDelegate(
    internal val linePaint: Paint,
    internal val pointInnerPaint: Paint,
    internal val pointOuterPaint: Paint,
    @Px private val pointInnerRadius: Float,
    @Px private val pointOuterRadius: Float,
    private val deltaTrackingTouchPercent: Float,
    private val onUpdate: () -> Unit
) {

    @Px
    internal var touchX: Float = NO_POSITION
    private var viewSize = Size()
    private var lines = emptyList<CurveLine>()
    private var popupLine: PopupLine? = null

    @Px
    internal val paddingVerticalUsed = pointOuterRadius

    internal var range = BinaryRange()
        private set

    private val intersections
        get() = popupLine?.intersections?.map { it.point }.orEmpty()

    private var onIntersectionsChanged: ((xPixel: Float?, intersections: List<IntersectionPoint>) -> Unit)? =
        null

    fun setRange(range: FloatRange) {
        this.range = range
        touchX = NO_POSITION
        onPopupLineChanged()
        onUpdate()
    }

    fun setLines(lines: List<CurveLine>) {
        this.lines = lines
        touchX = NO_POSITION
        onPopupLineChanged()
        onUpdate()
    }

    fun setOnIntersectionsChangedListener(
        onIntersectionsChangedListener: ((xPixel: Float?, intersections: List<IntersectionPoint>) -> Unit)?
    ) {
        onIntersectionsChanged = onIntersectionsChangedListener
        onIntersectionsChanged()
    }

    private fun onIntersectionsChanged() {
        onIntersectionsChanged?.invoke(popupLine?.xDrawPixel, intersections)
    }

    fun onTouchEvent(event: MotionEvent) = when (event.actionMasked) {
        MotionEvent.ACTION_DOWN -> {
            touchX = event.x
            onPopupLineChanged()
            onUpdate()
            false
        }
        MotionEvent.ACTION_MOVE -> {
            touchX = NO_POSITION
            onPopupLineChanged()
            onUpdate()
            false
        }
        else -> false
    }

    fun onMeasure(viewSize: Size) {
        this.viewSize = viewSize
    }

    private fun onPopupLineChanged() {
        popupLine = calculatePopupLine()
        onIntersectionsChanged()
    }

    @Suppress("ReturnCount")
    private fun calculatePopupLine(): PopupLine? {
        if (touchX == NO_POSITION) return null
        val (startValue, endValue) = range.interpolateByLineAbscissas(lines)
        val intersections = lines.getIntersections(touchX, startValue, endValue)
        val intersectionX = intersections.firstOrNull()?.x ?: return null
        val (minY, maxY) = lines.getMinMaxY(range)
        val xDrawPixel = intersectionX.abscissaToPx(viewSize.width, startValue, endValue)
        val availableHeight = (viewSize.height - 2 * paddingVerticalUsed).toInt()
        return PopupLine(
            xDrawPixel = xDrawPixel,
            intersections = intersections.map {
                PopupLine.Intersection(
                    point = it,
                    xDrawPixel = xDrawPixel,
                    yDrawPixel = it.y.ordinateToPx(
                        availableHeight,
                        minY,
                        maxY
                    ) + paddingVerticalUsed
                )
            }
        )
    }

    fun onRestoreInstanceState(
        touchX: Float?,
        range: FloatRange?,
        lineColor: Int?,
        pointInnerColor: Int?,
        lineWidth: Float?
    ) {
        @Suppress("ComplexCondition")
        if (this.touchX == touchX &&
            this.range == range &&
            linePaint.color == lineColor &&
            pointInnerPaint.color == pointInnerColor &&
            linePaint.strokeWidth == lineWidth
        ) return

        lineColor?.let(linePaint::setColor)
        pointInnerColor?.let(pointInnerPaint::setColor)
        lineWidth?.let(linePaint::setStrokeWidth)

        if (range == this.range && !touchX.isEqualsOrNull(this.touchX)) {
            this.range = range
            touchX?.let { this.touchX = it }
            onPopupLineChanged()
        }
        onUpdate()
    }

    fun drawPopupLine(canvas: Canvas) {
        val popupLine = popupLine ?: return
        canvas.drawPopupLine(popupLine)
        popupLine.intersections.forEach { canvas.drawIntersectionPoint(it) }
    }

    private fun Canvas.drawPopupLine(popupLine: PopupLine) {
        drawLine(
            popupLine.xDrawPixel,
            0f,
            popupLine.xDrawPixel,
            viewSize.height.toFloat(),
            linePaint
        )
    }

    private fun Canvas.drawIntersectionPoint(intersection: PopupLine.Intersection) {
        pointOuterPaint.color = intersection.point.lineColor
        drawCircle(
            intersection.xDrawPixel,
            intersection.yDrawPixel,
            pointOuterRadius,
            pointOuterPaint
        )
        drawCircle(
            intersection.xDrawPixel,
            intersection.yDrawPixel,
            pointInnerRadius,
            pointInnerPaint
        )
    }

    private fun List<CurveLine>.getIntersections(
        @Px xPixel: Float,
        minX: Float,
        maxX: Float
    ): List<IntersectionPoint> {
        val xValue = xPixel.pxToAbscissa(viewSize.width, minX, maxX)
        val delta = (maxX - minX) * deltaTrackingTouchPercent
        return this.getIntersections(xValue, delta)
    }

    internal companion object {
        const val NO_POSITION = -1f
    }
}
