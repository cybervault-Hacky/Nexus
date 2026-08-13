package com.nexus.app

import com.nexus.app.data.local.ContextEntity
import com.nexus.app.data.local.toDomain
import com.nexus.app.data.local.toEntity
import com.nexus.app.domain.model.NexusContext
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for entity ↔ domain model mapping.
 * Ensures data survives the round-trip without loss.
 */
class ContextEntityMappingTest {

    private val sampleContext = NexusContext(
        id = "test-id",
        name = "Coding",
        description = "Development workspace",
        iconId = "code",
        appCount = 4,
        actionCount = 2,
        isActive = true,
        accentColor = 0xFF6366F1,
        createdAt = 1000L,
        updatedAt = 2000L,
    )

    private val sampleEntity = ContextEntity(
        id = "test-id",
        name = "Coding",
        description = "Development workspace",
        iconId = "code",
        appCount = 4,
        actionCount = 2,
        isActive = true,
        accentColor = 0xFF6366F1,
        createdAt = 1000L,
        updatedAt = 2000L,
    )

    @Test
    fun `domain to entity preserves all fields`() {
        val entity = sampleContext.toEntity()
        assertEquals(sampleContext.id, entity.id)
        assertEquals(sampleContext.name, entity.name)
        assertEquals(sampleContext.description, entity.description)
        assertEquals(sampleContext.iconId, entity.iconId)
        assertEquals(sampleContext.appCount, entity.appCount)
        assertEquals(sampleContext.actionCount, entity.actionCount)
        assertEquals(sampleContext.isActive, entity.isActive)
        assertEquals(sampleContext.accentColor, entity.accentColor)
        assertEquals(sampleContext.createdAt, entity.createdAt)
        assertEquals(sampleContext.updatedAt, entity.updatedAt)
    }

    @Test
    fun `entity to domain preserves all fields`() {
        val domain = sampleEntity.toDomain()
        assertEquals(sampleEntity.id, domain.id)
        assertEquals(sampleEntity.name, domain.name)
        assertEquals(sampleEntity.description, domain.description)
        assertEquals(sampleEntity.iconId, domain.iconId)
        assertEquals(sampleEntity.appCount, domain.appCount)
        assertEquals(sampleEntity.actionCount, domain.actionCount)
        assertEquals(sampleEntity.isActive, domain.isActive)
        assertEquals(sampleEntity.accentColor, domain.accentColor)
        assertEquals(sampleEntity.createdAt, domain.createdAt)
        assertEquals(sampleEntity.updatedAt, domain.updatedAt)
    }

    @Test
    fun `round-trip domain to entity to domain preserves data`() {
        val roundTripped = sampleContext.toEntity().toDomain()
        assertEquals(sampleContext, roundTripped)
    }

    @Test
    fun `round-trip entity to domain to entity preserves data`() {
        val roundTripped = sampleEntity.toDomain().toEntity()
        assertEquals(sampleEntity, roundTripped)
    }
}
