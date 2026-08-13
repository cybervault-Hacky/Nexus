package com.nexus.app

import com.nexus.app.data.automation.safety.AutomationSafetyEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutomationSafetyTest {

    @Test
    fun `can execute within rate limit`() {
        assertTrue(AutomationSafetyEngine.canExecute("test_auto"))
    }

    @Test
    fun `rate limited after max executions`() {
        repeat(AutomationSafetyEngine.MAX_EXECUTIONS_PER_MINUTE) {
            AutomationSafetyEngine.recordExecution("test_rate")
        }
        assertFalse(AutomationSafetyEngine.canExecute("test_rate"))
    }

    @Test
    fun `remaining count decreases`() {
        val before = AutomationSafetyEngine.remainingInWindow("test_remaining")
        AutomationSafetyEngine.recordExecution("test_remaining")
        val after = AutomationSafetyEngine.remainingInWindow("test_remaining")
        assertEquals(before - 1, after)
    }

    @Test
    fun `different automations have independent limits`() {
        repeat(AutomationSafetyEngine.MAX_EXECUTIONS_PER_MINUTE) {
            AutomationSafetyEngine.recordExecution("auto_a")
        }
        assertTrue(AutomationSafetyEngine.canExecute("auto_b"))
    }
}
