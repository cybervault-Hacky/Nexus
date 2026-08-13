package com.nexus.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "automation_rules",
    indices = [
        Index(value = ["contextId"]),
        Index(value = ["isEnabled"]),
        Index(value = ["triggerType"]),
        Index(value = ["priority"]),
        Index(value = ["healthStatus"]),
    ],
)
data class AutomationEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "isEnabled") val isEnabled: Boolean,
    @ColumnInfo(name = "triggerType") val triggerType: String,
    @ColumnInfo(name = "triggerPayload") val triggerPayload: String,
    @ColumnInfo(name = "contextId") val contextId: String,
    @ColumnInfo(name = "cooldownSeconds") val cooldownSeconds: Int,
    @ColumnInfo(name = "lastTriggeredAt") val lastTriggeredAt: Long?,
    @ColumnInfo(name = "createdAt") val createdAt: Long,
    @ColumnInfo(name = "updatedAt") val updatedAt: Long,
    // Phase 10 additions
    @ColumnInfo(name = "priority", defaultValue = "1") val priority: Int = 1, // NORMAL
    @ColumnInfo(name = "healthStatus", defaultValue = "UNKNOWN") val healthStatus: String = "UNKNOWN",
    @ColumnInfo(name = "conditionsJson", defaultValue = "") val conditionsJson: String = "",
    @ColumnInfo(name = "executionCount", defaultValue = "0") val executionCount: Int = 0,
    @ColumnInfo(name = "failureCount", defaultValue = "0") val failureCount: Int = 0,
    @ColumnInfo(name = "successCount", defaultValue = "0") val successCount: Int = 0,
)
