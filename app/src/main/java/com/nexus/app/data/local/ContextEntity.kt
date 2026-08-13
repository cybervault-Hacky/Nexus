package com.nexus.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nexus.app.domain.model.NexusContext

/**
 * Room entity that mirrors [NexusContext].
 * Kept separate from the domain model so the database schema
 * can evolve independently of the public API.
 */
@Entity(tableName = "contexts")
data class ContextEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val iconId: String,
    val appCount: Int,
    val actionCount: Int,
    val isActive: Boolean,
    val accentColor: Long,
    val createdAt: Long,
    val updatedAt: Long,
)

/** Convert from domain model to database entity. */
fun NexusContext.toEntity(): ContextEntity = ContextEntity(
    id = id,
    name = name,
    description = description,
    iconId = iconId,
    appCount = appCount,
    actionCount = actionCount,
    isActive = isActive,
    accentColor = accentColor,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

/** Convert from database entity to domain model. */
fun ContextEntity.toDomain(): NexusContext = NexusContext(
    id = id,
    name = name,
    description = description,
    iconId = iconId,
    appCount = appCount,
    actionCount = actionCount,
    isActive = isActive,
    accentColor = accentColor,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
