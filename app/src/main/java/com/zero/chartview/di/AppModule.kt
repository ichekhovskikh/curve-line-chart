package com.zero.chartview.di

import com.zero.chartview.BuildConfig
import com.zero.chartview.service.AnimationLegendService
import com.zero.chartview.service.AnimationLineService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    @Singleton
    fun provideAnimationLineService(): AnimationLineService = AnimationLineService(BuildConfig.ANIMATION_DURATION_MS)

    @Provides
    @Singleton
    fun provideAnimationLegendService(): AnimationLegendService = AnimationLegendService(BuildConfig.ANIMATION_DURATION_MS)
}