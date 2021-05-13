package com.testing.medianotification.notification

import android.media.session.MediaSession
import android.os.Build
import android.os.Bundle
import android.os.ResultReceiver
import android.util.Log
import androidx.annotation.RequiresApi

class MySessionCallback constructor(private val manager: Manager) : MediaSession.Callback() {
    val TAG = "MySessionCallback"

    override fun onCommand(command: String, args: Bundle?, cb: ResultReceiver?) {
        super.onCommand(command, args, cb)
        Log.d(TAG, "onCommand called with: $command")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPlay() {
        super.onPlay()
        Log.d(TAG, "onPlay")
        manager.togglePlaying()
        manager.updateNotification()
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