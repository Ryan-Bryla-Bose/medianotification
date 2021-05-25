package com.testing.medianotification.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.media.VolumeProviderCompat
import com.testing.medianotification.MainActivity

class MyMediaSession(private val context: Context, tag: String) : MediaSessionCompat(context, tag) {

    private val TAG = "MyMediaSession"

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

    init {
        build()
    }

    private fun build() {
        if (SDK_INT < Build.VERSION_CODES.O) {
            return
        }
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
        setPlaybackState(
            PlaybackStateCompat.Builder().setState(
                PlaybackStateCompat.STATE_PAUSED, 0, 1F
            )
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                ).build()
        )

        setCallback(
            MySessionCallback(Manager.getInstance(context, this), context)
        )
        setSessionActivity(
            PendingIntent.getActivity(
                context,
                1,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        setPlaybackToRemote(volumeProvider)
        controller.registerCallback(object : MediaControllerCompat.Callback() {
            override fun onAudioInfoChanged(info: MediaControllerCompat.PlaybackInfo?) {
                super.onAudioInfoChanged(info)
                Log.d(TAG, "onAudioInfoChanged: ${info.toString()}")
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                super.onPlaybackStateChanged(state)
                val notificationManager = NotificationManagerCompat.from(context)
                notificationManager.notify(
                    1,
                    Manager.getInstance(context, this@MyMediaSession).updateNotification()
                )
                Log.d(TAG, "onPlaybackStateChanged: ${state.toString()}")

            }
        })
        isActive = true
    }
}