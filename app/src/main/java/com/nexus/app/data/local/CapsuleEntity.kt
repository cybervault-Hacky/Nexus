package com.nexus.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a Capsule.
 *
 * The context snapshot is stored as a JSON string so the capsule is fully
 * self-contained. App and action snapshots are in separate normalized tables.
 *
 * [sourceContextId] is metadata/history — there is NO foreign key to contexts
 * because capsules must survive context deletion.
 */
@Entity(
    tableName = "capsules",
    indices = [Index(value = ["sourceContextId"])],
)
data class CapsuleEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "sourceContextId") val sourceContextId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "schemaVersion") val schemaVersion: Int,
    @ColumnInfo(name = "accentColor") val accentColor: Long,
    @ColumnInfo(name = "contextSnapshot") val contextSnapshot: String?, // JSON
    @ColumnInfo(name = "createdAt") val createdAt: Long,
    @ColumnInfo(name = "capturedAt") val capturedAt: Long,
)
