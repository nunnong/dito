package com.dito.app.core.ui.util

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes

object SoundPlayer {
    private var mediaPlayer: MediaPlayer? = null

    fun playSound(context: Context, @RawRes soundResId: Int) {
        // Stop and release any previously playing sound
        mediaPlayer?.release()
        mediaPlayer = null

        mediaPlayer = MediaPlayer.create(context.applicationContext, soundResId)
        mediaPlayer?.setOnCompletionListener {
            it.release()
            mediaPlayer = null
        }
        mediaPlayer?.start()
    }
}
