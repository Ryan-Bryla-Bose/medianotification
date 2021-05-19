package com.testing.medianotification.notification

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.media.VolumeProvider
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.media.session.MediaButtonReceiver
import com.testing.medianotification.R

class Manager private constructor(
    private val context: Context,
    val mediaSession: MediaSession
) {

    var playing = false

    val TAG = "Manager"

    val volumeProvider = object : VolumeProvider(VolumeProvider.VOLUME_CONTROL_FIXED, 10, 2) {
        override fun onAdjustVolume(direction: Int) {
            super.onAdjustVolume(direction)
            Log.d(TAG, "Adjusted volume to: $direction")
        }

        override fun onSetVolumeTo(volume: Int) {
            super.onSetVolumeTo(volume)
            Log.d(TAG, "Set volume to: $volume")
        }
    }

    companion object {
        fun getInstance(context: Context, mediaSession: MediaSession): Manager {
            instance = instance
                ?: Manager(
                    context,
                    mediaSession
                )
            return instance as Manager
        }

        private var instance: Manager? = null
    }

    fun togglePlaying() {
        playing = !playing
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateNotification(): Notification {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "MEDIANOTIFICATIN.TESTING.channel_id"
        val channelName: CharSequence = "Some Channel"
        val importance = NotificationManager.IMPORTANCE_LOW
        val notificationChannel =
            NotificationChannel(channelId, channelName, importance)

        notificationManager.createNotificationChannel(notificationChannel)

        mediaSession.setPlaybackState(
            PlaybackState.Builder()
                .setState(
                    if (playing) PlaybackState.STATE_PLAYING
                    else PlaybackState.STATE_STOPPED, 0, 1F
                )
                .setActions(
                    PlaybackState.ACTION_PLAY or
                            PlaybackState.ACTION_PAUSE or
                            PlaybackState.ACTION_SKIP_TO_NEXT or
                            PlaybackState.ACTION_SKIP_TO_PREVIOUS
                ).build()
        )

        // Create a MediaStyle object and supply your media session token to it.
        val mediaStyle = Notification.MediaStyle().setMediaSession(mediaSession.sessionToken)
            .setShowActionsInCompactView(0, 1, 2)


        // Create a Notification which is styled by your MediaStyle object.
        // This connects your media session to the media controls.
        // Don't forget to include a small icon.
        val notification = getNotification(mediaStyle, channelId)

        notificationManager.notify(1, notification)
        return notification
    }


    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getNotification(
        mediaStyle: Notification.MediaStyle,
        channelId: String
    ): Notification {

        return Notification.Builder(context, channelId)
            .setStyle(mediaStyle)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .addAction(
                Notification.Action.Builder(
                    R.drawable.ic_previous_enabled_dark,
                    "Prev",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,
                        PlaybackState.ACTION_SKIP_TO_PREVIOUS
                    )
                ).build()
            )
            .addAction(
                Notification.Action.Builder(
                    if (!playing) R.drawable.ic_play else R.drawable.ic_stop_button,
                    "Play",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,
                        PlaybackState.ACTION_PLAY
                    )
                ).build()
            )
            .addAction(
                Notification.Action.Builder(
                    R.drawable.ic_next_enabled_dark,
                    "Skip",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,
                        PlaybackState.ACTION_SKIP_TO_NEXT
                    )
                ).build()
            )
            .setColor(context.resources.getColor(R.color.purple_200, context.theme))
            .setColorized(true)
            .build()
    }

}