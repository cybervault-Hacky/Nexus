package com.nexus.app.data.environment.location

import com.nexus.app.domain.model.automation.TriggerConfig

/**
 * Repository for managing geofence registrations.
 * Stores geofence definitions and coordinates with the system geofencing API.
 */
interface GeofenceRepository {
    suspend fun registerGeofence(config: TriggerConfig.Geofence)
    suspend fun unregisterGeofence(geofenceId: String)
    suspend fun unregisterAll()
    suspend fun getRegisteredGeofences(): List<TriggerConfig.Geofence>
}

/**
 * In-memory implementation — geofences are re-registered on app start.
 * Persisted geofence configs live in the automation_rules triggerPayload.
 */
class GeofenceRepositoryImpl : GeofenceRepository {
    private val geofences = mutableMapOf<String, TriggerConfig.Geofence>()

    override suspend fun registerGeofence(config: TriggerConfig.Geofence) {
        geofences[config.geofenceId] = config
    }

    override suspend fun unregisterGeofence(geofenceId: String) {
        geofences.remove(geofenceId)
    }

    override suspend fun unregisterAll() {
        geofences.clear()
    }

    override suspend fun getRegisteredGeofences(): List<TriggerConfig.Geofence> =
        geofences.values.toList()
}
