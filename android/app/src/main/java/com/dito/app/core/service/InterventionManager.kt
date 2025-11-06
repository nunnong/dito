package com.dito.app.core.service
import android.content.Context
import android.util.Log

//추후 가이드라인 작성
object InterventionManager {

    private const val TAG = "InterventionManager"

    fun canIntervene(context: Context): Boolean {
        Log.d(TAG, "테스트 모드 → 항상 개입 허용")
        return true
    }

    fun recordIntervention(context: Context) {
        Log.d(TAG, "테스트 모드 -> 개입 기록 생략")
    }
}