package com.testing.medianotification

import android.content.Context
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.session.MediaButtonReceiver

class MyController constructor(private val context: Context) {

    fun play() {
        MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PLAY).send()
    }

    fun pause() {
        MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PAUSE).send()
    }

}