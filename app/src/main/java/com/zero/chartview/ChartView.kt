package com.zero.chartview

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.zero.chartview.axis.YAxisView
import com.zero.chartview.model.CurveLine
import com.zero.chartview.service.AnimationThemeService
import com.zero.chartview.utils.findMaxYValue
import com.zero.chartview.utils.findMinYValue
import javax.inject.Inject

class ChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes), Themeable {

    private val graph: GraphicView = GraphicView(context, attrs, defStyleAttr, defStyleRes)
    private val yAxis: YAxisView = YAxisView(context, attrs, defStyleAttr, defStyleRes)

    @Inject
    lateinit var animationThemeService: AnimationThemeService
        protected set

    init {
        addView(yAxis)
        addView(graph)

        App.appComponent.inject(this)
        animationThemeService.onInvalidate = ::onThemeChanged

        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.ChartView, defStyleAttr, defStyleRes)
        setLightStyles(typedArray)
        setDarkStyles(typedArray)
        val themeStyle = getThemeStyleDefault(typedArray)
        typedArray.recycle()
        updateTheme(themeStyle)
    }

    override lateinit var lightTheme: Themeable.ThemeColor

    override lateinit var darkTheme: Themeable.ThemeColor

    override lateinit var currentTheme: Themeable.ThemeColor

    override fun setTheme(colors: Themeable.ThemeColor) {
        if (::currentTheme.isInitialized) {
            animationThemeService.updateTheme(currentTheme, colors)
        } else {
            onThemeChanged(colors)
        }
        currentTheme = colors
    }

    override fun addView(child: View?) {
        if (child is ChartControlView) {
            //TODO Observe ChartControlView
        }
        super.addView(child)
    }

    fun setRange(start: Float, endInclusive: Float) {
        graph.setRange(start, endInclusive)
    }

    fun setLines(lines: List<CurveLine>) {
        graph.setLines(lines)
        updateYAxis(lines)
    }

    fun addLine(line: CurveLine) {
        val lines = graph.getLines()
        graph.addLine(line)
        updateYAxis(lines + line)
    }

    fun removeLine(line: CurveLine) {
        val lines = graph.getLines()
        graph.removeLine(line)
        updateYAxis(lines - line)
    }

    private fun updateYAxis(lines: List<CurveLine>) {
        val maxY = findMaxYValue(lines)
        val minY = findMinYValue(lines)
        graph.setYAxis(minY, maxY)
        yAxis.setYAxis(minY, maxY)
    }

    private fun getThemeStyleDefault(typedArray: TypedArray) =
        if (typedArray.getBoolean(R.styleable.ChartView_darkTheme, false)) Themeable.ThemeStyle.DARK
        else Themeable.ThemeStyle.LIGHT

    private fun onThemeChanged(colors: Themeable.ThemeColor) {
        yAxis.onThemeChanged(colors.colorLegend, colors.colorGrid)
        setBackgroundColor(colors.colorBackground)
        invalidate()
    }

    private fun setLightStyles(typedArray: TypedArray) {
        typedArray.apply {
            val lightColorBackground =
                getColor(R.styleable.ChartView_lightColorBackground, resources.getColor(R.color.colorBackground))
            val lightColorLegend =
                getColor(R.styleable.ChartView_lightColorLegend, resources.getColor(R.color.colorLegend))
            val lightColorTitle =
                getColor(R.styleable.ChartView_lightColorTitle, resources.getColor(R.color.colorTitle))
            val lightColorLabel =
                getColor(R.styleable.ChartView_lightColorLabel, resources.getColor(R.color.colorLabel))
            val lightColorGrid = getColor(R.styleable.ChartView_lightColorGrid, resources.getColor(R.color.colorGrid))
            setLightThemeColor(
                lightColorBackground,
                lightColorLegend,
                lightColorTitle,
                lightColorLabel,
                lightColorGrid
            )
        }
    }

    private fun setDarkStyles(typedArray: TypedArray) {
        typedArray.apply {
            val darkColorBackground =
                getColor(R.styleable.ChartView_darkColorBackground, resources.getColor(R.color.darkColorBackground))
            val darkColorLegend =
                getColor(R.styleable.ChartView_darkColorLegend, resources.getColor(R.color.darkColorLegend))
            val darkColorTitle =
                getColor(R.styleable.ChartView_darkColorTitle, resources.getColor(R.color.darkColorTitle))
            val darkColorLabel =
                getColor(R.styleable.ChartView_darkColorLabel, resources.getColor(R.color.darkColorLabel))
            val darkColorGrid = getColor(R.styleable.ChartView_darkColorGrid, resources.getColor(R.color.darkColorGrid))
            setDarkThemeColor(
                darkColorBackground,
                darkColorLegend,
                darkColorTitle,
                darkColorLabel,
                darkColorGrid
            )
        }
    }
}