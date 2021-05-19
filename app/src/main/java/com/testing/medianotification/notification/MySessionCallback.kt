package com.testing.medianotification.notification

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.session.MediaSession
import android.os.Build
import android.os.Bundle
import android.os.ResultReceiver
import android.util.Log
import android.view.KeyEvent
import androidx.annotation.RequiresApi

class MySessionCallback constructor(private val manager: Manager, private val context: Context) : MediaSession.Callback() {
    val TAG = "MySessionCallback"

    override fun onCommand(command: String, args: Bundle?, cb: ResultReceiver?) {
        super.onCommand(command, args, cb)
        Log.d(TAG, "onCommand called with: $command")
    }

    override fun onCustomAction(action: String, extras: Bundle?) {
        super.onCustomAction(action, extras)
        Log.d(TAG, "onCustomAction called with: $action")
    }

    override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
        Log.d(TAG, "onMediaButtonEvent: ${mediaButtonIntent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)}")
        return super.onMediaButtonEvent(mediaButtonIntent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPlay() {
        super.onPlay()
        Log.d(TAG, "onPlay")
        manager.togglePlaying()


        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Request audio focus for playback, this registers the afChangeListener

        val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
            setOnAudioFocusChangeListener(AudioManager.OnAudioFocusChangeListener {
                Log.d(TAG, "audio focus changed: $it")
            })
            setAudioAttributes(AudioAttributes.Builder().run {
                setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                build()
            })
            build()
        }
        val result = am.requestAudioFocus(audioFocusRequest)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d(TAG, "AUDIOFOCUS_REQUEST_GRANTED")
            // Start the service
            context.startService(Intent(context, MediaPlaybackService::class.java))
            // Set the session active  (and update metadata and state)
            manager.mediaSession.isActive = true
            // start the player (custom call)
//            player.start()
//            // Register BECOME_NOISY BroadcastReceiver
//            registerReceiver(myNoisyAudioStreamReceiver, intentFilter)
            // Put the service in the foreground, post notification
            manager.updateNotification()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
        manager.togglePlaying()
        manager.updateNotification()
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onSkipToNext() {
        super.onSkipToNext()
        Log.d(TAG, "onSkipToNext")
    }

    override fun onSkipToPrevious() {
        super.onSkipToPrevious()
        Log.d(TAG, "onSkipToPrevious")
    }
}