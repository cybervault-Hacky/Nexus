package com.nexus.app

import com.nexus.app.data.automation.health.AutomationHealthEngine
import com.nexus.app.data.local.AutomationEntity
import com.nexus.app.data.local.AutomationExecutionEntity
import com.nexus.app.domain.model.smart.AutomationHealth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AutomationHealthTest {

    @Test
    fun `healthy automation with high success rate`() {
        val executions = (1..10).map { makeExecution("SUCCESS") }
        val stats = AutomationHealthEngine.calculateStats(executions)
        assertEquals(10, stats.totalExecutions)
        assertEquals(10, stats.successfulExecutions)
        assertTrue(stats.successRate > 0.9f)
    }

    @Test
    fun `failing automation with low success rate`() {
        val executions = (1..10).map { makeExecution("FAILED") }
        val stats = AutomationHealthEngine.calculateStats(executions)
        assertEquals(10, stats.failedExecutions)
        assertEquals(0f, stats.successRate)
    }

    @Test
    fun `health score for healthy automation`() {
        val executions = (1..10).map { makeExecution("SUCCESS") }
        val stats = AutomationHealthEngine.calculateStats(executions)
        val score = AutomationHealthEngine.healthScore(stats)
        assertTrue(score >= 90)
    }

    @Test
    fun `health score for failing automation`() {
        val executions = (1..10).map { makeExecution("FAILED") }
        val stats = AutomationHealthEngine.calculateStats(executions)
        val score = AutomationHealthEngine.healthScore(stats)
        assertTrue(score <= 10)
    }

    @Test
    fun `health score for empty executions`() {
        val stats = AutomationHealthEngine.calculateStats(emptyList())
        val score = AutomationHealthEngine.healthScore(stats)
        assertEquals(50, score)
    }

    private fun makeExecution(status: String) = AutomationExecutionEntity(
        id = "e1", automationId = "a1", startedAt = 1000, completedAt = 2000,
        status = status, triggerType = "MANUAL", contextId = "c1",
        successfulActions = 1, failedActions = 0, errorMessage = null,
    )
}
