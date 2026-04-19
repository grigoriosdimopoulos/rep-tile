package com.reptile.gymtracker.data.datastore

import androidx.camera.core.CameraSelector
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class UserPreferences(
    val useImperialUnits: Boolean = false,
    val cameraLensFacing: Int = CameraSelector.LENS_FACING_FRONT,
    val repSoundEnabled: Boolean = true,
    val autoDetectExercise: Boolean = true,
    val inactivityTimeoutSeconds: Int = 10
)

@Singleton
class UserPreferencesDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val KEY_IMPERIAL = booleanPreferencesKey("use_imperial_units")
        val KEY_CAMERA_FACING = intPreferencesKey("camera_lens_facing")
        val KEY_REP_SOUND = booleanPreferencesKey("rep_sound_enabled")
        val KEY_AUTO_DETECT = booleanPreferencesKey("auto_detect_exercise")
        val KEY_INACTIVITY_TIMEOUT = intPreferencesKey("inactivity_timeout_seconds")
    }

    val preferencesFlow: Flow<UserPreferences> = dataStore.data.map { prefs ->
        UserPreferences(
            useImperialUnits = prefs[KEY_IMPERIAL] ?: false,
            cameraLensFacing = prefs[KEY_CAMERA_FACING] ?: CameraSelector.LENS_FACING_FRONT,
            repSoundEnabled = prefs[KEY_REP_SOUND] ?: true,
            autoDetectExercise = prefs[KEY_AUTO_DETECT] ?: true,
            inactivityTimeoutSeconds = prefs[KEY_INACTIVITY_TIMEOUT] ?: 10
        )
    }

    suspend fun setImperialUnits(imperial: Boolean) {
        dataStore.edit { it[KEY_IMPERIAL] = imperial }
    }

    suspend fun setCameraLensFacing(facing: Int) {
        dataStore.edit { it[KEY_CAMERA_FACING] = facing }
    }

    suspend fun setRepSoundEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_REP_SOUND] = enabled }
    }

    suspend fun setAutoDetectExercise(enabled: Boolean) {
        dataStore.edit { it[KEY_AUTO_DETECT] = enabled }
    }

    suspend fun setInactivityTimeout(seconds: Int) {
        dataStore.edit { it[KEY_INACTIVITY_TIMEOUT] = seconds }
    }
}
