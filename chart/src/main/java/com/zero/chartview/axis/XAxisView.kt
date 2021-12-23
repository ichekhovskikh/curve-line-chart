package com.zero.chartview.axis

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.annotation.ColorInt
import android.util.AttributeSet
import android.view.View
import com.zero.chartview.R
import com.zero.chartview.extensions.applyStyledAttributes
import com.zero.chartview.extensions.interpolateByValues
import com.zero.chartview.extensions.distance
import com.zero.chartview.model.FloatRange
import com.zero.chartview.model.PercentRange
import com.zero.chartview.tools.xValueToPixel
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log2
import kotlin.math.pow

internal class XAxisView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val legendPaint = Paint().apply {
        textSize = resources.getDimension(R.dimen.legend_text_size_default)
    }

    @ColorInt private var legendColor = resources.getColor(R.color.colorLegend)

    private var textMarginTop = resources.getDimension(R.dimen.abscissa_legend_margin_top_default)
    private var textLength = resources.getInteger(R.integer.abscissa_legend_label_length_default)
    private var legendCount = resources.getInteger(R.integer.abscissa_legend_count_default)

    private lateinit var correspondingLegends: Map<Float, String>
    private var abscissas: List<Float> = emptyList()
    private var range: FloatRange = FloatRange(0F, 1F)

    internal val legendTextHeightUsed
        get() = (legendPaint.textSize + textMarginTop).toInt()

    init {
        applyStyledAttributes(attrs, R.styleable.XAxisView, defStyleAttr, defStyleRes) {
            legendPaint.textSize = getDimension(R.styleable.XAxisView_legendTextSize, legendPaint.textSize)
            textMarginTop = getDimension(R.styleable.XAxisView_abscissaLegendMarginTop, textMarginTop)
            legendCount = getInteger(R.styleable.XAxisView_abscissaLegendCount, legendCount)
        }
    }

    fun setAbscissas(abscissas: List<Float>) {
        this.abscissas = abscissas.sorted()
        postInvalidateOnAnimation()
    }

    fun setCorrespondingLegends(correspondingLegends: Map<Float, String>) {
        this.correspondingLegends = correspondingLegends
    }

    fun setRange(start: Float, endInclusive: Float, smoothScroll: Boolean) {
        // todo add smooth scroll
        range = PercentRange(start, endInclusive)
        postInvalidateOnAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        if (abscissas.isEmpty()) return
        val interpolatedRange = range.interpolateByValues(abscissas)
        val step = calculateStep(interpolatedRange)
        val positions = getDrawPositions(step)
        positions.forEachIndexed { index, position ->
            val averageCoordinate = getCorrespondingWithBias(position, step)
            val legendText = getLegendText(averageCoordinate)
            val textHalfWidth = legendPaint.measureText(legendText) / 2
            val pixel = xValueToPixel(position - textHalfWidth, measuredWidth, interpolatedRange.start, interpolatedRange.endInclusive)
            if (index % 2 != 0) {
                legendPaint.color = getTransparencyColor(legendColor, interpolatedRange)
            } else {
                legendPaint.color = legendColor
            }
            canvas.drawText(legendText, pixel, measuredHeight.toFloat(), legendPaint)
        }
    }

    private fun getDrawPositions(step: Float): List<Float> {
        val positions = mutableListOf<Float>()
        var position: Float = abscissas.first()
        while (position <= abscissas.last()) {
            positions.add(position)
            position += step
        }
        return positions
    }

    private fun getCorrespondingWithBias(drawPosition: Float, step: Float): Float? {
        val halfStep = step / 2
        val list = abscissas.filter { it in (drawPosition - halfStep)..(drawPosition + halfStep) }
        return list.getOrNull(list.size / 2)
    }

    private fun calculateStep(interpolatedRange: FloatRange): Float {
        val exponent = log2((abscissas.last() - abscissas.first()) / interpolatedRange.distance)
        return (fullRangeStep() / 2.0.pow(floor(exponent).toDouble())).toFloat()
    }

    private fun fullRangeStep() = (abscissas.last() - abscissas.first()) / (legendCount * 2)

    private fun getTransparencyColor(color: Int, interpolatedRange: FloatRange): Int {
        val currentExponent = log2((abscissas.last() - abscissas.first()) / interpolatedRange.distance)
        val weight = abs(floor(currentExponent) - currentExponent)
        return Color.argb((255 * weight).toInt(), Color.red(color), Color.green(color), Color.blue(color))
    }

    private fun getLegendText(averageCoordinate: Float?): String {
        var legendText = correspondingLegends[averageCoordinate] ?: ""
        if (legendText.length > textLength) {
            legendText = legendText.substring(0, textLength)
        }
        return legendText
    }

    fun setLegendColor(@ColorInt legendColor: Int) {
        if (legendColor != this.legendColor) {
            this.legendColor = legendColor
        }
    }
}
