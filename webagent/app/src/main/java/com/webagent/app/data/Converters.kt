package com.webagent.app.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromEventType(value: EventType): String {
        return value.name
    }
    
    @TypeConverter
    fun toEventType(value: String): EventType {
        return EventType.valueOf(value)
    }
    
    @TypeConverter
    fun fromRecommendationType(value: RecommendationType): String {
        return value.name
    }
    
    @TypeConverter
    fun toRecommendationType(value: String): RecommendationType {
        return RecommendationType.valueOf(value)
    }
}
