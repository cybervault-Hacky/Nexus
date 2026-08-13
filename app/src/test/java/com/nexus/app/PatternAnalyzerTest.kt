package com.nexus.app

import com.nexus.app.data.automation.pattern.PatternAnalyzer
import com.nexus.app.data.automation.pattern.PatternType
import com.nexus.app.data.local.AutomationEntity
import com.nexus.app.data.local.AutomationExecutionEntity
import com.nexus.app.data.local.EventHistoryEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PatternAnalyzerTest {

    @Test
    fun `detects frequently triggered automation`() {
        val rules = listOf(makeRule("a1", "Test Rule"))
        val executions = (1..10).map { makeExecution("a1", "SUCCESS") }
        val events = emptyList<EventHistoryEntity>()

        val insights = PatternAnalyzer.analyzePatterns(executions, events, rules)
        assertTrue(insights.any { it.type == PatternType.FREQUENT_TRIGGER })
    }

    @Test
    fun `detects frequently failed automation`() {
        val rules = listOf(makeRule("a1", "Test Rule"))
        val executions = (1..5).map { makeExecution("a1", "FAILED") }
        val events = emptyList<EventHistoryEntity>()

        val insights = PatternAnalyzer.analyzePatterns(executions, events, rules)
        assertTrue(insights.any { it.type == PatternType.FREQUENT_FAILURE })
    }

    @Test
    fun `detects unused automation`() {
        val rules = listOf(makeRule("a1", "Unused Rule"))
        val executions = emptyList<AutomationExecutionEntity>()
        val events = emptyList<EventHistoryEntity>()

        val insights = PatternAnalyzer.analyzePatterns(executions, events, rules)
        assertTrue(insights.any { it.type == PatternType.UNUSED })
    }

    @Test
    fun `generates suggestions for failures`() {
        val rules = listOf(makeRule("a1", "Failing Rule"))
        val insights = listOf(
            com.nexus.app.data.automation.pattern.PatternInsight(
                type = PatternType.FREQUENT_FAILURE,
                description = "Failed 5 times",
                automationId = "a1",
                frequency = 5,
            )
        )
        val suggestions = PatternAnalyzer.generateSuggestions(insights, rules)
        assertEquals(1, suggestions.size)
    }

    private fun makeRule(id: String, name: String) = AutomationEntity(
        id = id, name = name, description = "", isEnabled = true,
        triggerType = "MANUAL", triggerPayload = "{}", contextId = "ctx1",
        cooldownSeconds = 60, lastTriggeredAt = null, createdAt = 1000, updatedAt = 1000,
    )

    private fun makeExecution(autoId: String, status: String) = AutomationExecutionEntity(
        id = "e_${autoId}_${System.nanoTime()}", automationId = autoId,
        startedAt = 1000, completedAt = 2000, status = status,
        triggerType = "MANUAL", contextId = "ctx1",
        successfulActions = 1, failedActions = 0, errorMessage = null,
    )
}
