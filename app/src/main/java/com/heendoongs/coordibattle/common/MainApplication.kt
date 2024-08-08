package com.heendoongs.coordibattle.common

import android.app.Application
import com.heendoongs.coordibattle.global.PreferenceUtil

class MainApplication : Application() {
    companion object {
        lateinit var prefs: PreferenceUtil
    }

    override fun onCreate() {
        super.onCreate()
        prefs = PreferenceUtil(applicationContext)
    }
}