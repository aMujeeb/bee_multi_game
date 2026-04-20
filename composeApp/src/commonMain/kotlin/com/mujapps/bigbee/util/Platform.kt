package com.mujapps.bigbee.util

enum class Platform {
    Android,
    iOS
}

expect fun getPlatform(): Platform