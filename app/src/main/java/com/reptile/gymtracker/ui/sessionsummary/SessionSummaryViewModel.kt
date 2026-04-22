package com.reptile.gymtracker.ui.sessionsummary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reptile.gymtracker.core.util.CalorieCalculator
import com.reptile.gymtracker.data.model.ExerciseSet
import com.reptile.gymtracker.data.model.ExerciseType
import com.reptile.gymtracker.data.model.SessionWithSets
import com.reptile.gymtracker.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExerciseSummary(
    val exerciseType: ExerciseType,
    val sets: Int,
    val reps: Int,
    val calories: Float
)

@HiltViewModel
class SessionSummaryViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _sessionWithSets = MutableStateFlow<SessionWithSets?>(null)
    val sessionWithSets: StateFlow<SessionWithSets?> = _sessionWithSets

    private var loadJob: Job? = null

    fun load(sessionId: Long) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            sessionRepository.getSessionWithSetsFlow(sessionId).collect { sws ->
                _sessionWithSets.value = sws
            }
        }
    }

    fun getExerciseSummaries(): List<ExerciseSummary> {
        val sws = _sessionWithSets.value ?: return emptyList()
        return sws.sets
            .groupBy { it.exerciseType }
            .map { (type, sets) ->
                ExerciseSummary(
                    exerciseType = type,
                    sets = sets.size,
                    reps = sets.sumOf { it.repCount },
                    calories = sets.sumOf { it.caloriesForSet.toDouble() }.toFloat()
                )
            }
    }

    fun updateSetReps(set: ExerciseSet, newRepCount: Int) {
        viewModelScope.launch {
            sessionRepository.updateExerciseSet(set.copy(repCount = newRepCount))
        }
    }

    fun deleteSet(setId: Long) {
        viewModelScope.launch {
            sessionRepository.deleteExerciseSet(setId)
        }
    }

    fun addManualSet(exerciseType: ExerciseType, repCount: Int) {
        viewModelScope.launch {
            val sws = _sessionWithSets.value ?: return@launch
            val nextSetNumber = (sws.sets.maxOfOrNull { it.setNumber } ?: 0) + 1
            sessionRepository.addExerciseSet(
                ExerciseSet(
                    sessionId = sws.session.id,
                    exerciseType = exerciseType,
                    setNumber = nextSetNumber,
                    repCount = repCount,
                    caloriesForSet = CalorieCalculator.calculate(exerciseType, 0L)
                )
            )
        }
    }
}
