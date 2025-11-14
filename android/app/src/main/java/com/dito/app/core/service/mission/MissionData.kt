package com.dito.app.core.service.mission

data class MissionData(
    val missionId: String,
    val missionType: String,
    val instruction: String,
    val durationSeconds: Int,
    val targetApps: List<String>,
    val coinReward: Int = 10,
    val deepLink: String? = null
)
