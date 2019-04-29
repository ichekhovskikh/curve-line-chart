package com.zero.chartview

import android.content.Context
import android.content.res.TypedArray
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.widget.FrameLayout
import com.zero.chartview.model.CurveLine
import com.zero.chartview.model.FloatRange
import com.zero.chartview.utils.findMinMaxYValueRanged

class ChartSelectorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes), Themeable {

    private val graph: GraphicsView = GraphicsView(context, attrs, defStyleAttr, defStyleRes)
    private var scrollFrame = ScrollFrameView(context, attrs, defStyleAttr, defStyleRes)

    private lateinit var themeColor: Themeable.ThemeColor

    init {
        addView(graph)
        addView(scrollFrame)

        val typedArray =
            context.theme.obtainStyledAttributes(attrs, R.styleable.ChartSelectorView, defStyleAttr, defStyleRes)
        val themeDefault = getThemeColorDefault(typedArray)
        typedArray.recycle()
        setThemeColor(themeDefault)
    }

    fun setRange(start: Float, endInclusive: Float) {
        scrollFrame.setRange(start, endInclusive)
    }

    fun addRangeChangedInvoker(invoker: (FloatRange) -> Unit) {
        scrollFrame.addRangeChangedInvoker(invoker)
    }

    fun removeRangeChangedInvoker(invoker: (FloatRange) -> Unit) {
        scrollFrame.removeRangeChangedInvoker(invoker)
    }

    fun setLines(lines: List<CurveLine>) {
        graph.setLines(lines)
        updateAxis(lines)
    }

    fun addLine(line: CurveLine) {
        val lines = graph.getLines()
        graph.addLine(line)
        updateAxis(lines + line)
    }

    fun removeLine(index: Int) {
        val lines = graph.getLines()
        removeLine(lines[index])
    }

    fun removeLine(line: CurveLine) {
        val lines = graph.getLines()
        graph.removeLine(line)
        updateAxis(lines - line)
    }

    private fun updateAxis(lines: List<CurveLine>) {
        val (minY, maxY) = findMinMaxYValueRanged(lines, graph.range)
        graph.setYAxis(minY, maxY)
    }

    override fun getThemeColor() = themeColor

    override fun setThemeColor(colors: Themeable.ThemeColor) {
        themeColor = colors
        onThemeColorChanged()
    }

    override fun setBackgroundColor(@ColorInt backgroundColor: Int) {
        themeColor.colorBackground = backgroundColor
        super.setBackgroundColor(backgroundColor)
    }

    fun setFrameControlColor(@ColorInt frameControlColor: Int) {
        themeColor.colorFrameControl = frameControlColor
        scrollFrame.setFrameControlColor(themeColor.colorFrameControl)
        scrollFrame.invalidate()
    }

    fun setFogControlColor(@ColorInt fogControlColor: Int) {
        themeColor.colorFogControl = fogControlColor
        scrollFrame.setFogControlColor(themeColor.colorFogControl)
        scrollFrame.invalidate()
    }

    private fun onThemeColorChanged() {
        scrollFrame.setFrameControlColor(themeColor.colorFrameControl)
        scrollFrame.setFogControlColor(themeColor.colorFogControl)
        val colorBackground = themeColor.colorBackground
        if (colorBackground != null) {
            super.setBackgroundColor(colorBackground)
        }
        scrollFrame.invalidate()
    }

    private fun getThemeColorDefault(typedArray: TypedArray): Themeable.ThemeColor {
        typedArray.apply {
            val colorBackground =
                getColor(R.styleable.ChartSelectorView_colorBackground, resources.getColor(R.color.colorBackground))
            val colorFrameControl =
                getColor(R.styleable.ChartSelectorView_colorFrameControl, resources.getColor(R.color.colorFrameControl))
            val colorFogControl =
                getColor(R.styleable.ChartSelectorView_colorFogControl, resources.getColor(R.color.colorFogControl))
            return Themeable.ThemeColor(
                colorBackground = colorBackground,
                colorFrameControl = colorFrameControl,
                colorFogControl = colorFogControl
            )
        }
    }
}