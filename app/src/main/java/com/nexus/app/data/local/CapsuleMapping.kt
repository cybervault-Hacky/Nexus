package com.nexus.app.data.local

import com.nexus.app.domain.model.ActionSnapshot
import com.nexus.app.domain.model.ActionType
import com.nexus.app.domain.model.AppSnapshot
import com.nexus.app.domain.model.ContextSnapshot
import com.nexus.app.domain.model.NexusCapsule
import org.json.JSONObject

/** Convert domain capsule to database entity. */
fun NexusCapsule.toEntity(): CapsuleEntity = CapsuleEntity(
    id = id,
    sourceContextId = sourceContextId,
    name = name,
    description = description,
    schemaVersion = schemaVersion,
    accentColor = accentColor,
    contextSnapshot = contextSnapshot?.let { snapshotToJson(it) },
    createdAt = createdAt,
    capturedAt = capturedAt,
)

/** Convert database entity + children to domain capsule. */
fun CapsuleEntity.toDomain(
    appSnapshots: List<AppSnapshot> = emptyList(),
    actionSnapshots: List<ActionSnapshot> = emptyList(),
): NexusCapsule = NexusCapsule(
    id = id,
    sourceContextId = sourceContextId,
    name = name,
    description = description,
    schemaVersion = schemaVersion,
    accentColor = accentColor,
    contextSnapshot = contextSnapshot?.let { snapshotFromJson(it) },
    appSnapshots = appSnapshots,
    actionSnapshots = actionSnapshots,
    createdAt = createdAt,
    capturedAt = capturedAt,
)

/** Convert domain AppSnapshot to database entity. */
fun AppSnapshot.toEntity(capsuleId: String): CapsuleAppEntity = CapsuleAppEntity(
    capsuleId = capsuleId,
    packageName = packageName,
    appName = appName,
    position = position,
)

/** Convert database entity to domain AppSnapshot. */
fun CapsuleAppEntity.toDomain(): AppSnapshot = AppSnapshot(
    packageName = packageName,
    appName = appName,
    position = position,
)

/** Convert domain ActionSnapshot to database entity. */
fun ActionSnapshot.toEntity(capsuleId: String): CapsuleActionEntity = CapsuleActionEntity(
    capsuleId = capsuleId,
    name = name,
    description = description,
    type = type.name,
    payload = payload,
    isEnabled = isEnabled,
    position = position,
)

/** Convert database entity to domain ActionSnapshot. */
fun CapsuleActionEntity.toDomain(): ActionSnapshot = ActionSnapshot(
    name = name,
    description = description,
    type = ActionType.valueOf(type),
    payload = payload,
    position = position,
    isEnabled = isEnabled,
)

// ── JSON serialization helpers ───────────────────────────────

private fun snapshotToJson(snapshot: ContextSnapshot): String = JSONObject().apply {
    put("name", snapshot.name)
    put("description", snapshot.description)
    put("iconId", snapshot.iconId)
    put("accentColor", snapshot.accentColor)
}.toString()

private fun snapshotFromJson(json: String): ContextSnapshot? {
    return try {
        val obj = JSONObject(json)
        ContextSnapshot(
            name = obj.getString("name"),
            description = obj.getString("description"),
            iconId = obj.optString("iconId", "grid"),
            accentColor = obj.optLong("accentColor", 0xFF6366F1),
        )
    } catch (_: Exception) {
        null
    }
}
