package com.mujapps.bigbee.domain

expect class AudioPlayer {
    fun playJumpSound()
    fun playFallingSound()
    fun playGameOverSound()
    fun playGamBackgroundSound()
    fun stopFallingSound() //When JUmp pressed or game ends should stop it
    fun stopGameSound() //When Game ENDS
    fun release() //Release all sounds
}

val soundResList = listOf(
    "files/falling.wav",
    "files/jump.wav",
    "files/game_over.wav",
    "files/game_sound.wav"
)