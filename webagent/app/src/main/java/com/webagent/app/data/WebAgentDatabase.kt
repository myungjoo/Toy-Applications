package com.webagent.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [EventData::class, Recommendation::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WebAgentDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun recommendationDao(): RecommendationDao
}
