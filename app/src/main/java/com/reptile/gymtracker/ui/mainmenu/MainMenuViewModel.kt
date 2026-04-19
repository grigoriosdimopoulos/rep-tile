package com.reptile.gymtracker.ui.mainmenu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reptile.gymtracker.data.model.SessionWithSets
import com.reptile.gymtracker.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainMenuViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    val recentSessions: StateFlow<List<SessionWithSets>> =
        sessionRepository.getRecentSessionsFlow(10)
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

    private val _newSessionId = MutableStateFlow<Long?>(null)
    val newSessionId: StateFlow<Long?> = _newSessionId

    fun startNewSession() {
        viewModelScope.launch {
            val sessionId = sessionRepository.startSession()
            _newSessionId.value = sessionId
        }
    }

    fun clearNewSessionId() {
        _newSessionId.value = null
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            sessionRepository.deleteSession(sessionId)
        }
    }
}
