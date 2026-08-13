package com.nexus.app

import android.app.Application
import com.nexus.app.data.action.ActionExecutor
import com.nexus.app.data.action.DelayHandler
import com.nexus.app.data.action.OpenAppHandler
import com.nexus.app.data.action.OpenUrlHandler
import com.nexus.app.data.action.WorkflowExecutor
import com.nexus.app.data.app.AppLauncher
import com.nexus.app.data.app.InstalledAppDataSource
import com.nexus.app.data.automation.AutomationScheduler
import com.nexus.app.data.automation.AutomationSettingsImpl
import com.nexus.app.data.automation.AutomationSimulator
import com.nexus.app.data.automation.CompositeTriggerEvaluator
import com.nexus.app.data.automation.EventDeduplicator
import com.nexus.app.data.automation.TriggerEngine
import com.nexus.app.data.automation.TriggerEventRouter
import com.nexus.app.data.automation.capability.CapabilityManager
import com.nexus.app.data.environment.battery.BatteryEventSource
import com.nexus.app.data.environment.bluetooth.BluetoothEventSource
import com.nexus.app.data.environment.boot.BootEventSource
import com.nexus.app.data.environment.calendar.CalendarEventSource
import com.nexus.app.data.environment.idle.IdleStateEventSource
import com.nexus.app.data.environment.location.GeofenceRepositoryImpl
import com.nexus.app.data.environment.nfc.NfcEventSource
import com.nexus.app.data.environment.notification.NotificationEventSource
import com.nexus.app.data.environment.power.PowerEventSource
import com.nexus.app.data.environment.screen.ScreenEventSource
import com.nexus.app.data.environment.wifi.WifiEventSource
import com.nexus.app.data.local.NexusDatabase
import com.nexus.app.data.repository.ActionRepositoryImpl
import com.nexus.app.data.repository.AutomationRepositoryImpl
import com.nexus.app.data.repository.CapsuleRepositoryImpl
import com.nexus.app.data.repository.ContextAppRepositoryImpl
import com.nexus.app.data.repository.ContextRepositoryImpl
import com.nexus.app.data.repository.EventHistoryRepositoryImpl
import com.nexus.app.data.repository.InstalledAppRepositoryImpl
import com.nexus.app.data.restore.CapsuleRestoreEngine
import com.nexus.app.data.restore.RestorePreviewEngine
import com.nexus.app.domain.event.EnvironmentEventSourceRegistry
import com.nexus.app.domain.model.ActionType
import com.nexus.app.domain.repository.ActionRepository
import com.nexus.app.domain.repository.AutomationRepository
import com.nexus.app.domain.repository.CapsuleRepository
import com.nexus.app.domain.repository.ContextAppRepository
import com.nexus.app.domain.repository.ContextRepository
import com.nexus.app.domain.repository.InstalledAppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class NexusApplication : Application() {

    // Repositories
    lateinit var contextRepository: ContextRepository; private set
    lateinit var contextAppRepository: ContextAppRepository; private set
    lateinit var installedAppRepository: InstalledAppRepository; private set
    lateinit var actionRepository: ActionRepository; private set
    lateinit var capsuleRepository: CapsuleRepository; private set
    lateinit var automationRepository: AutomationRepository; private set
    lateinit var eventHistoryRepository: EventHistoryRepositoryImpl; private set

    // Engines
    lateinit var appLauncher: AppLauncher; private set
    lateinit var workflowExecutor: WorkflowExecutor; private set
    lateinit var restorePreviewEngine: RestorePreviewEngine; private set
    lateinit var capsuleRestoreEngine: CapsuleRestoreEngine; private set
    lateinit var triggerEngine: TriggerEngine; private set
    lateinit var automationScheduler: AutomationScheduler; private set
    lateinit var eventSourceRegistry: EnvironmentEventSourceRegistry; private set
    lateinit var automationSettings: AutomationSettingsImpl; private set
    lateinit var triggerEventRouter: TriggerEventRouter; private set
    lateinit var capabilityManager: CapabilityManager; private set
    lateinit var geofenceRepository: GeofenceRepositoryImpl; private set
    lateinit var automationSimulator: AutomationSimulator; private set
    // Phase 10 engines
    lateinit var healthEngine: com.nexus.app.data.automation.health.AutomationHealthEngine; private set
    lateinit var safetyEngine: com.nexus.app.data.automation.safety.AutomationSafetyEngine; private set
    lateinit var nfcEventSource: NfcEventSource; private set
    lateinit var calendarEventSource: CalendarEventSource; private set
    lateinit var notificationEventSource: NotificationEventSource; private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        val database = NexusDatabase.getInstance(applicationContext)
        val appDataSource = InstalledAppDataSource(applicationContext)

        contextRepository = ContextRepositoryImpl(database.contextDao())
        installedAppRepository = InstalledAppRepositoryImpl(appDataSource)
        contextAppRepository = ContextAppRepositoryImpl(database.contextAppDao(), database.contextDao(), installedAppRepository)
        appLauncher = AppLauncher(applicationContext)
        actionRepository = ActionRepositoryImpl(database.actionDao(), database.contextDao())
        capsuleRepository = CapsuleRepositoryImpl(database, database.capsuleDao(), database.capsuleAppDao(), database.capsuleActionDao(), database.contextDao(), database.contextAppDao(), database.actionDao())
        automationRepository = AutomationRepositoryImpl(database.automationDao(), database.automationExecutionDao())
        eventHistoryRepository = EventHistoryRepositoryImpl(database.eventHistoryDao())

        val actionExecutor = ActionExecutor(mapOf(
            ActionType.OPEN_APP to OpenAppHandler(appLauncher),
            ActionType.OPEN_URL to OpenUrlHandler(applicationContext),
            ActionType.DELAY to DelayHandler(),
        ))
        workflowExecutor = WorkflowExecutor(actionExecutor)
        restorePreviewEngine = RestorePreviewEngine(installedAppRepository)
        capsuleRestoreEngine = CapsuleRestoreEngine(database, database.contextDao(), database.contextAppDao(), database.actionDao(), installedAppRepository)
        triggerEngine = TriggerEngine(automationRepository, actionRepository, workflowExecutor)
        automationScheduler = AutomationScheduler(applicationContext)

        // Phase 8 + 9: Environment event sources
        automationSettings = AutomationSettingsImpl(applicationContext)
        capabilityManager = CapabilityManager(applicationContext)
        geofenceRepository = GeofenceRepositoryImpl()
        nfcEventSource = NfcEventSource(applicationContext)
        calendarEventSource = CalendarEventSource(applicationContext)
        notificationEventSource = NotificationEventSource(applicationContext)

        eventSourceRegistry = EnvironmentEventSourceRegistry().apply {
            register(WifiEventSource(applicationContext))
            register(BluetoothEventSource(applicationContext))
            register(PowerEventSource(applicationContext))
            register(BatteryEventSource(applicationContext))
            register(BootEventSource(applicationContext))
            register(ScreenEventSource(applicationContext))
            register(IdleStateEventSource(applicationContext))
            register(nfcEventSource)
            register(calendarEventSource)
            register(notificationEventSource)
        }
        eventSourceRegistry.startEnabled()

        automationSimulator = AutomationSimulator(triggerEngine, automationRepository)

        triggerEventRouter = TriggerEventRouter(eventSourceRegistry, triggerEngine, EventDeduplicator(), automationSettings)
        triggerEventRouter.startRouting(appScope)
    }
}
