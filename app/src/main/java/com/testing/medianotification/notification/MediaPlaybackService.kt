package com.testing.medianotification.notification

import android.content.Intent
import android.media.MediaDescription
import android.media.MediaMetadata
import android.media.browse.MediaBrowser
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.Bundle
import android.service.media.MediaBrowserService
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.media.session.MediaButtonReceiver

private const val MY_MEDIA_ROOT_ID = "media_root_id"
private const val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"

class MediaPlaybackService : MediaBrowserService() {

    val TAG = "MediaPlaybackService"

    private lateinit var mediaSession: MediaSession
    private lateinit var stateBuilder: PlaybackState.Builder
    private lateinit var manager: Manager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val mediaSessionCompat = MediaSessionCompat.fromMediaSession(this, mediaSession)
        MediaButtonReceiver.handleIntent(mediaSessionCompat, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        mediaSession = MediaSession(this, "MediaNotificationSession")

        manager = Manager.getInstance(this, mediaSession)

        mediaSession.apply {
            setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS)

            setMetadata(
                MediaMetadata.Builder()
                    .putString(MediaMetadata.METADATA_KEY_TITLE, "Wonder")
                    .putString(MediaMetadata.METADATA_KEY_ARTIST, "Tranquilities")
                    .putLong(
                        MediaMetadata.METADATA_KEY_DURATION,
                        -1
                    ) // negative is unknown or infinite
                    .build()
            )

            setCallback(
                MySessionCallback(
                    manager,
                    this@MediaPlaybackService
                )
            )

            isActive = true
            setSessionToken(sessionToken)

            controller.registerCallback(object : MediaController.Callback() {
                override fun onAudioInfoChanged(info: MediaController.PlaybackInfo?) {
                    super.onAudioInfoChanged(info)
                    Log.d(TAG, "onAudioInfoChanged: ${info.toString()}")
                }

                override fun onPlaybackStateChanged(state: PlaybackState?) {
                    super.onPlaybackStateChanged(state)
                    Log.d(TAG, "onPlaybackStateChanged: ${state.toString()}")

                }
            })

            controller.transportControls
        }


        Log.d(TAG, "onCreate called, mediaSession created")
        Log.d(TAG, "media session active: ${mediaSession.isActive}")

        startForeground(1, manager.updateNotification())
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.release()
        stopForeground(true)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowser.MediaItem>>
    ) {
        val mediaItems = mutableListOf<MediaBrowser.MediaItem>()

        // Check if this is the root menu:
        if (MY_MEDIA_ROOT_ID == parentId) {
            mediaItems.add(
                MediaBrowser.MediaItem(
                    MediaDescription.Builder().setTitle("hello").build(),
                    MediaBrowser.MediaItem.FLAG_PLAYABLE
                )
            )
            // Build the MediaItem objects for the top level,
            // and put them in the mediaItems list...
        } else {
            // Examine the passed parentMediaId to see which submenu we're at,
            // and put the children of that menu in the mediaItems list...
        }
        result.sendResult(mediaItems)
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot(MY_MEDIA_ROOT_ID, null)
    }

    companion object {
        val ACTION_SKIP_TO_NEXT = "NOTIFICATION_SKIP_TO_NEXT"
        val ACTION_SKIP_TO_PREV = "NOTIFICATION_SKIP_TO_PREV"
    }
}