package com.dito.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DitoApplication : Application(){

    override fun onCreate() {
        super.onCreate()
    }
}