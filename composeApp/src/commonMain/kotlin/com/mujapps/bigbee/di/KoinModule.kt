package com.mujapps.bigbee.di

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.observable.makeObservable
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

@OptIn(ExperimentalSettingsApi::class)
val sharedModule = module {
    single<ObservableSettings> { Settings().makeObservable() }
}

expect val targetModule: Module //Need to declare Audio player in each platform

fun initializeKoin(
    config: (KoinApplication.() -> Unit)? = null //Since nullable other platforms no need to pass a value
) {
    startKoin {
        config?.invoke(this)
        modules(sharedModule, targetModule)
    }
}
