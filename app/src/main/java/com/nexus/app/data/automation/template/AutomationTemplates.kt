package com.nexus.app.data.automation.template

import com.nexus.app.domain.model.automation.TriggerType

/**
 * Static local automation templates.
 * User must explicitly choose to use a template — never auto-activated.
 */
object AutomationTemplates {

    fun all(): List<AutomationTemplate> = listOf(
        AutomationTemplate(
            id = "tmpl_work",
            name = "Work Mode",
            description = "Activate your work context when arriving at the office",
            category = TemplateCategory.WORK,
            iconId = "work",
            suggestedTriggerType = TriggerType.WIFI_CONNECTED,
            suggestedTriggerPayload = "{}",
        ),
        AutomationTemplate(
            id = "tmpl_night",
            name = "Night Mode",
            description = "Run your evening routine at a configured time",
            category = TemplateCategory.HOME,
            iconId = "night",
            suggestedTriggerType = TriggerType.TIME,
            suggestedTriggerPayload = """{"hour":22,"minute":0,"daysOfWeek":127}""",
        ),
        AutomationTemplate(
            id = "tmpl_charging",
            name = "Charging Mode",
            description = "Run actions when your device starts charging",
            category = TemplateCategory.DEVICE,
            iconId = "charging",
            suggestedTriggerType = TriggerType.CHARGING_STARTED,
            suggestedTriggerPayload = "{}",
        ),
        AutomationTemplate(
            id = "tmpl_focus",
            name = "Focus Session",
            description = "Manually trigger a focused work session",
            category = TemplateCategory.PRODUCTIVITY,
            iconId = "focus",
            suggestedTriggerType = TriggerType.MANUAL,
            suggestedTriggerPayload = "{}",
        ),
        AutomationTemplate(
            id = "tmpl_travel",
            name = "Travel Mode",
            description = "Activate when connecting to a Bluetooth device",
            category = TemplateCategory.TRAVEL,
            iconId = "travel",
            suggestedTriggerType = TriggerType.BLUETOOTH_CONNECTED,
            suggestedTriggerPayload = "{}",
        ),
        AutomationTemplate(
            id = "tmpl_study",
            name = "Study Session",
            description = "Start a study session manually",
            category = TemplateCategory.STUDY,
            iconId = "study",
            suggestedTriggerType = TriggerType.MANUAL,
            suggestedTriggerPayload = "{}",
        ),
    )

    fun byCategory(category: TemplateCategory): List<AutomationTemplate> =
        all().filter { it.category == category }

    fun byId(id: String): AutomationTemplate? = all().find { it.id == id }
}

data class AutomationTemplate(
    val id: String,
    val name: String,
    val description: String,
    val category: TemplateCategory,
    val iconId: String,
    val suggestedTriggerType: TriggerType,
    val suggestedTriggerPayload: String,
)

enum class TemplateCategory {
    PRODUCTIVITY, TRAVEL, HOME, WORK, STUDY, MEDIA, COMMUNICATION, DEVICE, CUSTOM,
}
