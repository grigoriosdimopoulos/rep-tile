package com.reptile.gymtracker.ui.activesession

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reptile.gymtracker.camera.PoseDetectionAnalyzer
import com.reptile.gymtracker.camera.model.DetectedPose
import com.reptile.gymtracker.core.util.CalorieCalculator
import com.reptile.gymtracker.data.model.ExerciseSet
import com.reptile.gymtracker.data.model.ExerciseType
import com.reptile.gymtracker.data.repository.SessionRepository
import com.reptile.gymtracker.data.repository.UserProfileRepository
import com.reptile.gymtracker.exercise.ExerciseDetector
import com.reptile.gymtracker.exercise.detector.ExercisePhase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActiveSessionUiState(
    val sessionId: Long = 0,
    val detectedExercise: ExerciseType = ExerciseType.UNKNOWN,
    val repCount: Int = 0,
    val setNumber: Int = 1,
    val elapsedSeconds: Long = 0,
    val setElapsedSeconds: Long = 0,
    val isPaused: Boolean = false,
    val currentPose: DetectedPose? = null,
    val exerciseConfidence: Float = 0f,
    val isEnded: Boolean = false,
    val currentPhase: ExercisePhase = ExercisePhase.NEUTRAL
)

@HiltViewModel
class ActiveSessionViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val userProfileRepository: UserProfileRepository,
    val poseAnalyzer: PoseDetectionAnalyzer,
    private val exerciseDetector: ExerciseDetector
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActiveSessionUiState())
    val uiState: StateFlow<ActiveSessionUiState> = _uiState

    private var weightKg: Float = 70f
    private var sessionStartMs: Long = 0L
    private var setStartMs: Long = 0L
    private var pauseStartMs: Long = 0L
    private var totalPauseMs: Long = 0L
    private var setTotalPauseMs: Long = 0L

    fun initialize(sessionId: Long) {
        if (_uiState.value.sessionId == sessionId) return
        _uiState.update { it.copy(sessionId = sessionId) }
        val now = System.currentTimeMillis()
        sessionStartMs = now
        setStartMs = now
        exerciseDetector.resetForNewSession()

        viewModelScope.launch {
            userProfileRepository.getProfile()?.let { profile ->
                weightKg = profile.weightKg
            }
        }

        viewModelScope.launch {
            while (true) {
                delay(1000)
                val state = _uiState.value
                if (!state.isPaused && !state.isEnded) {
                    val now2 = System.currentTimeMillis()
                    val elapsed = (now2 - sessionStartMs - totalPauseMs) / 1000
                    val setElapsed = (now2 - setStartMs - setTotalPauseMs) / 1000
                    _uiState.update { it.copy(elapsedSeconds = elapsed, setElapsedSeconds = setElapsed) }
                }
            }
        }

        viewModelScope.launch {
            poseAnalyzer.poseFlow.collect { pose ->
                val state = _uiState.value
                if (state.isPaused || state.isEnded) return@collect
                if (pose != null) {
                    val counterState = exerciseDetector.processFrame(pose)
                    _uiState.update { it.copy(
                        currentPose = pose,
                        detectedExercise = counterState.exerciseType,
                        repCount = counterState.repCount,
                        setNumber = counterState.setNumber,
                        currentPhase = counterState.currentPhase,
                        exerciseConfidence = counterState.confidence
                    )}
                } else {
                    _uiState.update { it.copy(currentPose = null) }
                }
            }
        }
    }

    fun pauseResume() {
        val now = System.currentTimeMillis()
        val state = _uiState.value
        if (!state.isPaused) {
            pauseStartMs = now
        } else {
            val pauseDuration = now - pauseStartMs
            totalPauseMs += pauseDuration
            setTotalPauseMs += pauseDuration
        }
        _uiState.update { it.copy(isPaused = !it.isPaused) }
    }

    fun endSet() {
        val state = _uiState.value
        if (state.repCount == 0) return
        viewModelScope.launch {
            val setDuration = state.setElapsedSeconds
            val calories = CalorieCalculator.calculate(
                state.detectedExercise, setDuration, weightKg
            )
            sessionRepository.addExerciseSet(
                ExerciseSet(
                    sessionId = state.sessionId,
                    exerciseType = state.detectedExercise,
                    setNumber = state.setNumber,
                    repCount = state.repCount,
                    durationSeconds = setDuration,
                    caloriesForSet = calories,
                    avgConfidence = state.exerciseConfidence
                )
            )
            exerciseDetector.startNewSet()
            val now = System.currentTimeMillis()
            setStartMs = now
            setTotalPauseMs = 0L
        }
    }

    fun endSession() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.repCount > 0) {
                endSet()
                delay(150)
            }
            val durationSeconds = state.elapsedSeconds
            val sets = sessionRepository.getSessionWithSets(state.sessionId)?.sets ?: emptyList()
            val totalCalories = sets.sumOf { it.caloriesForSet.toDouble() }.toFloat()
            sessionRepository.endSession(state.sessionId, durationSeconds, totalCalories)
            _uiState.update { it.copy(isEnded = true) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        poseAnalyzer.close()
    }
}
