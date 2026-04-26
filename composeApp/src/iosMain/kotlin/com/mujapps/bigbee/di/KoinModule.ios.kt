package com.mujapps.bigbee.di

import com.mujapps.bigbee.domain.AudioPlayer
import org.koin.dsl.module

actual val targetModule = module {
    single<AudioPlayer> { AudioPlayer() }
}