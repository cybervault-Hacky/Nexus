package com.nexus.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Normalized snapshot of an app within a Capsule.
 * Stores both packageName and appName so the capsule is self-contained
 * even if the app is later uninstalled or renamed.
 *
 * Foreign key CASCADE: deleting the Capsule removes its app snapshots.
 */
@Entity(
    tableName = "capsule_apps",
    foreignKeys = [
        ForeignKey(
            entity = CapsuleEntity::class,
            parentColumns = ["id"],
            childColumns = ["capsuleId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["capsuleId"]),
    ],
)
data class CapsuleAppEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "capsuleId") val capsuleId: String,
    @ColumnInfo(name = "packageName") val packageName: String,
    @ColumnInfo(name = "appName") val appName: String,
    @ColumnInfo(name = "position") val position: Int,
)
