package com.nexus.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The single Room database for NEXUS.
 *
 * Version 1 (Phase 2): contexts table
 * Version 2 (Phase 3): adds context_apps table
 * Version 3 (Phase 4): adds actions table
 * Version 4 (Phase 5 initial): adds capsules and capsule_apps tables
 * Version 5 (Phase 5 complete): adds capsule_actions, schema version,
 *   sourceContextId, capturedAt, appName on capsule_apps
 * Version 6 (Phase 7): adds automation_rules and automation_executions tables
 */
@Database(
    entities = [
        ContextEntity::class,
        ContextAppEntity::class,
        ActionEntity::class,
        CapsuleEntity::class,
        CapsuleAppEntity::class,
        CapsuleActionEntity::class,
        AutomationEntity::class,
        AutomationExecutionEntity::class,
    ],
    version = 6,
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

    companion object {
        private const val DATABASE_NAME = "nexus.db"

        @Volatile
        private var INSTANCE: NexusDatabase? = null

        fun getInstance(applicationContext: Context): NexusDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    applicationContext,
                    NexusDatabase::class.java,
                    DATABASE_NAME,
                )
                    .addMigrations(*NexusMigrations.ALL)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
