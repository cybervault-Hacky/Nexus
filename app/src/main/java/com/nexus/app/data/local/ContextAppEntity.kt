package com.nexus.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing the many-to-many relationship between
 * Contexts and installed Android applications.
 *
 * [packageName] is the stable Android package identifier (e.g. "com.termux").
 * A package may belong to multiple contexts but only once per context.
 */
@Entity(
    tableName = "context_apps",
    foreignKeys = [
        ForeignKey(
            entity = ContextEntity::class,
            parentColumns = ["id"],
            childColumns = ["contextId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["contextId"]),
        Index(value = ["contextId", "packageName"], unique = true),
    ],
)
data class ContextAppEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "contextId") val contextId: String,
    @ColumnInfo(name = "packageName") val packageName: String,
)
