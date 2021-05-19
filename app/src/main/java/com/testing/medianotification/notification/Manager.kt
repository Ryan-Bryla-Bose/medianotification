package com.testing.medianotification.notification

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.media.VolumeProvider
import android.media.session.PlaybackState
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationCompat
import androidx.media.VolumeProviderCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver

import com.testing.medianotification.R

class Manager private constructor(
    private val context: Context,
    val mediaSession: MediaSessionCompat
) {

    var playing = false

    val TAG = "Manager"

    companion object {
        fun getInstance(context: Context, mediaSession: MediaSessionCompat): Manager {
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
        val notificationManager = NotificationManagerCompat.from(context)

        val channelId = "MEDIANOTIFICATIN.TESTING.channel_id"
        val channelName: CharSequence = "Some Channel"
        val importance = NotificationManager.IMPORTANCE_LOW
        val notificationChannel =
            NotificationChannel(channelId, channelName, importance)

        notificationManager.createNotificationChannel(notificationChannel)

        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(
                    if (playing) PlaybackStateCompat.STATE_PLAYING
                    else PlaybackStateCompat.STATE_STOPPED, 0, 1F
                )
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                ).build()
        )
        // Create a MediaStyle object and supply your media session token to it.
//        val mediaStyle = Notification.MediaStyle().setMediaSession(mediaSession.sessionToken)
//            .setShowActionsInCompactView(0, 1, 2)
        val mediaStyle = MediaStyle().setMediaSession(mediaSession.sessionToken)
            .setShowActionsInCompactView(0, 1, 2)


        // Create a Notification which is styled by your MediaStyle object.
        // This connects your media session to the media controls.
        // Don't forget to include a small icon.
        val notificationBuilder = getNotification(mediaStyle, channelId)

        notificationManager.notify(1, notificationBuilder.build())
        return notificationBuilder.build()
    }


    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getNotification(
        mediaStyle: MediaStyle,
        channelId: String
    ): NotificationCompat.Builder {

        return NotificationCompat.Builder(context, channelId)
            .setStyle(mediaStyle)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_previous_enabled_dark,
                    "Prev",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    )
                ).build()
            )
            .addAction(
                NotificationCompat.Action.Builder(
                    if (!playing) R.drawable.ic_play else R.drawable.ic_stop_button,
                    "Play",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,
                        PlaybackStateCompat.ACTION_PLAY
                    )
                ).build()
            )
            .addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_next_enabled_dark,
                    "Skip",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    )
                ).build()
            )
            .setColor(context.resources.getColor(R.color.purple_200, context.theme))
            .setColorized(true)
    }

}