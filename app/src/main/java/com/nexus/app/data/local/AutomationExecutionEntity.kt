package com.nexus.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "automation_executions",
    foreignKeys = [
        ForeignKey(
            entity = AutomationEntity::class,
            parentColumns = ["id"],
            childColumns = ["automationId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["automationId"]),
        Index(value = ["startedAt"]),
    ],
)
data class AutomationExecutionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "automationId") val automationId: String,
    @ColumnInfo(name = "startedAt") val startedAt: Long,
    @ColumnInfo(name = "completedAt") val completedAt: Long?,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "triggerType") val triggerType: String,
    @ColumnInfo(name = "contextId") val contextId: String?,
    @ColumnInfo(name = "successfulActions") val successfulActions: Int,
    @ColumnInfo(name = "failedActions") val failedActions: Int,
    @ColumnInfo(name = "errorMessage") val errorMessage: String?,
)
