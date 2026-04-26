package com.mujapps.bigbee

import android.app.Application
import com.mujapps.bigbee.di.initializeKoin
import org.koin.android.ext.koin.androidContext

class BeeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeKoin { androidContext(this@BeeApp) }
    }
}