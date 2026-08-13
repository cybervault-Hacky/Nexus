package com.nexus.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The single Room database for NEXUS.
 * Version 7 (Phase 9): adds event_history table.
 */
@Database(
    entities = [
        ContextEntity::class, ContextAppEntity::class, ActionEntity::class,
        CapsuleEntity::class, CapsuleAppEntity::class, CapsuleActionEntity::class,
        AutomationEntity::class, AutomationExecutionEntity::class,
        EventHistoryEntity::class,
    ],
    version = 7,
    exportSchema = false,
)
abstract class NexusDatabase : RoomDatabase() {
    abstract fun contextDao(): ContextDao
    abstract fun contextAppDao(): ContextAppDao
    abstract fun actionDao(): ActionDao
    abstract fun capsuleDao(): CapsuleDao
    abstract fun capsuleAppDao(): CapsuleAppDao
    abstract fun capsuleActionDao(): CapsuleActionDao
    abstract fun automationDao(): AutomationDao
    abstract fun automationExecutionDao(): AutomationExecutionDao
    abstract fun eventHistoryDao(): EventHistoryDao

    companion object {
        private const val DATABASE_NAME = "nexus.db"
        @Volatile private var INSTANCE: NexusDatabase? = null

        fun getInstance(applicationContext: Context): NexusDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(applicationContext, NexusDatabase::class.java, DATABASE_NAME)
                    .addMigrations(*NexusMigrations.ALL)
                    .build().also { INSTANCE = it }
            }
        }
    }
}
