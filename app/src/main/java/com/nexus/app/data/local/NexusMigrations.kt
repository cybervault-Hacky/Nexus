package com.nexus.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Non-destructive schema migrations for [NexusDatabase].
 *
 * Keep the SQL in these migrations aligned with Room's entity schema. Room
 * validates nullability, defaults, indices, and foreign keys after applying a
 * migration; SQL that merely has the right columns is not sufficient.
 */
object NexusMigrations {

    /** Phase 3: add the context-to-app relationship. */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `context_apps` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `contextId` TEXT NOT NULL,
                    `packageName` TEXT NOT NULL,
                    FOREIGN KEY(`contextId`) REFERENCES `contexts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
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

    /** Phase 4: add actions. */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `actions` (
                    `id` TEXT NOT NULL,
                    `contextId` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `payload` TEXT NOT NULL,
                    `isEnabled` INTEGER NOT NULL,
                    `position` INTEGER NOT NULL,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`contextId`) REFERENCES `contexts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_actions_contextId` ON `actions` (`contextId`)"
            )
        }
    }

    /** Phase 5 initial schema. It is normalized by [MIGRATION_4_5]. */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `capsules` (
                    `id` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `accentColor` INTEGER NOT NULL,
                    `contextSnapshot` TEXT,
                    `actionSnapshots` TEXT NOT NULL,
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

    /**
     * Phase 5 complete schema.
     *
     * Rebuilds the capsule tables because SQLite cannot add the required
     * foreign keys with ALTER TABLE. Child rows are staged before the parent
     * table is replaced, which also works for a version-4 database that was
     * created directly by Room and already has a child foreign key.
     */
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE `capsules_new` (
                    `id` TEXT NOT NULL,
                    `sourceContextId` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `schemaVersion` INTEGER NOT NULL,
                    `accentColor` INTEGER NOT NULL,
                    `contextSnapshot` TEXT,
                    `createdAt` INTEGER NOT NULL,
                    `capturedAt` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO `capsules_new` (
                    `id`, `sourceContextId`, `name`, `description`,
                    `schemaVersion`, `accentColor`, `contextSnapshot`,
                    `createdAt`, `capturedAt`
                )
                SELECT `id`, '', `name`, `description`, 1, `accentColor`,
                       `contextSnapshot`, `createdAt`, `createdAt`
                FROM `capsules`
                """.trimIndent()
            )

            // Stage valid child rows before dropping the version-4 parent.
            db.execSQL(
                """
                CREATE TEMP TABLE `capsule_apps_backup` (
                    `id` INTEGER NOT NULL,
                    `capsuleId` TEXT NOT NULL,
                    `packageName` TEXT NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO `capsule_apps_backup` (`id`, `capsuleId`, `packageName`)
                SELECT app.`id`, app.`capsuleId`, app.`packageName`
                FROM `capsule_apps` AS app
                INNER JOIN `capsules` AS capsule ON capsule.`id` = app.`capsuleId`
                """.trimIndent()
            )

            db.execSQL("DROP TABLE `capsule_apps`")
            db.execSQL("DROP TABLE `capsules`")
            db.execSQL("ALTER TABLE `capsules_new` RENAME TO `capsules`")
            db.execSQL(
                "CREATE INDEX `index_capsules_sourceContextId` ON `capsules` (`sourceContextId`)"
            )

            db.execSQL(
                """
                CREATE TABLE `capsule_apps` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `capsuleId` TEXT NOT NULL,
                    `packageName` TEXT NOT NULL,
                    `appName` TEXT NOT NULL,
                    `position` INTEGER NOT NULL,
                    FOREIGN KEY(`capsuleId`) REFERENCES `capsules`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO `capsule_apps` (`id`, `capsuleId`, `packageName`, `appName`, `position`)
                SELECT `id`, `capsuleId`, `packageName`, '', 0
                FROM `capsule_apps_backup`
                """.trimIndent()
            )
            db.execSQL("DROP TABLE `capsule_apps_backup`")
            db.execSQL(
                "CREATE INDEX `index_capsule_apps_capsuleId` ON `capsule_apps` (`capsuleId`)"
            )

            db.execSQL(
                """
                CREATE TABLE `capsule_actions` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `capsuleId` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `payload` TEXT NOT NULL,
                    `isEnabled` INTEGER NOT NULL,
                    `position` INTEGER NOT NULL,
                    FOREIGN KEY(`capsuleId`) REFERENCES `capsules`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX `index_capsule_actions_capsuleId` ON `capsule_actions` (`capsuleId`)"
            )
        }
    }

    /** Phase 7: persistent automation rules and execution history. */
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `automation_rules` (
                    `id` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `isEnabled` INTEGER NOT NULL,
                    `triggerType` TEXT NOT NULL,
                    `triggerPayload` TEXT NOT NULL,
                    `contextId` TEXT NOT NULL,
                    `cooldownSeconds` INTEGER NOT NULL,
                    `lastTriggeredAt` INTEGER,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_automation_rules_contextId` ON `automation_rules` (`contextId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_automation_rules_isEnabled` ON `automation_rules` (`isEnabled`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_automation_rules_triggerType` ON `automation_rules` (`triggerType`)")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `automation_executions` (
                    `id` TEXT NOT NULL,
                    `automationId` TEXT NOT NULL,
                    `startedAt` INTEGER NOT NULL,
                    `completedAt` INTEGER,
                    `status` TEXT NOT NULL,
                    `triggerType` TEXT NOT NULL,
                    `contextId` TEXT,
                    `successfulActions` INTEGER NOT NULL,
                    `failedActions` INTEGER NOT NULL,
                    `errorMessage` TEXT,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`automationId`) REFERENCES `automation_rules`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_automation_executions_automationId` ON `automation_executions` (`automationId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_automation_executions_startedAt` ON `automation_executions` (`startedAt`)")
        }
    }

    /** Phase 9: privacy-safe event diagnostics. */
    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `event_history` (
                    `id` TEXT NOT NULL,
                    `source` TEXT NOT NULL,
                    `eventType` TEXT NOT NULL,
                    `timestamp` INTEGER NOT NULL,
                    `matchedAutomationCount` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_event_history_timestamp` ON `event_history` (`timestamp`)")
        }
    }

    /** Phase 10: priority, health, smart conditions, and counters. */
    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `automation_rules` ADD COLUMN `priority` INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE `automation_rules` ADD COLUMN `healthStatus` TEXT NOT NULL DEFAULT 'UNKNOWN'")
            db.execSQL("ALTER TABLE `automation_rules` ADD COLUMN `conditionsJson` TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE `automation_rules` ADD COLUMN `executionCount` INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE `automation_rules` ADD COLUMN `failureCount` INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE `automation_rules` ADD COLUMN `successCount` INTEGER NOT NULL DEFAULT 0")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_automation_rules_priority` ON `automation_rules` (`priority`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_automation_rules_healthStatus` ON `automation_rules` (`healthStatus`)")
        }
    }

    val ALL = arrayOf(
        MIGRATION_1_2,
        MIGRATION_2_3,
        MIGRATION_3_4,
        MIGRATION_4_5,
        MIGRATION_5_6,
        MIGRATION_6_7,
        MIGRATION_7_8,
    )
}
