package com.nexus.app

import com.nexus.app.domain.model.smart.ConditionOperator
import com.nexus.app.domain.model.smart.ConditionResult
import com.nexus.app.domain.model.smart.ConditionType
import com.nexus.app.domain.model.smart.SmartCondition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConditionTypeTest {

    @Test
    fun `ConditionType has expected count`() {
        assertTrue(ConditionType.entries.size >= 25)
    }

    @Test
    fun `ConditionOperator has three values`() {
        assertEquals(3, ConditionOperator.entries.size)
    }

    @Test
    fun `ConditionResult has four values`() {
        assertEquals(4, ConditionResult.entries.size)
    }

    @Test
    fun `SmartCondition Leaf holds data`() {
        val leaf = SmartCondition.Leaf(ConditionType.BATTERY_ABOVE, "30", "Battery above 30%")
        assertEquals(ConditionType.BATTERY_ABOVE, leaf.type)
        assertEquals("30", leaf.parameter)
    }

    @Test
    fun `SmartCondition Composite holds children`() {
        val child1 = SmartCondition.Leaf(ConditionType.WIFI_CONNECTED)
        val child2 = SmartCondition.Leaf(ConditionType.SCREEN_ON)
        val composite = SmartCondition.Composite(ConditionOperator.ALL, listOf(child1, child2))
        assertEquals(2, composite.children.size)
        assertEquals(ConditionOperator.ALL, composite.operator)
    }
}
