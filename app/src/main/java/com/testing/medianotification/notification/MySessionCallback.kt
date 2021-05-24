package com.testing.medianotification.notification

import android.app.Service
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
import androidx.core.app.NotificationManagerCompat
import androidx.media.MediaBrowserServiceCompat

@RequiresApi(Build.VERSION_CODES.O)
class MySessionCallback constructor(private val manager: Manager, private val context: Context, private val service: MediaBrowserServiceCompat) :
    MediaSessionCompat.Callback() {
    val TAG = "MySessionCallback"

    val playbackStateBuilder: PlaybackStateCompat.Builder = PlaybackStateCompat.Builder()

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
        manager.togglePlaying()


        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Request audio focus for playback, this registers the afChangeListener

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
//            manager.mediaSession.setPlaybackState(
//                PlaybackStateCompat.Builder()
//                    .setState(
//                        PlaybackStateCompat.STATE_PLAYING, 0, 1F
//                    )
//                    .setActions(
//                        PlaybackStateCompat.ACTION_PAUSE or
//                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
//                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
//                    ).build()
//            )
//            service.startForeground(1, manager.updateNotification())
            Log.d(TAG, "AUDIOFOCUS already acquired")

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(1, manager.updateNotification())

            return
        }
        val result = am.requestAudioFocus(audioFocusRequest)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            hasFocus = true
            Log.d(TAG, "AUDIOFOCUS_REQUEST_GRANTED")
            manager.mediaSession.isActive = true
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(1, manager.updateNotification())
        }
        //service.startForeground(1, manager.updateNotification())
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
        manager.togglePlaying()

//        manager.mediaSession.setPlaybackState(
//            PlaybackStateCompat.Builder()
//                .setState(
//                    PlaybackStateCompat.STATE_PAUSED, 0, 1F
//                )
//                .setActions(
//                    PlaybackStateCompat.ACTION_PLAY or
//                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
//                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
//                ).build()
//        )
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(1, manager.updateNotification())
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


    private class audioFocusChangeListener constructor(private val manager: Manager) :
        AudioManager.OnAudioFocusChangeListener {

        val TAG = "AudioChangeListener"

        override fun onAudioFocusChange(it: Int) {
            Log.d(TAG, "audio focus changed: $it")
            when (it) {
                AudioManager.AUDIOFOCUS_LOSS -> {
                    manager.playing = false
                    hasFocus = false
                    Log.d(TAG, "audioFocus LOST")
                    manager.updateNotification()
                }
                AudioManager.AUDIOFOCUS_GAIN -> {
                    hasFocus = true
                    manager.playing = true
                    Log.d(TAG, "audioFocus GAINED")
                    manager.updateNotification()
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    manager.playing = false
                    hasFocus = false
                    Log.d(TAG, "audioFocus AUDIOFOCUS_LOSS_TRANSIENT")
                    manager.updateNotification()
                }
                else -> {
                    manager.playing = false
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