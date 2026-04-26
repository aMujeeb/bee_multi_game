package com.mujapps.bigbee.domain

import android.content.Context
import android.media.SoundPool
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import bigbee.composeapp.generated.resources.Res
import com.mujapps.bigbee.R

actual class AudioPlayer(mContext: Context) {

    private val mLoopingPlayer = ExoPlayer.Builder(mContext).build()
    private val mMediaItem = soundResList.map {
        MediaItem.fromUri(Res.getUri(it))
    }

    private val mSoundPool = SoundPool.Builder()
        .setMaxStreams(3) //How many sounds can play simultaneously
        .build()

    private val mJumpSoundFile = mSoundPool.load(mContext, R.raw.jump, 2)
    private val mFallingSoundFile = mSoundPool.load(mContext, R.raw.falling, 1)
    private val mGameOverSoundFile = mSoundPool.load(mContext, R.raw.game_over, 1)
    private val mGameSoundFile = mSoundPool.load(mContext, R.raw.game_sound, 2)

    private var mFallingSoundId: Int = 0

    init {
        mLoopingPlayer.prepare()
    }

    actual fun playJumpSound() {
        stopFallingSound()
        mSoundPool.play(mJumpSoundFile, 1f, 1f, 0, 1, 1f)
    }

    actual fun playFallingSound() {
        mFallingSoundId = mSoundPool.play(mFallingSoundFile, 1f, 1f, 0, 1, 1f)
    }

    actual fun playGameOverSound() {
        stopFallingSound()
        mSoundPool.play(mGameOverSoundFile, 1f, 1f, 0, 1, 1f)
    }

    actual fun playGamBackgroundSound() {
        mLoopingPlayer.repeatMode = Player.REPEAT_MODE_ONE
        mLoopingPlayer.setMediaItem(mMediaItem[3])
        mLoopingPlayer.play()
    }

    actual fun stopFallingSound() {
        mSoundPool.stop(mFallingSoundId)
    }

    actual fun stopGameSound() {
        mLoopingPlayer.pause()//Pause is convenient than Stop
        playGameOverSound()
    }

    actual fun release() {
        mLoopingPlayer.stop()
        mLoopingPlayer.clearMediaItems()
        mLoopingPlayer.release()
        mSoundPool.release()
    }
}