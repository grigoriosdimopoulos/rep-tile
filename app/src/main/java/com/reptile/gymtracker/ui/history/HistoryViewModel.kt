package com.reptile.gymtracker.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reptile.gymtracker.data.model.ExerciseType
import com.reptile.gymtracker.data.model.SessionWithSets
import com.reptile.gymtracker.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

enum class TimeFilter { THIS_WEEK, THIS_MONTH, ALL_TIME }

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    val timeFilter = MutableStateFlow(TimeFilter.ALL_TIME)
    val exerciseTypeFilter = MutableStateFlow<ExerciseType?>(null)

    private val allSessionsWithSets: StateFlow<List<SessionWithSets>> =
        sessionRepository.getAllSessionsFlow()
            .map { sessions ->
                sessions.map { session ->
                    val sets = sessionRepository.getSessionWithSets(session.id)?.sets
                        ?: emptyList()
                    SessionWithSets(session, sets)
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val filteredSessions: StateFlow<List<SessionWithSets>> = combine(
        allSessionsWithSets, timeFilter, exerciseTypeFilter
    ) { sessions, time, exercise ->
        val timeFiltered = when (time) {
            TimeFilter.THIS_WEEK -> {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                sessions.filter { it.session.startTimestamp >= cal.timeInMillis }
            }
            TimeFilter.THIS_MONTH -> {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                sessions.filter { it.session.startTimestamp >= cal.timeInMillis }
            }
            TimeFilter.ALL_TIME -> sessions
        }
        exercise?.let { type ->
            timeFiltered.filter { sws -> sws.sets.any { it.exerciseType == type } }
        } ?: timeFiltered
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            sessionRepository.deleteSession(sessionId)
        }
    }
}
