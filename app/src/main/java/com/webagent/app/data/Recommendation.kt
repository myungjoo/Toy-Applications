package com.webagent.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recommendations")
data class Recommendation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: RecommendationType,
    val title: String,
    val description: String,
    val action: String? = null, // JSON string for action data
    val priority: Int = 0, // 0 = low, 1 = medium, 2 = high
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

enum class RecommendationType {
    REMINDER,
    EXERCISE_SUGGESTION,
    MESSAGE_TO_SEND,
    EMAIL_TO_SEND,
    SETTINGS_CHANGE
}
