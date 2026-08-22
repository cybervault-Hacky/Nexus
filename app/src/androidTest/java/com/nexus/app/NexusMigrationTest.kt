package com.nexus.app

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nexus.app.data.local.NexusDatabase
import com.nexus.app.data.local.NexusMigrations
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Opens representative legacy databases through the complete production
 * migration path. Room's own schema validator checks every column default,
 * index, and foreign key when writableDatabase is opened.
 */
@RunWith(AndroidJUnit4::class)
class NexusMigrationTest {
    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val databaseNames = mutableListOf<String>()

    @After
    fun cleanUp() {
        databaseNames.forEach(context::deleteDatabase)
    }

    @Test
    fun migrateVersion1To8_preservesContextAndPassesRoomValidation() {
        val name = "migration-v1-to-v8.db"
        createLegacyDatabase(name, 1, VERSION_1_SCHEMA) { db ->
            db.execSQL(
                """
                INSERT INTO `contexts` (
                    `id`, `name`, `description`, `iconId`, `appCount`,
                    `actionCount`, `isActive`, `accentColor`, `createdAt`, `updatedAt`
                ) VALUES ('legacy-context', 'Legacy', '', 'grid', 0, 0, 1, 1, 10, 20)
                """.trimIndent()
            )
        }

        val database = openMigratedDatabase(name)
        try {
            // This access forces RoomOpenHelper to run all migrations and its
            // generated version-8 schema validation before the query executes.
            val sqliteDatabase = database.openHelper.writableDatabase
            val contextEntity = runBlocking {
                database.contextDao().getById("legacy-context")
            }
            assertNotNull(contextEntity)
            assertEquals("Legacy", contextEntity?.name)
            assertEquals(8, sqliteDatabase.query("PRAGMA user_version").use { cursor ->
                cursor.moveToFirst()
                cursor.getInt(0)
            })
        } finally {
            database.close()
        }
    }

    @Test
    fun migrateVersion5To8_preservesCapsuleAndPassesRoomValidation() {
        val name = "migration-v5-to-v8.db"
        createLegacyDatabase(name, 5, VERSION_5_SCHEMA) { db ->
            db.execSQL(
                """
                INSERT INTO `contexts` (
                    `id`, `name`, `description`, `iconId`, `appCount`,
                    `actionCount`, `isActive`, `accentColor`, `createdAt`, `updatedAt`
                ) VALUES ('context-v5', 'Version 5', '', 'grid', 0, 0, 0, 1, 10, 20)
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO `capsules` (
                    `id`, `sourceContextId`, `name`, `description`,
                    `schemaVersion`, `accentColor`, `contextSnapshot`,
                    `createdAt`, `capturedAt`
                ) VALUES ('capsule-v5', 'context-v5', 'Saved capsule', '', 1, 1, NULL, 10, 20)
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO `capsule_apps` (
                    `capsuleId`, `packageName`, `appName`, `position`
                ) VALUES ('capsule-v5', 'com.example.legacy', 'Legacy app', 0)
                """.trimIndent()
            )
        }

        val database = openMigratedDatabase(name)
        try {
            val sqliteDatabase = database.openHelper.writableDatabase
            val capsule = runBlocking {
                database.capsuleDao().getById("capsule-v5")
            }
            val apps = runBlocking {
                database.capsuleAppDao().getByCapsule("capsule-v5")
            }
            assertNotNull(capsule)
            assertEquals("Saved capsule", capsule?.name)
            assertEquals(listOf("com.example.legacy"), apps.map { it.packageName })
            assertEquals(8, sqliteDatabase.query("PRAGMA user_version").use { cursor ->
                cursor.moveToFirst()
                cursor.getInt(0)
            })
        } finally {
            database.close()
        }
    }

    private fun createLegacyDatabase(
        name: String,
        version: Int,
        schema: List<String>,
        seed: (SupportSQLiteDatabase) -> Unit,
    ) {
        databaseNames += name
        context.deleteDatabase(name)
        val configuration = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(name)
            .callback(object : SupportSQLiteOpenHelper.Callback(version) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    schema.forEach { statement -> db.execSQL(statement) }
                }

                override fun onUpgrade(
                    db: SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int,
                ) = Unit
            })
            .build()
        val helper = FrameworkSQLiteOpenHelperFactory().create(configuration)
        seed(helper.writableDatabase)
        helper.close()
    }

    private fun openMigratedDatabase(name: String): NexusDatabase =
        Room.databaseBuilder(context, NexusDatabase::class.java, name)
            .addMigrations(*NexusMigrations.ALL)
            .build()

    companion object {
        private val VERSION_1_SCHEMA = listOf(
            """
            CREATE TABLE `contexts` (
                `id` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `description` TEXT NOT NULL,
                `iconId` TEXT NOT NULL,
                `appCount` INTEGER NOT NULL,
                `actionCount` INTEGER NOT NULL,
                `isActive` INTEGER NOT NULL,
                `accentColor` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )

        private val VERSION_5_SCHEMA = VERSION_1_SCHEMA + listOf(
            """
            CREATE TABLE `context_apps` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `contextId` TEXT NOT NULL,
                `packageName` TEXT NOT NULL,
                FOREIGN KEY(`contextId`) REFERENCES `contexts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
            "CREATE INDEX `index_context_apps_contextId` ON `context_apps` (`contextId`)",
            "CREATE UNIQUE INDEX `index_context_apps_contextId_packageName` ON `context_apps` (`contextId`, `packageName`)",
            """
            CREATE TABLE `actions` (
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
            """.trimIndent(),
            "CREATE INDEX `index_actions_contextId` ON `actions` (`contextId`)",
            """
            CREATE TABLE `capsules` (
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
            """.trimIndent(),
            "CREATE INDEX `index_capsules_sourceContextId` ON `capsules` (`sourceContextId`)",
            """
            CREATE TABLE `capsule_apps` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `capsuleId` TEXT NOT NULL,
                `packageName` TEXT NOT NULL,
                `appName` TEXT NOT NULL,
                `position` INTEGER NOT NULL,
                FOREIGN KEY(`capsuleId`) REFERENCES `capsules`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
            "CREATE INDEX `index_capsule_apps_capsuleId` ON `capsule_apps` (`capsuleId`)",
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
            """.trimIndent(),
            "CREATE INDEX `index_capsule_actions_capsuleId` ON `capsule_actions` (`capsuleId`)",
        )
    }
}
