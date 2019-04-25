package com.zero.chartview.di

import com.zero.chartview.ChartView
import com.zero.chartview.GraphicView
import com.zero.chartview.SelectorView
import com.zero.chartview.axis.YAxisView
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(graphicView: GraphicView)
    fun inject(chartView: ChartView)
    fun inject(selectorView: SelectorView)
    fun inject(yAxisView: YAxisView)
}