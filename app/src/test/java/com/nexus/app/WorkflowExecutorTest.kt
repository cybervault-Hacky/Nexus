package com.nexus.app

import com.nexus.app.data.action.ActionExecutor
import com.nexus.app.data.action.WorkflowExecutor
import com.nexus.app.domain.model.ActionPayload
import com.nexus.app.domain.model.ActionResult
import com.nexus.app.domain.model.ActionType
import com.nexus.app.domain.model.NexusAction
import com.nexus.app.domain.model.WorkflowState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests for the WorkflowExecutor using a fake ActionHandler.
 * Does not require Android framework.
 */
class WorkflowExecutorTest {

    // A fake handler that always succeeds
    private val successHandler = object : com.nexus.app.data.action.ActionHandler {
        override suspend fun execute(actionId: String, payload: ActionPayload, startTime: Long): ActionResult {
            return ActionResult.Success(actionId, startTime, System.currentTimeMillis())
        }
    }

    // A fake handler that always fails
    private val failHandler = object : com.nexus.app.data.action.ActionHandler {
        override suspend fun execute(actionId: String, payload: ActionPayload, startTime: Long): ActionResult {
            return ActionResult.Failed(actionId, startTime, System.currentTimeMillis(), "Test failure")
        }
    }

    private fun makeAction(
        id: String = "a1",
        type: ActionType = ActionType.OPEN_APP,
        isEnabled: Boolean = true,
        position: Int = 0,
    ) = NexusAction(
        id = id,
        contextId = "ctx1",
        name = "Action $id",
        description = "",
        type = type,
        payload = ActionPayload.OpenApp("com.example").toJson(),
        isEnabled = isEnabled,
        position = position,
    )

    @Test
    fun `empty workflow completes successfully`() = runTest {
        val executor = WorkflowExecutor(ActionExecutor(emptyMap()))
        val result = executor.execute(emptyList())
        assertTrue(result.overallSuccess)
        assertEquals(0, result.totalCount)
    }

    @Test
    fun `single successful action completes`() = runTest {
        val actionExecutor = ActionExecutor(mapOf(ActionType.OPEN_APP to successHandler))
        val workflowExecutor = WorkflowExecutor(actionExecutor)
        val result = workflowExecutor.execute(listOf(makeAction()))
        assertTrue(result.overallSuccess)
        assertEquals(1, result.completedCount)
        assertEquals(1, result.totalCount)
    }

    @Test
    fun `disabled actions are skipped`() = runTest {
        val actionExecutor = ActionExecutor(mapOf(ActionType.OPEN_APP to successHandler))
        val workflowExecutor = WorkflowExecutor(actionExecutor)
        val actions = listOf(
            makeAction(id = "a1", isEnabled = false),
            makeAction(id = "a2", isEnabled = true),
        )
        val result = workflowExecutor.execute(actions)
        assertTrue(result.overallSuccess)
        assertEquals(1, result.completedCount)
        assertEquals(1, result.totalCount)
    }

    @Test
    fun `failure stops execution`() = runTest {
        val actionExecutor = ActionExecutor(mapOf(ActionType.OPEN_APP to failHandler))
        val workflowExecutor = WorkflowExecutor(actionExecutor)
        val actions = listOf(
            makeAction(id = "a1"),
            makeAction(id = "a2"),
            makeAction(id = "a3"),
        )
        val result = workflowExecutor.execute(actions)
        assertTrue(!result.overallSuccess)
        assertEquals(0, result.completedCount)
        assertEquals(1, result.actionResults.size) // Only first action executed
    }

    @Test
    fun `successful sequence completes all`() = runTest {
        val actionExecutor = ActionExecutor(mapOf(ActionType.OPEN_APP to successHandler))
        val workflowExecutor = WorkflowExecutor(actionExecutor)
        val actions = listOf(
            makeAction(id = "a1", position = 0),
            makeAction(id = "a2", position = 1),
            makeAction(id = "a3", position = 2),
        )
        val result = workflowExecutor.execute(actions)
        assertTrue(result.overallSuccess)
        assertEquals(3, result.completedCount)
        assertEquals(3, result.actionResults.size)
    }

    @Test
    fun `all disabled actions result in empty workflow`() = runTest {
        val actionExecutor = ActionExecutor(mapOf(ActionType.OPEN_APP to successHandler))
        val workflowExecutor = WorkflowExecutor(actionExecutor)
        val actions = listOf(
            makeAction(id = "a1", isEnabled = false),
            makeAction(id = "a2", isEnabled = false),
        )
        val result = workflowExecutor.execute(actions)
        assertTrue(result.overallSuccess)
        assertEquals(0, result.totalCount)
    }

    @Test
    fun `cancel stops execution`() = runTest {
        // Use a slow handler
        val slowHandler = object : com.nexus.app.data.action.ActionHandler {
            override suspend fun execute(actionId: String, payload: ActionPayload, startTime: Long): ActionResult {
                kotlinx.coroutines.delay(5000)
                return ActionResult.Success(actionId, startTime, System.currentTimeMillis())
            }
        }
        val actionExecutor = ActionExecutor(mapOf(ActionType.OPEN_APP to slowHandler))
        val workflowExecutor = WorkflowExecutor(actionExecutor)
        val actions = listOf(
            makeAction(id = "a1"),
            makeAction(id = "a2"),
        )

        // Start and cancel
        kotlinx.coroutines.launch {
            kotlinx.coroutines.delay(100)
            workflowExecutor.cancel()
        }

        val result = workflowExecutor.execute(actions)
        // Should be cancelled or have fewer completed
        assertTrue(result.completedCount <= 1)
    }
}
