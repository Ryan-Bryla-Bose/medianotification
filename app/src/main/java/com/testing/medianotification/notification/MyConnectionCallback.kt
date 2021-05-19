package com.testing.medianotification.notification

import android.media.browse.MediaBrowser
import android.util.Log

class MyConnectionCallback : MediaBrowser.ConnectionCallback() {

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