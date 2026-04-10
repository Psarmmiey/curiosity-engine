package com.curiosityengine.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CuriosityApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
