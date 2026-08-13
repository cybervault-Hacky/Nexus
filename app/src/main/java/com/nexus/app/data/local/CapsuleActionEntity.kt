package com.nexus.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Normalized snapshot of an Action within a Capsule.
 * This is a frozen copy — it does NOT reference the live actions table.
 *
 * Foreign key CASCADE: deleting the Capsule removes its action snapshots.
 */
@Entity(
    tableName = "capsule_actions",
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
data class CapsuleActionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "capsuleId") val capsuleId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "payload") val payload: String,
    @ColumnInfo(name = "isEnabled") val isEnabled: Boolean,
    @ColumnInfo(name = "position") val position: Int,
)
