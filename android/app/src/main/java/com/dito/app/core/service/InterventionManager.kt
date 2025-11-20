package com.dito.app.core.service
import android.content.Context
import android.util.Log
import com.dito.app.BuildConfig

//추후 가이드라인 작성
object InterventionManager {

    private const val TAG = "InterventionManager"

    fun canIntervene(context: Context): Boolean {
        if (BuildConfig.SKIP_AI_INTERVENTION) {
            Log.d(TAG, "DEBUG 모드 → AI 개입 스킵")
            return false
        }
        Log.d(TAG, "AI 개입 허용")
        return true
    }

    fun recordIntervention(context: Context) {
        Log.d(TAG, "테스트 모드 -> 개입 기록 생략")
    }
}