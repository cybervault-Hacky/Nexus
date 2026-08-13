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
)
