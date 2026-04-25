package com.mujapps.bigbee

import android.app.Application
import com.mujapps.bigbee.di.initializeKoin

class BeeApp: Application() {
    override fun onCreate() {
        super.onCreate()
        initializeKoin()
    }
}