package com.mujapps.bigbee.domain

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.NSBundle
import platform.Foundation.NSURL
import platform.Foundation.NSURL.Companion.fileURLWithPath

@OptIn(ExperimentalForeignApi::class)
actual class AudioPlayer {

    //Each sound will be assigned a separate AV player
    private var mAudioPlayer: MutableMap<String, AVAudioPlayer?> =
        mutableMapOf()
    private var mFallingSoundPlayer: AVAudioPlayer? = null

    init {
        //Config audio sessions for play back
        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryPlayback, error = null)
        session.setActive(true, null)
    }

    actual fun playJumpSound() {
        stopFallingSound()
        playSound("jump")
    }

    actual fun playFallingSound() {
        playSound("falling")
    }

    actual fun playGameOverSound() {
        stopFallingSound()
        playSound("game_over")
    }

    actual fun playGamBackgroundSound() {
        val url = getSoundUrl("game_sound") //need another instance to be played parallel
        val player = url?.let { AVAudioPlayer(it, null) }
        player?.numberOfLoops = -1 //Indicate indefinite repeats
        player?.prepareToPlay()
        player?.play()
        mAudioPlayer["game_sound"] = player
    }

    actual fun stopFallingSound() {
        mFallingSoundPlayer?.stop()
        mFallingSoundPlayer = null
    }

    actual fun stopGameSound() {
        mAudioPlayer["game_sound"]?.stop()
        mAudioPlayer["game_sound"] = null
    }

    actual fun release() {
        mAudioPlayer.values.forEach {
            it?.stop()
        }
        mAudioPlayer.clear()
        mFallingSoundPlayer?.stop()
        mFallingSoundPlayer = null
    }

    private fun playSound(soundName: String): AVAudioPlayer? {
        val player = mAudioPlayer[soundName] ?: getSoundUrl(soundName)?.let { url ->
            val newPlayer = AVAudioPlayer(contentsOfURL = url, error = null)
            newPlayer.prepareToPlay()
            mAudioPlayer[soundName] = newPlayer
            newPlayer
        }
        player?.play()
        return player
    }

    private fun getSoundUrl(resourceName: String): NSURL? {
        val bundle = NSBundle.mainBundle()
        val path = bundle.pathForResource(resourceName, "wav")
        return path?.let { fileURLWithPath(it) }
    }
}