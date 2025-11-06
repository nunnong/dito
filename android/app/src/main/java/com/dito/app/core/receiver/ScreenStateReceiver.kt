package com.dito.app.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dito.app.core.service.mission.MissionTracker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ScreenStateReceiver : BroadcastReceiver() {

    @Inject
    lateinit var missionTracker: MissionTracker

    companion object{
        private const val TAG = "ScreenStateReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            Intent.ACTION_SCREEN_ON -> {
                Log.d(TAG, "화면 ON")
                missionTracker.onScreenEvent(isScreenOn = true)
            }
            Intent.ACTION_SCREEN_OFF -> {
                Log.d(TAG, "화면 OFF")
                missionTracker.onScreenEvent(isScreenOn = false)
            }
        }
    }
}