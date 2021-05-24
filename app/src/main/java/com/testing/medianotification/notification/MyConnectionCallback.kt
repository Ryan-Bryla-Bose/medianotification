package com.testing.medianotification.notification

import android.support.v4.media.MediaBrowserCompat
import android.util.Log

class MyConnectionCallback : MediaBrowserCompat.ConnectionCallback() {

    private val TAG = "MyConnectionCallback"

    override fun onConnected() {
        super.onConnected()
        Log.d(TAG, "onConnected called")
    }

    override fun onConnectionFailed() {
        super.onConnectionFailed()
        Log.d(TAG, "onConnectionFailed called")
    }

    override fun onConnectionSuspended() {
        super.onConnectionSuspended()
        Log.d(TAG, "onConnectionSuspended called")
    }
}