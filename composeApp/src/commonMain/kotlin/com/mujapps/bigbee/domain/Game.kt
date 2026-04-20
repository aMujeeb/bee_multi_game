package com.mujapps.bigbee.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class Game(//Using a data class to store game state. since it has special functions as 'copy' can update values of the game status dynamically
    val screenWidth: Int = 0, val screenHeight: Int = 0,
    val gravity: Float = 0.2f, //This can increase when game proceed levels
    val beeJumpImpulse: Float = -12f, // Jump velocity upwards
    val beeMaxVelocity: Float = 25f
) {
    var status by mutableStateOf(GameStatus.Idle)
        private set

    var beeVelocity by mutableStateOf(0f)
        private set

    //Bee characters initial position on screen
    var bee by mutableStateOf(
        BeeCharacter(
            x = screenWidth / 4f,
            y = screenHeight / 2f
        )
    )
        private set

    fun start() {
        status = GameStatus.Started
    }

    fun gameOver() {
        status = GameStatus.Over
    }

    fun jump() {
        beeVelocity = beeJumpImpulse
    }

    fun updateGameProgress() {
        if (bee.y < 0) {
            stopTheBee()
            return
        } else if (bee.y > screenHeight) {
            gameOver()
            return
        }
        beeVelocity = (beeVelocity + gravity).coerceIn(-beeMaxVelocity, beeMaxVelocity)
        bee = bee.copy(y = bee.y + beeVelocity)
    }

    fun stopTheBee() {
        beeVelocity = 0f
        bee = bee.copy(y = 0f)
    }
}