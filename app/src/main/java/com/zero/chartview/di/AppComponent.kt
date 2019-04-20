package com.zero.chartview.di

import com.zero.chartview.GraphicView
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(graphicView: GraphicView)
}