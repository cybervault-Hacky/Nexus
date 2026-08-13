package com.nexus.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Schema migrations for [NexusDatabase].
 *
 * Migration 1→2 (Phase 3): Adds context_apps table.
 * Migration 2→3 (Phase 4): Adds actions table.
 * Migration 3→4 (Phase 5 initial): Adds capsules and capsule_apps tables.
 * Migration 4→5 (Phase 5 complete): Adds capsule_actions, schemaVersion,
 *   sourceContextId, capturedAt on capsules; appName/position on capsule_apps.
 */
object NexusMigrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `context_apps` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `contextId` TEXT NOT NULL,
                    `packageName` TEXT NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_context_apps_contextId` ON `context_apps` (`contextId`)"
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_context_apps_contextId_packageName` ON `context_apps` (`contextId`, `packageName`)"
            )
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `actions` (
                    `id` TEXT NOT NULL,
                    `contextId` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `description` TEXT NOT NULL DEFAULT '',
                    `type` TEXT NOT NULL,
                    `payload` TEXT NOT NULL,
                    `isEnabled` INTEGER NOT NULL DEFAULT 1,
                    `position` INTEGER NOT NULL DEFAULT 0,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`contextId`) REFERENCES `contexts`(`id`) ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_actions_contextId` ON `actions` (`contextId`)"
            )
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `capsules` (
                    `id` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `description` TEXT NOT NULL DEFAULT '',
                    `accentColor` INTEGER NOT NULL,
                    `contextSnapshot` TEXT,
                    `actionSnapshots` TEXT NOT NULL DEFAULT '[]',
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    `lastRestoredAt` INTEGER,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `capsule_apps` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `capsuleId` TEXT NOT NULL,
                    `packageName` TEXT NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_capsule_apps_capsuleId` ON `capsule_apps` (`capsuleId`)"
            )
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create the new capsule_actions table
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `capsule_actions` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `capsuleId` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `description` TEXT NOT NULL DEFAULT '',
                    `type` TEXT NOT NULL,
                    `payload` TEXT NOT NULL,
                    `isEnabled` INTEGER NOT NULL DEFAULT 1,
                    `position` INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_capsule_actions_capsuleId` ON `capsule_actions` (`capsuleId`)"
            )

            // Create new capsules table with updated schema
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `capsules_new` (
                    `id` TEXT NOT NULL,
                    `sourceContextId` TEXT NOT NULL DEFAULT '',
                    `name` TEXT NOT NULL,
                    `description` TEXT NOT NULL DEFAULT '',
                    `schemaVersion` INTEGER NOT NULL DEFAULT 1,
                    `accentColor` INTEGER NOT NULL,
                    `contextSnapshot` TEXT,
                    `createdAt` INTEGER NOT NULL,
                    `capturedAt` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_capsules_new_sourceContextId` ON `capsules_new` (`sourceContextId`)"
            )

            // Copy data from old capsules table (best-effort)
            db.execSQL(
                """
                INSERT INTO `capsules_new` (`id`, `sourceContextId`, `name`, `description`, `schemaVersion`, `accentColor`, `contextSnapshot`, `createdAt`, `capturedAt`)
                SELECT `id`, '', `name`, `description`, 1, `accentColor`, `contextSnapshot`, `createdAt`, `createdAt`
                FROM `capsules`
                """.trimIndent()
            )

            // Drop old table and rename
            db.execSQL("DROP TABLE IF EXISTS `capsules`")
            db.execSQL("ALTER TABLE `capsules_new` RENAME TO `capsules`")

            // Create new capsule_apps table with appName and position
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `capsule_apps_new` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `capsuleId` TEXT NOT NULL,
                    `packageName` TEXT NOT NULL,
                    `appName` TEXT NOT NULL DEFAULT '',
                    `position` INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_capsule_apps_new_capsuleId` ON `capsule_apps_new` (`capsuleId`)"
            )

            // Copy data from old capsule_apps
            db.execSQL(
                """
                INSERT INTO `capsule_apps_new` (`id`, `capsuleId`, `packageName`, `appName`, `position`)
                SELECT `id`, `capsuleId`, `packageName`, '', 0
                FROM `capsule_apps`
                """.trimIndent()
            )

            // Drop old table and rename
            db.execSQL("DROP TABLE IF EXISTS `capsule_apps`")
            db.execSQL("ALTER TABLE `capsule_apps_new` RENAME TO `capsule_apps`")
        }
    }

    val ALL = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
}
