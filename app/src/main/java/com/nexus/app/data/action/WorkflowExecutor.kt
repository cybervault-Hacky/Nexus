package com.nexus.app.data.action

import com.nexus.app.domain.model.ActionResult
import com.nexus.app.domain.model.NexusAction
import com.nexus.app.domain.model.WorkflowResult
import com.nexus.app.domain.model.WorkflowState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Executes a sequence of actions (a workflow) sequentially.
 * Reports progress via [state] and supports cancellation.
 *
 * Execution policy: stop on first failure.
 */
class WorkflowExecutor(
    private val actionExecutor: ActionExecutor,
) {

    private val _state = MutableStateFlow<WorkflowState>(WorkflowState.Idle)
    val state: StateFlow<WorkflowState> = _state.asStateFlow()

    private var currentJob: Job? = null

    /**
     * Execute a workflow (ordered list of actions).
     * Disabled actions are skipped.
     * Execution stops on the first failure.
     */
    suspend fun execute(actions: List<NexusAction>): WorkflowResult {
        val enabledActions = actions.filter { it.isEnabled }
        if (enabledActions.isEmpty()) {
            return WorkflowResult(
                actionResults = emptyList(),
                completedCount = 0,
                totalCount = 0,
                overallSuccess = true,
            )
        }

        val results = mutableListOf<ActionResult>()
        var completedCount = 0

        try {
            coroutineScope {
                currentJob = launch {
                    for ((index, action) in enabledActions.withIndex()) {
                        if (!isActive) {
                            // Cancellation detected
                            results.add(
                                ActionResult.Cancelled(action.id, System.currentTimeMillis(), System.currentTimeMillis())
                            )
                            break
                        }

                        _state.value = WorkflowState.Running(
                            currentActionId = action.id,
                            currentActionName = action.name,
                            completedCount = completedCount,
                            totalCount = enabledActions.size,
                        )

                        val result = actionExecutor.execute(action)
                        results.add(result)

                        when (result) {
                            is ActionResult.Success -> {
                                completedCount++
                            }
                            is ActionResult.Failed -> {
                                // Stop execution on failure
                                val workflowResult = WorkflowResult(
                                    actionResults = results.toList(),
                                    completedCount = completedCount,
                                    totalCount = enabledActions.size,
                                    overallSuccess = false,
                                )
                                _state.value = WorkflowState.Failed(workflowResult)
                                return@launch
                            }
                            is ActionResult.Cancelled -> {
                                val workflowResult = WorkflowResult(
                                    actionResults = results.toList(),
                                    completedCount = completedCount,
                                    totalCount = enabledActions.size,
                                    overallSuccess = false,
                                )
                                _state.value = WorkflowState.Cancelled(workflowResult)
                                return@launch
                            }
                        }
                    }

                    // All actions completed successfully
                    val workflowResult = WorkflowResult(
                        actionResults = results.toList(),
                        completedCount = completedCount,
                        totalCount = enabledActions.size,
                        overallSuccess = true,
                    )
                    _state.value = WorkflowState.Completed(workflowResult)
                }
            }
        } catch (_: CancellationException) {
            // Coroutine was cancelled externally
            results.add(
                ActionResult.Cancelled("cancelled", System.currentTimeMillis(), System.currentTimeMillis())
            )
        }

        return WorkflowResult(
            actionResults = results.toList(),
            completedCount = completedCount,
            totalCount = enabledActions.size,
            overallSuccess = results.all { it is ActionResult.Success },
        )
    }

    /** Cancel the currently running workflow. */
    fun cancel() {
        currentJob?.cancel()
        currentJob = null
        val currentState = _state.value
        if (currentState is WorkflowState.Running) {
            _state.value = WorkflowState.Cancelled(
                WorkflowResult(
                    actionResults = emptyList(),
                    completedCount = currentState.completedCount,
                    totalCount = currentState.totalCount,
                    overallSuccess = false,
                )
            )
        }
    }

    /** Reset to idle state. */
    fun reset() {
        _state.value = WorkflowState.Idle
    }
}
