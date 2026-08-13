package com.nexus.app.data.local

import com.nexus.app.domain.model.ActionType
import com.nexus.app.domain.model.NexusAction

/** Convert domain model to database entity. */
fun NexusAction.toEntity(): ActionEntity = ActionEntity(
    id = id,
    contextId = contextId,
    name = name,
    description = description,
    type = type.name,
    payload = payload,
    isEnabled = isEnabled,
    position = position,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

/** Convert database entity to domain model. */
fun ActionEntity.toDomain(): NexusAction = NexusAction(
    id = id,
    contextId = contextId,
    name = name,
    description = description,
    type = ActionType.valueOf(type),
    payload = payload,
    isEnabled = isEnabled,
    position = position,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
