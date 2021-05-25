package com.testing.medianotification.notification

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.KeyEvent
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.O)
class MySessionCallback constructor(private val manager: Manager, private val context: Context) :
    MediaSessionCompat.Callback() {
    val TAG = "MySessionCallback"

    private val playbackStateBuilder: PlaybackStateCompat.Builder = PlaybackStateCompat.Builder()

    private val playingState: PlaybackStateCompat get() = playbackStateBuilder
        .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1F)
        .setActions(
            PlaybackStateCompat.ACTION_PAUSE or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        ).build()

    private val pausedState: PlaybackStateCompat get() = playbackStateBuilder
        .setState(PlaybackStateCompat.STATE_PAUSED, 0, 1F)
        .setActions(
            PlaybackStateCompat.ACTION_PLAY or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        ).build()


    override fun onCommand(command: String, args: Bundle?, cb: ResultReceiver?) {
        super.onCommand(command, args, cb)
        Log.d(TAG, "onCommand called with: $command")
    }

    override fun onCustomAction(action: String, extras: Bundle?) {
        super.onCustomAction(action, extras)
        Log.d(TAG, "onCustomAction called with: $action")
    }

    override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
        Log.d(
            TAG,
            "onMediaButtonEvent: ${mediaButtonIntent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)}"
        )
        return super.onMediaButtonEvent(mediaButtonIntent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPlay() {
        super.onPlay()
        Log.d(TAG, "onPlay")

        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
            setOnAudioFocusChangeListener(audioFocusChangeListener(manager))
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            build()
        }

        if (hasFocus) {
            manager.mediaSession.isActive = true
            manager.mediaSession.setPlaybackState(playingState)
//            service.startForeground(1, manager.updateNotification())
            Log.d(TAG, "AUDIO_FOCUS already acquired")
            return
        }
        val result = am.requestAudioFocus(audioFocusRequest)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            hasFocus = true
            Log.d(TAG, "AUDIO_FOCUS_REQUEST_GRANTED")
            manager.mediaSession.isActive = true
            manager.mediaSession.setPlaybackState(playingState)
        }
    }

    override fun onPlayFromSearch(query: String?, extras: Bundle?) {
        super.onPlayFromSearch(query, extras)
        Log.d(TAG, "Playing based on query: $query")
        Log.d(TAG, "Search bundle: ${extras.toString()}")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")

        manager.mediaSession.setPlaybackState(pausedState)
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onSkipToNext() {
        super.onSkipToNext()
        Log.d(TAG, "onSkipToNext")
        manager.updateNotification()
    }

    override fun onSkipToPrevious() {
        super.onSkipToPrevious()
        Log.d(TAG, "onSkipToPrevious")
        manager.updateNotification()
    }

    inner class audioFocusChangeListener constructor(private val manager: Manager) :
        AudioManager.OnAudioFocusChangeListener {

        val TAG = "AudioChangeListener"

        override fun onAudioFocusChange(it: Int) {
            Log.d(TAG, "audio focus changed: $it")
            when (it) {
                AudioManager.AUDIOFOCUS_LOSS -> {
                    hasFocus = false
                    Log.d(TAG, "audioFocus LOST")
                    manager.mediaSession.setPlaybackState(pausedState)
                    manager.updateNotification()
                }
                AudioManager.AUDIOFOCUS_GAIN -> {
                    hasFocus = true
                    Log.d(TAG, "audioFocus GAINED")
                    manager.updateNotification()
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    hasFocus = false
                    Log.d(TAG, "audioFocus AUDIOFOCUS_LOSS_TRANSIENT")
                    manager.updateNotification()
                }
                else -> {
                    Log.d(TAG, "other audiofocus event")
                    manager.updateNotification()
                }

            }
        }
    }

    companion object {
        var hasFocus = false
    }
}