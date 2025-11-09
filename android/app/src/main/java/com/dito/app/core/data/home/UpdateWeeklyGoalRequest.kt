package com.dito.app.core.data.home

import kotlinx.serialization.Serializable

@Serializable
data class UpdateWeeklyGoalRequest (
    val goal: String
)