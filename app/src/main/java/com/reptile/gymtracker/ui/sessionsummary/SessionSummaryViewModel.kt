package com.reptile.gymtracker.ui.sessionsummary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reptile.gymtracker.data.model.ExerciseType
import com.reptile.gymtracker.data.model.Session
import com.reptile.gymtracker.data.model.SessionWithSets
import com.reptile.gymtracker.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun load(sessionId: Long) {
        viewModelScope.launch {
            _sessionWithSets.value = sessionRepository.getSessionWithSets(sessionId)
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
}
