package com.dito.app.core.data.home

import kotlinx.serialization.Serializable

@Serializable
data class UpdateWeeklyGoalResponse (
    val error: Boolean,
    val message: String?
)