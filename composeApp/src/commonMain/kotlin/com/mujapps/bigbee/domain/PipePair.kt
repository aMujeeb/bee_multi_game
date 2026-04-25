package com.mujapps.bigbee.domain

data class PipePair(
    var x: Float, //Position on canvas
    val y:Float,
    val topHeight: Float, //Piller height variation
    val bottomHeight: Float,
    var scored: Boolean = false //Used of score counting
)
