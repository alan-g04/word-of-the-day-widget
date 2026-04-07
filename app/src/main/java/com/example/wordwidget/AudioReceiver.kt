package com.example.wordwidget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AudioReceiver : BroadcastReceiver() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onReceive(context: Context, intent: Intent) {
        val audioUrl = intent.getStringExtra("AUDIO_URL") ?: return

        // Launch in background thread to prevent receiver thread blocking
        CoroutineScope(Dispatchers.IO).launch {
            try {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(audioUrl)
                    prepare() // prepare synchronously in IO thread
                    start()
                }
                
                // Tidy up when playback over
                mediaPlayer?.setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                mediaPlayer?.release()
                mediaPlayer = null
            }
        }
    }
}