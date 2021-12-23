package com.zero.sample

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.zero.chartview.BuildConfig
import com.zero.chartview.axis.formatter.ShortAxisFormatter
import com.zero.chartview.model.CurveLine
import com.zero.sample.service.AnimationThemeService
import com.zero.sample.service.ColoringService
import com.zero.sample.utils.convertChartColorsToThemeColors
import com.zero.sample.utils.convertThemeColorsToChartColors
import com.zero.sample.utils.testLines
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), Stainable {

    private val themeService = AnimationThemeService(BuildConfig.ANIMATION_DURATION_MS)
    private val coloringService = ColoringService(this)

    private var theme = ColoringService.ThemeStyle.LIGHT

    private lateinit var view: View

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        themeService.onInvalidate = ::colorsUpdated
        view = layoutInflater.inflate(R.layout.activity_main, null)
        setContentView(view)
        setThemeColorsDefault()
        createThemeButtonListener()
        initLabels()
        addLabels(testLines)
    }

    private fun createThemeButtonListener() {
        button.setOnClickListener {
            if (theme == ColoringService.ThemeStyle.LIGHT) {
                theme = ColoringService.ThemeStyle.DARK
                coloringService.updateColors(theme)
            } else {
                theme = ColoringService.ThemeStyle.LIGHT
                coloringService.updateColors(theme)
            }
        }
    }

    private fun initLabels() {
        labels.chart = chart.apply { yAxisFormatter = ShortAxisFormatter() }
    }

    fun addLabels(lines: List<CurveLine>) {
        lines.forEach { labels.addLineLabel(it) }
    }

    override fun getCurrentColors(): Stainable.ThemeColors {
        val currentColors = convertChartColorsToThemeColors(chartLayout.getChartColors())
        currentColors.colorTitle = titleView.textColors.defaultColor
        currentColors.colorLabel = labels.getTextColor()
        return currentColors
    }

    override fun setThemeColors(colors: Stainable.ThemeColors) {
        themeService.updateTheme(getCurrentColors(), colors)
    }

    private fun colorsUpdated(colors: Stainable.ThemeColors) {
        val chartColors = convertThemeColorsToChartColors(colors)
        chartLayout.setChartColors(chartColors)
        view.setBackgroundColor(colors.colorBackground)
        titleView.setTextColor(colors.colorTitle)
        labels.setTextColor(colors.colorLabel)
    }

    private fun setThemeColorsDefault() {
        setLightThemeColorsDefault()
        setDarkThemeColorsDefault()
    }

    private fun setLightThemeColorsDefault() {
        val lightColorBackground = resources.getColor(R.color.colorBackground)
        val lightColorLegend = resources.getColor(R.color.colorLegend)
        val lightColorGrid = resources.getColor(R.color.colorGrid)
        val lightPopupLine = resources.getColor(R.color.colorPopupLine)
        val lightFrameSelector = resources.getColor(R.color.colorFrameSelector)
        val lightColorFogSelector = resources.getColor(R.color.colorFogSelector)
        val lightColorTitle = resources.getColor(R.color.colorTitle)
        val lightColorLabel = resources.getColor(R.color.colorLabel)
        coloringService.setLightThemeColor(
            lightColorBackground,
            lightColorLegend,
            lightColorGrid,
            lightPopupLine,
            lightFrameSelector,
            lightColorFogSelector,
            lightColorTitle,
            lightColorLabel
        )
    }

    private fun setDarkThemeColorsDefault() {
        val darkColorBackground = resources.getColor(R.color.darkColorBackground)
        val darkColorLegend = resources.getColor(R.color.darkColorLegend)
        val darkColorGrid = resources.getColor(R.color.darkColorGrid)
        val darkPopupLine = resources.getColor(R.color.darkColorPopupLine)
        val darkColorFrameSelector = resources.getColor(R.color.darkColorFrameSelector)
        val darkColorFogSelector = resources.getColor(R.color.darkColorFogSelector)
        val darkColorTitle = resources.getColor(R.color.darkColorTitle)
        val darkColorLabel = resources.getColor(R.color.darkColorLabel)
        coloringService.setDarkThemeColor(
            darkColorBackground,
            darkColorLegend,
            darkColorGrid,
            darkPopupLine,
            darkColorFrameSelector,
            darkColorFogSelector,
            darkColorTitle,
            darkColorLabel
        )
    }
}
