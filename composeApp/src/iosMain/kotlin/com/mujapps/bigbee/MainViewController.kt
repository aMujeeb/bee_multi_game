package com.mujapps.bigbee

import androidx.compose.ui.window.ComposeUIViewController
import com.mujapps.bigbee.di.initializeKoin

fun MainViewController() = ComposeUIViewController(
    configure = { initializeKoin() }
) { App() }