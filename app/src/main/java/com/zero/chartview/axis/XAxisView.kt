package com.zero.chartview.axis

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.View
import com.zero.chartview.R
import com.zero.chartview.model.FloatRange
import com.zero.chartview.utils.formatLegend
import com.zero.chartview.utils.xValueToPixel
import kotlin.math.floor
import kotlin.math.log2

class XAxisView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val legendPaint = Paint()

    private var textMarginTop = resources.getDimension(R.dimen.abscissa_legend_margin_top_default)
    private var textLength = resources.getInteger(R.integer.abscissa_legend_label_length_default)
    private var legendCount = resources.getInteger(R.integer.abscissa_legend_count_default)

    private lateinit var correspondingLegends: Map<Float, String>
    private lateinit var coordinates: List<Float>
    private var range: FloatRange = FloatRange(0F, 0F)

    init {
        var textSize = resources.getDimension(R.dimen.legend_text_size_default)
        context.theme.obtainStyledAttributes(attrs, R.styleable.XAxisView, defStyleAttr, defStyleRes).apply {
            textSize = getDimension(R.styleable.XAxisView_legendTextSize, textSize)
            textMarginTop = getDimension(R.styleable.XAxisView_abscissaLegendMarginTop, textMarginTop)
            legendCount = getInteger(R.styleable.XAxisView_abscissaLegendCount, legendCount)
            recycle()
        }
        legendPaint.textSize = textSize
    }

    fun getLegendWidth() = (legendPaint.textSize + textMarginTop).toInt()

    fun setCoordinates(coordinates: List<Float>) {
        this.coordinates = coordinates.sorted()
    }

    fun setCorrespondingLegends(correspondingLegends: Map<Float, String>) {
        this.correspondingLegends = correspondingLegends
    }

    fun setRange(start: Float, endInclusive: Float) {
        range.start = start
        range.endInclusive = endInclusive
        invalidate()
    }

    private fun initializeRangeIfRequired() {
        if (range.isEmpty()) {
            range.start = coordinates.first()
            range.endInclusive = coordinates.last()
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (!::coordinates.isInitialized) return
        initializeRangeIfRequired()
        val step = calculateStep()
        val positions = getDrawPositions(step)
        val color = legendPaint.color
        positions.forEachIndexed { index, position ->
            val averageCoordinate = getCorrespondingWithBias(position, step)
            val legendText = getLegendText(averageCoordinate)
            val textHalfWidth = legendPaint.measureText(legendText) / 2
            val pixel = xValueToPixel(position - textHalfWidth, measuredWidth, range.start, range.endInclusive)
            if (index % 2 != 0) {
                legendPaint.color = getTransparencyColor(color)
            } else {
                legendPaint.color = color
            }
            canvas.drawText(legendText, pixel, measuredHeight.toFloat(), legendPaint)
        }
    }

    private fun getDrawPositions(step: Float): List<Float> {
        val positions = mutableListOf<Float>()
        var position: Float = coordinates.first()
        while (position <= coordinates.last()) {
            positions.add(position)
            position += step
        }
        return positions
    }

    private fun getCorrespondingWithBias(drawPosition: Float, step: Float): Float? {
        val halfStep = step / 2
        val list = coordinates.filter { it in drawPosition - halfStep..drawPosition + halfStep }
        return if (list.isEmpty()) null else list[list.size / 2]
    }

    private fun calculateStep(): Float {
        val exponent = log2((coordinates.last() - coordinates.first()) / range.distance())
        return (fullRangeStep() / Math.pow(2.0, floor(exponent).toDouble())).toFloat()
    }

    private fun fullRangeStep() = (coordinates.last() - coordinates.first()) / (legendCount * 2)

    private fun getTransparencyColor(color: Int): Int {
        val currentExponent = log2((coordinates.last() - coordinates.first()) / range.distance())
        val weight = Math.abs(floor(currentExponent) - currentExponent)
        return Color.argb(255 * weight.toInt(), Color.red(color), Color.green(color), Color.blue(color))
    }

    private fun getLegendText(averageCoordinate: Float?): String {
        var legendText = correspondingLegends[averageCoordinate] ?: ""
        val numeric = legendText.toFloatOrNull()
        if (numeric != null) {
            legendText = formatLegend(numeric)
        } else if (legendText.length > textLength) {
            legendText.substring(0, textLength)
        }
        return legendText
    }

    fun onThemeChanged(@ColorInt colorLegend: Int) {
        legendPaint.color = colorLegend
    }
}