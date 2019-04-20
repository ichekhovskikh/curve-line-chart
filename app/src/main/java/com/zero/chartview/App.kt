package com.zero.chartview

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.zero.chartview.di.AppComponent
import com.zero.chartview.di.DaggerAppComponent

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        appComponent = DaggerAppComponent.builder().build()
    }

    companion object {
        @JvmStatic
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        lateinit var appComponent: AppComponent
    }
}