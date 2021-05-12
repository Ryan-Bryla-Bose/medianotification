package com.testing.medianotification

import android.content.Intent
import android.media.browse.MediaBrowser
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.Bundle
import android.service.media.MediaBrowserService
import android.util.Log
import androidx.media.session.MediaButtonReceiver
import com.testing.medianotification.callback.MySessionCallback

private const val MY_MEDIA_ROOT_ID = "media_root_id"
private const val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"

class MediaPlaybackService : MediaBrowserService() {

    val TAG = "MediaPlaybackService"

    private lateinit var mediaSession: MediaSession
    private lateinit var stateBuilder: PlaybackState.Builder
    private lateinit var manager: Manager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //MediaButtonReceiver.handleIntent(mediaSession, intent)
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

            stateBuilder = PlaybackState.Builder()
                .setActions(
                    PlaybackState.ACTION_PLAY or
                    PlaybackState.ACTION_SKIP_TO_NEXT or
                    PlaybackState.ACTION_SKIP_TO_PREVIOUS
                )

            setPlaybackState(stateBuilder.build())

            setCallback(MySessionCallback(manager))

            setSessionToken(sessionToken)
        }

        Log.d(TAG, "onCreate called, mediaSession created")

        startForeground(1, manager.updateNotification())
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowser.MediaItem>>
    ) {
        val mediaItems = mutableListOf<MediaBrowser.MediaItem>()

        // Check if this is the root menu:
        if (MY_MEDIA_ROOT_ID == parentId) {
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