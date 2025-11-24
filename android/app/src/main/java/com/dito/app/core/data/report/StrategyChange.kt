package com.dito.app.core.data.report

import com.dito.app.R
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StrategyChange(
    @SerialName("time_slot") val timeSlot: String,
    val previous: String,
    val current: String,
    val reason: String
)

enum class TimeSlot {
    MORNING, LUNCH, AFTERNOON, NIGHT;

    companion object {
        fun from(value: String): TimeSlot = try {
            valueOf(value.uppercase())
        } catch (e: IllegalArgumentException) {
            MORNING // 기본값
        }
    }

    fun toEmoji(): String = when(this) {
        MORNING -> "🌅"
        LUNCH -> "🍽️"
        AFTERNOON -> "🌞"
        NIGHT -> "🌙"
    }

    fun toDisplayName(): String = when(this) {
        MORNING -> "오전"
        LUNCH -> "점심"
        AFTERNOON -> "오후"
        NIGHT -> "밤"
    }
}

enum class StrategyMode {
    STRICT, MODERATE, RELAXED, FOCUS;

    companion object {
        fun from(value: String): StrategyMode = try {
            valueOf(value.uppercase())
        } catch (e: IllegalArgumentException) {
            MODERATE // 기본값
        }
    }

    fun toIconRes(): Int = when(this) {
        STRICT -> R.drawable.flash
        MODERATE -> R.drawable.self_control
        RELAXED -> R.drawable.lock_open
        FOCUS -> R.drawable.goal
    }

    fun toDisplayName(): String = when(this) {
        STRICT -> "엄격"
        MODERATE -> "맞춤형"
        RELAXED -> "자유"
        FOCUS -> "집중"
    }
}
