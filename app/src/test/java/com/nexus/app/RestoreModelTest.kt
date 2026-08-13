package com.nexus.app

import com.nexus.app.domain.model.restore.ChangeCategory
import com.nexus.app.domain.model.restore.RestoreChange
import com.nexus.app.domain.model.restore.RestoreChangeType
import com.nexus.app.domain.model.restore.RestorePreview
import com.nexus.app.domain.model.restore.RestoreResult
import com.nexus.app.domain.model.restore.RestoreStatus
import com.nexus.app.domain.model.restore.RestoreTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for restoration domain models.
 */
class RestoreModelTest {

    // ── RestoreTarget ────────────────────────────────────────

    @Test
    fun `RestoreTarget has two values`() {
        assertEquals(2, RestoreTarget.entries.size)
    }

    // ── RestoreChangeType ────────────────────────────────────

    @Test
    fun `RestoreChangeType has five values`() {
        assertEquals(5, RestoreChangeType.entries.size)
    }

    // ── RestoreChange ────────────────────────────────────────

    @Test
    fun `RestoreChange holds correct data`() {
        val change = RestoreChange(
            category = ChangeCategory.APP,
            name = "Termux",
            detail = "com.termux",
            type = RestoreChangeType.ADDED,
        )
        assertEquals(ChangeCategory.APP, change.category)
        assertEquals("Termux", change.name)
        assertEquals(RestoreChangeType.ADDED, change.type)
    }

    // ── RestorePreview ───────────────────────────────────────

    @Test
    fun `RestorePreview hasChanges is true when apps added`() {
        val preview = makePreview(appsAdded = 1)
        assertTrue(preview.hasChanges)
    }

    @Test
    fun `RestorePreview hasChanges is true when actions removed`() {
        val preview = makePreview(actionsRemoved = 1)
        assertTrue(preview.hasChanges)
    }

    @Test
    fun `RestorePreview hasChanges is false when nothing changes`() {
        val preview = makePreview()
        assertFalse(preview.hasChanges)
    }

    @Test
    fun `RestorePreview hasMissingApps is true when apps missing`() {
        val preview = makePreview(appsMissing = 2)
        assertTrue(preview.hasMissingApps)
    }

    @Test
    fun `RestorePreview hasInvalidActions is true when actions invalid`() {
        val preview = makePreview(actionsInvalid = 1)
        assertTrue(preview.hasInvalidActions)
    }

    // ── RestoreResult ────────────────────────────────────────

    @Test
    fun `RestoreResult SUCCESS is successful`() {
        val result = makeResult(RestoreStatus.SUCCESS)
        assertTrue(result.isSuccessful)
    }

    @Test
    fun `RestoreResult PARTIAL is successful`() {
        val result = makeResult(RestoreStatus.PARTIAL)
        assertTrue(result.isSuccessful)
    }

    @Test
    fun `RestoreResult FAILED is not successful`() {
        val result = makeResult(RestoreStatus.FAILED)
        assertFalse(result.isSuccessful)
    }

    @Test
    fun `RestoreResult CANCELLED is not successful`() {
        val result = makeResult(RestoreStatus.CANCELLED)
        assertFalse(result.isSuccessful)
    }

    @Test
    fun `RestoreResult holds correct counts`() {
        val result = RestoreResult(
            status = RestoreStatus.PARTIAL,
            contextId = "ctx1",
            appsRestored = 2,
            appsSkipped = 1,
            actionsRestored = 3,
            actionsSkipped = 0,
            warnings = listOf("App unavailable"),
            errors = emptyList(),
            startedAt = 1000L,
            completedAt = 2000L,
        )
        assertEquals(2, result.appsRestored)
        assertEquals(1, result.appsSkipped)
        assertEquals(3, result.actionsRestored)
        assertEquals(1, result.warnings.size)
        assertTrue(result.errors.isEmpty())
    }

    // ── Helpers ──────────────────────────────────────────────

    private fun makePreview(
        appsAdded: Int = 0,
        appsRemoved: Int = 0,
        appsMissing: Int = 0,
        actionsAdded: Int = 0,
        actionsRemoved: Int = 0,
        actionsInvalid: Int = 0,
    ) = RestorePreview(
        capsuleName = "Test",
        targetContextName = "Target",
        changes = emptyList(),
        appsAdded = appsAdded,
        appsRemoved = appsRemoved,
        appsMissing = appsMissing,
        appsUnchanged = 0,
        actionsAdded = actionsAdded,
        actionsRemoved = actionsRemoved,
        actionsInvalid = actionsInvalid,
        actionsUnchanged = 0,
        contextNameChanged = false,
        contextDescriptionChanged = false,
    )

    private fun makeResult(status: RestoreStatus) = RestoreResult(
        status = status,
        contextId = "ctx1",
        appsRestored = 0,
        appsSkipped = 0,
        actionsRestored = 0,
        actionsSkipped = 0,
        warnings = emptyList(),
        errors = emptyList(),
        startedAt = 1000L,
        completedAt = 2000L,
    )
}
