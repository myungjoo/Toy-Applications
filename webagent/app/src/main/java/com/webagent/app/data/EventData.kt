package com.webagent.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "events")
data class EventData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: EventType,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: String? = null // JSON string for additional data
)

enum class EventType {
    SMS_INCOMING,
    SMS_OUTGOING,
    NOTIFICATION,
    KEYBOARD_INPUT,
    EMAIL_INCOMING,
    EMAIL_OUTGOING
}
