package com.nexus.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Lightweight event history — stores only safe metadata.
 * No sensitive payloads, no notification content, no location data.
 */
@Entity(
    tableName = "event_history",
    indices = [Index(value = ["timestamp"])],
)
data class EventHistoryEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "source") val source: String,
    @ColumnInfo(name = "eventType") val eventType: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "matchedAutomationCount") val matchedAutomationCount: Int,
)
