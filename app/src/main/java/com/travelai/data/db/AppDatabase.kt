package com.travelai.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.travelai.data.db.entities.ChatMessageEntity
import com.travelai.data.db.entities.ChatSessionEntity

@Database(
    entities = [
        ChatSessionEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
}
