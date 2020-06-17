package com.tp.forestfiremonitor

import android.app.Application
import com.tp.forestfiremonitor.di.KoinModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ForestFireMonitorApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin()
    }

    private fun initKoin() {
        startKoin {
            androidContext(this@ForestFireMonitorApplication)
            modules(KoinModules().getAllModules())
        }
    }
}
