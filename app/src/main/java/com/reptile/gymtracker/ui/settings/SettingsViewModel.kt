package com.reptile.gymtracker.ui.settings

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reptile.gymtracker.data.datastore.UserPreferencesDataStore
import com.reptile.gymtracker.data.export.DataExporter
import com.reptile.gymtracker.data.export.DataImporter
import com.reptile.gymtracker.data.export.ImportResult
import com.reptile.gymtracker.data.export.ImportStrategy
import com.reptile.gymtracker.data.model.ActivityLevel
import com.reptile.gymtracker.data.model.Gender
import com.reptile.gymtracker.data.model.UserProfile
import com.reptile.gymtracker.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userProfileRepository: UserProfileRepository,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val dataExporter: DataExporter,
    private val dataImporter: DataImporter
) : ViewModel() {

    val profile: StateFlow<UserProfile?> = userProfileRepository.getProfileFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val preferences = userPreferencesDataStore.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),
            com.reptile.gymtracker.data.datastore.UserPreferences())

    private val _exportUri = MutableStateFlow<Uri?>(null)
    val exportUri: StateFlow<Uri?> = _exportUri

    private val _importResult = MutableStateFlow<ImportResult?>(null)
    val importResult: StateFlow<ImportResult?> = _importResult

    private val _profileSaved = MutableStateFlow(false)
    val profileSaved: StateFlow<Boolean> = _profileSaved

    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch {
            userProfileRepository.saveProfile(profile)
            _profileSaved.value = true
        }
    }

    fun clearProfileSaved() {
        _profileSaved.value = false
    }

    fun setImperialUnits(imperial: Boolean) {
        viewModelScope.launch { userPreferencesDataStore.setImperialUnits(imperial) }
    }

    fun setRepSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { userPreferencesDataStore.setRepSoundEnabled(enabled) }
    }

    fun setAutoDetectExercise(enabled: Boolean) {
        viewModelScope.launch { userPreferencesDataStore.setAutoDetectExercise(enabled) }
    }

    fun setCameraFacingFront(front: Boolean) {
        viewModelScope.launch {
            val facing = if (front) androidx.camera.core.CameraSelector.LENS_FACING_FRONT
                         else androidx.camera.core.CameraSelector.LENS_FACING_BACK
            userPreferencesDataStore.setCameraLensFacing(facing)
        }
    }

    fun exportData() {
        viewModelScope.launch {
            try {
                val uri = dataExporter.export()
                _exportUri.value = uri
            } catch (e: Exception) {
                Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun clearExportUri() {
        _exportUri.value = null
    }

    fun importData(uri: Uri, strategy: ImportStrategy) {
        viewModelScope.launch {
            val result = dataImporter.import(uri, strategy)
            _importResult.value = result
        }
    }

    fun clearImportResult() {
        _importResult.value = null
    }
}
