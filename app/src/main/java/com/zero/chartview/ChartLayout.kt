package com.zero.chartview

import android.content.Context
import android.content.res.TypedArray
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.zero.chartview.model.CurveLine

class ChartLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes), Themeable {

    private val chartLayoutView = LayoutInflater.from(context).inflate(R.layout.chart_layout_view, this, false)
    private val titleView: TextView
    private val lineNameContainer: LinearLayout
    private val chartContainer: LinearLayout
    private val lineNameViews = mutableListOf<View>()

    private var chart: ChartView? = null
    private var control: ChartControlView? = null

    private var animationOut = AnimationUtils.loadAnimation(context, android.R.anim.slide_out_right)
    private var animationIn = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)

    private lateinit var themeColor: Themeable.ThemeColor

    init {
        addView(chartLayoutView)

        titleView = chartLayoutView.findViewById(R.id.title)
        chartContainer = chartLayoutView.findViewById(R.id.chartContainer)
        lineNameContainer = chartLayoutView.findViewById(R.id.lineNameContainer)
        super.setBackgroundColor(resources.getColor(android.R.color.transparent))

        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.ChartLayout, defStyleAttr, defStyleRes)
        val themeDefault = getThemeColorDefault(typedArray)
        typedArray.recycle()
        setThemeColor(themeDefault)
    }

    override fun addView(child: View) {
        if (child is ChartView) {
            chart = child
            child.addLinesChangedInvoker { lines ->
                control?.setLines(lines)
            }
        } else if (child is ChartControlView) {
            control = child
            child.addRangeChangedInvoker { range ->
                chart?.setRange(range.start, range.endInclusive)
            }
        } else {
            super.addView(child)
        }
    }

    fun setRange(start: Float, endInclusive: Float) {
        control?.setRange(start, endInclusive)
    }

    fun setLines(lines: List<CurveLine>, correspondingLegends: Map<Float, String>? = null) {
        chart?.setLines(lines, correspondingLegends)
        //TODO lineNameContainer
    }

    fun addLine(line: CurveLine, correspondingLegends: Map<Float, String>? = null) {
        chart?.addLine(line, correspondingLegends)
        //TODO lineNameContainer
    }

    fun removeLine(index: Int) {
        chart?.removeLine(index)
        //TODO lineNameContainer
    }

    fun removeLine(line: CurveLine) {
        chart?.removeLine(line)
        //TODO lineNameContainer
    }

    override fun setBackgroundColor(color: Int) {
        chartLayoutView.setBackgroundColor(color)
    }

    fun setTitleText(text: String) {
        titleView.text = text
    }

    fun setTitleColor(@ColorInt color: Int?) {
        if (color != null && color != themeColor.colorTitle) {
            themeColor.colorTitle = color
            titleView.setTextColor(color)
        }
    }

    fun setLabelColor(@ColorInt color: Int?) {
        if (color != null && color != themeColor.colorLabel) {
            themeColor.colorLabel = color
            //TODO lineNameContainer
        }
    }

    override fun getThemeColor(): Themeable.ThemeColor {
        val chartColors = chart?.getThemeColor()
        val controlColors = control?.getThemeColor()
        chartColors?.apply {
            themeColor.colorLegend = colorLegend
            themeColor.colorGrid = colorGrid
            themeColor.colorPopupLine = colorPopupLine
        }
        controlColors?.apply {
            themeColor.colorFrameControl = colorFrameControl
            themeColor.colorFogControl = colorFogControl
        }
        return themeColor
    }

    override fun setThemeColor(colors: Themeable.ThemeColor) {
        themeColor = colors
        setTitleColor(themeColor.colorTitle)
        setLabelColor(themeColor.colorLabel)
        chart?.setThemeColor(colors)
        control?.setThemeColor(colors)
        val colorBackground = colors.colorBackground
        if (colorBackground != null) {
            setBackgroundColor(colorBackground)
        }
    }

    private fun getThemeColorDefault(typedArray: TypedArray): Themeable.ThemeColor {
        typedArray.apply {
            val colorBackground =
                getColor(R.styleable.ChartLayout_colorBackground, resources.getColor(R.color.colorBackground))
            val colorTitle =
                getColor(R.styleable.ChartLayout_colorTitle, resources.getColor(R.color.colorTitle))
            val colorLabel =
                getColor(R.styleable.ChartLayout_colorLabel, resources.getColor(R.color.colorLabel))
            return Themeable.ThemeColor(
                colorBackground = colorBackground,
                colorTitle = colorTitle,
                colorLabel = colorLabel
            )
        }
    }
}