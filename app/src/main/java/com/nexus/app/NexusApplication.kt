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
import com.nexus.app.data.automation.TriggerEngine
import com.nexus.app.data.local.NexusDatabase
import com.nexus.app.data.repository.ActionRepositoryImpl
import com.nexus.app.data.repository.AutomationRepositoryImpl
import com.nexus.app.data.repository.CapsuleRepositoryImpl
import com.nexus.app.data.repository.ContextAppRepositoryImpl
import com.nexus.app.data.repository.ContextRepositoryImpl
import com.nexus.app.data.repository.InstalledAppRepositoryImpl
import com.nexus.app.data.restore.CapsuleRestoreEngine
import com.nexus.app.data.restore.RestorePreviewEngine
import com.nexus.app.domain.model.ActionType
import com.nexus.app.domain.repository.ActionRepository
import com.nexus.app.domain.repository.AutomationRepository
import com.nexus.app.domain.repository.CapsuleRepository
import com.nexus.app.domain.repository.ContextAppRepository
import com.nexus.app.domain.repository.ContextRepository
import com.nexus.app.domain.repository.InstalledAppRepository

class NexusApplication : Application() {

    lateinit var contextRepository: ContextRepository
        private set
    lateinit var contextAppRepository: ContextAppRepository
        private set
    lateinit var installedAppRepository: InstalledAppRepository
        private set
    lateinit var actionRepository: ActionRepository
        private set
    lateinit var capsuleRepository: CapsuleRepository
        private set
    lateinit var automationRepository: AutomationRepository
        private set
    lateinit var appLauncher: AppLauncher
        private set
    lateinit var workflowExecutor: WorkflowExecutor
        private set
    lateinit var restorePreviewEngine: RestorePreviewEngine
        private set
    lateinit var capsuleRestoreEngine: CapsuleRestoreEngine
        private set
    lateinit var triggerEngine: TriggerEngine
        private set
    lateinit var automationScheduler: AutomationScheduler
        private set

    override fun onCreate() {
        super.onCreate()

        val database = NexusDatabase.getInstance(applicationContext)
        val appDataSource = InstalledAppDataSource(applicationContext)

        contextRepository = ContextRepositoryImpl(database.contextDao())
        installedAppRepository = InstalledAppRepositoryImpl(appDataSource)
        contextAppRepository = ContextAppRepositoryImpl(
            contextAppDao = database.contextAppDao(),
            contextDao = database.contextDao(),
            installedAppRepository = installedAppRepository,
        )
        appLauncher = AppLauncher(applicationContext)

        actionRepository = ActionRepositoryImpl(
            actionDao = database.actionDao(),
            contextDao = database.contextDao(),
        )

        capsuleRepository = CapsuleRepositoryImpl(
            database = database,
            capsuleDao = database.capsuleDao(),
            capsuleAppDao = database.capsuleAppDao(),
            capsuleActionDao = database.capsuleActionDao(),
            contextDao = database.contextDao(),
            contextAppDao = database.contextAppDao(),
            actionDao = database.actionDao(),
        )

        automationRepository = AutomationRepositoryImpl(
            automationDao = database.automationDao(),
            executionDao = database.automationExecutionDao(),
        )

        val actionExecutor = ActionExecutor(
            handlers = mapOf(
                ActionType.OPEN_APP to OpenAppHandler(appLauncher),
                ActionType.OPEN_URL to OpenUrlHandler(applicationContext),
                ActionType.DELAY to DelayHandler(),
            )
        )
        workflowExecutor = WorkflowExecutor(actionExecutor)

        restorePreviewEngine = RestorePreviewEngine(installedAppRepository)
        capsuleRestoreEngine = CapsuleRestoreEngine(
            database = database,
            contextDao = database.contextDao(),
            contextAppDao = database.contextAppDao(),
            actionDao = database.actionDao(),
            installedAppRepository = installedAppRepository,
        )

        triggerEngine = TriggerEngine(automationRepository, actionRepository, workflowExecutor)
        automationScheduler = AutomationScheduler(applicationContext)
    }
}
