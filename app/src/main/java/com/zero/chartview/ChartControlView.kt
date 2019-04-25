package com.zero.chartview

import android.content.Context
import android.content.res.TypedArray
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.widget.FrameLayout
import com.zero.chartview.model.CurveLine
import com.zero.chartview.utils.findMaxYValue
import com.zero.chartview.utils.findMinYValue

class ChartControlView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes), Themeable {

    private lateinit var themeColor: Themeable.ThemeColor

    private val graph: GraphicView = GraphicView(context, attrs, defStyleAttr, defStyleRes)
    private var selectorView = SelectorView(context, attrs, defStyleAttr, defStyleRes)

    init {
        addView(graph)
        addView(selectorView)

        val typedArray =
            context.theme.obtainStyledAttributes(attrs, R.styleable.ChartControlView, defStyleAttr, defStyleRes)
        val themeDefault = getThemeColorDefault(typedArray)
        typedArray.recycle()
        setThemeColor(themeDefault)
    }

    fun setRange(start: Float, endInclusive: Float) {
        selectorView.setRange(start, endInclusive)
    }

    fun getLiveDataRange() = selectorView.getRange()

    fun setLines(lines: List<CurveLine>) {
        graph.setLines(lines)
        updateAxis(lines)
    }

    fun addLine(line: CurveLine) {
        val lines = graph.getLines()
        graph.addLine(line)
        updateAxis(lines + line)
    }

    fun removeLine(line: CurveLine) {
        val lines = graph.getLines()
        graph.removeLine(line)
        updateAxis(lines - line)
    }

    private fun updateAxis(lines: List<CurveLine>) {
        val maxY = findMaxYValue(lines)
        val minY = findMinYValue(lines)
        //TODO setMinMaxX
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
        selectorView.setFrameControlColor(themeColor.colorFrameControl)
    }

    fun setFogControlColor(@ColorInt fogControlColor: Int) {
        themeColor.colorFogControl = fogControlColor
        selectorView.setFogControlColor(themeColor.colorFogControl)
    }

    private fun onThemeColorChanged() {
        super.setBackgroundColor(themeColor.colorBackground)
        selectorView.setThemeColor(themeColor.colorFrameControl, themeColor.colorFogControl)
    }

    private fun getThemeColorDefault(typedArray: TypedArray): Themeable.ThemeColor {
        typedArray.apply {
            val colorBackground =
                getColor(R.styleable.ChartControlView_colorBackground, resources.getColor(R.color.colorBackground))
            val colorFrameControl =
                getColor(R.styleable.ChartControlView_colorFrameControl, resources.getColor(R.color.colorFrameControl))
            val colorFogControl =
                getColor(R.styleable.ChartControlView_colorFogControl, resources.getColor(R.color.colorFogControl))
            return Themeable.ThemeColor(
                colorBackground = colorBackground,
                colorFrameControl = colorFrameControl,
                colorFogControl = colorFogControl
            )
        }
    }
}