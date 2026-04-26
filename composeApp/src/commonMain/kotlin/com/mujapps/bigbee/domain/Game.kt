package com.mujapps.bigbee.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.russhwolf.settings.ObservableSettings
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.random.Random

const val HIGH_SCORE_KEY = "score_key"

data class Game(//Using a data class to store game state. since it has special functions as 'copy' can update values of the game status dynamically
    val screenWidth: Int = 0, val screenHeight: Int = 0,
    val gravity: Float = 0.6f, //This can increase when game proceed levels
    val beeJumpImpulse: Float = -10f, // Jump velocity upwards
    val beeMaxVelocity: Float = 15f,
    val beeRadius: Float = 30f,
    val pipeWidth: Float = 150f,
    val pipeVelocity: Float = 5f,
    val pipeGapSize: Float = 250f // This can alter to mage game easier or harder
) : KoinComponent {

    //Injecting Settings object
    private val mSettings: ObservableSettings by inject()

    //Injecting Audio player object
    private val mAudioPlayer: AudioPlayer by inject()
    var status by mutableStateOf(GameStatus.Idle)
        private set

    var beeVelocity by mutableStateOf(0f)
        private set

    //Bee characters initial position on screen
    var bee by mutableStateOf(
        BeeCharacter(
            x = screenWidth / 4f,
            y = screenHeight / 2f,
            radius = beeRadius
        )
    )
        private set


    //Adding List of pipes
    var mPipePairs = mutableStateListOf<PipePair>()

    var mCurrentScore by mutableStateOf(0)
        private set

    var mBestScore by mutableStateOf(0)
        private set


    //Game over should stop falling sound. Also when press jump should stop falling sound
    private var isFallingSoundPlayed = false


    init {
        mBestScore = mSettings.getInt(
            key = HIGH_SCORE_KEY,
            defaultValue = 0
        )

        mSettings.addIntListener(
            key = HIGH_SCORE_KEY,
            defaultValue = 0
        ) {
            mBestScore = it
        }
    }

    fun start() {
        status = GameStatus.Started
        mAudioPlayer.playGamBackgroundSound()
    }

    fun gameOver() {
        status = GameStatus.Over
        mAudioPlayer.stopGameSound()
        saveScore()
        isFallingSoundPlayed = false
    }

    fun jump() {
        beeVelocity = beeJumpImpulse
        mAudioPlayer.playJumpSound()
        isFallingSoundPlayed = false
    }

    fun updateGameProgress() {
        mPipePairs.forEach { pare ->
            if (isCollided(pare)) {
                gameOver()
                return
            }

            //Score counter
            if(!pare.scored && bee.x > pare.x + pipeWidth / 2) {
                pare.scored = true
                mCurrentScore += 1
            }
        }
        if (bee.y < 0) {
            stopTheBee()
            return
        } else if (bee.y > screenHeight) {
            gameOver()
            return
        }
        beeVelocity = (beeVelocity + gravity).coerceIn(-beeMaxVelocity, beeMaxVelocity)
        bee = bee.copy(y = bee.y + beeVelocity)

        //Spawning new pipes
        spawnPipes()

        //When to play the falling sound
        if(beeVelocity > (beeVelocity / 1.1)) {
            if(!isFallingSoundPlayed) {
                mAudioPlayer.playFallingSound()
                isFallingSoundPlayed = true
            }
        }
    }

    private fun spawnPipes() {
        mPipePairs.forEach { it.x -= pipeVelocity }
        mPipePairs.removeAll { it.x + pipeWidth < 0 }// Remove pipes out of screen
        if (mPipePairs.isEmpty() || mPipePairs.last().x < screenWidth / 2) { //If no pipes or last pipe has juts passed the center point of the screen
            val initialPipeX =
                screenWidth.toFloat() + pipeWidth //Initial x position should be outside of the screen
            val topHeight =
                Random.nextFloat() * (screenHeight / 2) //Random generated pipe size will not exceed the height of the screen. nextFloat between 1 and -1
            val bottomHeight = screenHeight - topHeight - pipeGapSize //Allow bee to pass through

            val newPipePair = PipePair(
                x = initialPipeX,
                y = topHeight + pipeGapSize / 2,
                topHeight = topHeight,
                bottomHeight = bottomHeight
            )
            mPipePairs.add(newPipePair)
        }
    }

    fun stopTheBee() {
        beeVelocity = 0f
        bee = bee.copy(y = 0f)
    }

    private fun resetBeePosition() {
        bee = bee.copy(y = (screenHeight / 2).toFloat())
        beeVelocity = 0f
    }

    fun reStartGame() {
        resetBeePosition()
        removeAllPipes()
        resetScore()
        start()
        isFallingSoundPlayed = false
    }

    //Remove pipes when Restart the game
    fun removeAllPipes() {
        mPipePairs.clear()
    }

    //Collision detection done by
    private fun isCollided(pipePair: PipePair): Boolean {
        //Check Horizontal collision. Bee overlaps pipes X range
        val beeRightEdge = bee.x + bee.radius
        val beeLeftEdge = bee.x - bee.radius
        val pipeLeftEdge = pipePair.x - pipeWidth / 2
        val pipeRightEdge = pipePair.x + pipeWidth / 2

        val horizontalCollision = beeRightEdge > pipeLeftEdge && beeLeftEdge < pipeRightEdge

        //Check the Bee within Vertical gap
        val beeTopEdge = bee.y - beeRadius
        val beeBottomEdge = bee.y + beeRadius
        val gapTopEdge = pipePair.y - pipeGapSize / 2
        val gapBottomEdge = pipePair.y + pipeGapSize / 2

        val beeInGap = beeTopEdge > gapTopEdge && beeBottomEdge < gapBottomEdge

        return horizontalCollision && !beeInGap
    }

    private fun saveScore() {
        if (mBestScore < mCurrentScore) {
            mSettings.putInt(key = HIGH_SCORE_KEY, value = mCurrentScore)
            mBestScore = mCurrentScore
        }
    }

    private fun resetScore() {
        mCurrentScore = 0
    }

    fun cleanUp() {
        mAudioPlayer
    }
}