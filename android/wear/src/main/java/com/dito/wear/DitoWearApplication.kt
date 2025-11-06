package com.dito.wear

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DitoWearApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
