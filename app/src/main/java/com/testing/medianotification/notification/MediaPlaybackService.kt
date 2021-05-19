package com.testing.medianotification.notification

import android.content.Intent
import android.media.MediaMetadata
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.media.MediaBrowserServiceCompat
import androidx.media.VolumeProviderCompat
import androidx.media.session.MediaButtonReceiver

private const val MY_MEDIA_ROOT_ID = "media_root_id"
private const val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"

class MediaPlaybackService : MediaBrowserServiceCompat() {

    val TAG = "MediaPlaybackService"

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private lateinit var manager: Manager

    private val volumeProvider = object : VolumeProviderCompat(VOLUME_CONTROL_ABSOLUTE, 10, 0) {
        override fun onAdjustVolume(direction: Int) {
            super.onAdjustVolume(direction)
            currentVolume += direction
            Log.d(TAG, "Adjusted volume in direction: $direction")
        }

        override fun onSetVolumeTo(volume: Int) {
            super.onSetVolumeTo(volume)
            currentVolume = volume
            Log.d(TAG, "Set volume to: $volume")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        mediaSession = MediaSessionCompat(this, "MediaNotificationSession")

        manager = Manager.getInstance(this, mediaSession)

        mediaSession.apply {

            setMetadata(
                MediaMetadataCompat.Builder()
                    .putString(MediaMetadata.METADATA_KEY_TITLE, "Wonder")
                    .putString(MediaMetadata.METADATA_KEY_ARTIST, "Tranquilities")
                    .putLong(
                        MediaMetadata.METADATA_KEY_DURATION,
                        -1
                    ) // negative is unknown or infinite
                    .build()
            )

            setPlaybackToRemote(volumeProvider)

            setCallback(
                MySessionCallback(
                    manager,
                    this@MediaPlaybackService
                )
            )

            isActive = true
            setSessionToken(sessionToken)

            controller.registerCallback(object : MediaControllerCompat.Callback() {
                override fun onAudioInfoChanged(info: MediaControllerCompat.PlaybackInfo?) {
                    super.onAudioInfoChanged(info)
                    Log.d(TAG, "onAudioInfoChanged: ${info.toString()}")
                }

                override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
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

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        val mediaItems = mutableListOf<MediaBrowserCompat.MediaItem>()

        // Check if this is the root menu:
        if (MY_MEDIA_ROOT_ID == parentId) {
            mediaItems.add(
                MediaBrowserCompat.MediaItem(
                    MediaDescriptionCompat.Builder().setTitle("hello").build(),
                    MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
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

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.release()
        stopForeground(true)
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