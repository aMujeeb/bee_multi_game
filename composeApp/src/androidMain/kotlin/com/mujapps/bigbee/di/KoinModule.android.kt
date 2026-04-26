package com.mujapps.bigbee.di

import com.mujapps.bigbee.domain.AudioPlayer
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val targetModule = module {
    single<AudioPlayer> {
        AudioPlayer(mContext = androidContext())
    }
}