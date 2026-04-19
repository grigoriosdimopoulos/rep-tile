package com.reptile.gymtracker.data.repository

import com.reptile.gymtracker.data.model.ExerciseSet
import com.reptile.gymtracker.data.model.Session
import com.reptile.gymtracker.data.model.SessionWithSets
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun getAllSessionsFlow(): Flow<List<Session>>
    fun getRecentSessionsFlow(limit: Int = 10): Flow<List<Session>>
    fun getSessionWithSetsFlow(sessionId: Long): Flow<SessionWithSets?>
    fun getSessionsSinceFlow(since: Long): Flow<List<Session>>
    suspend fun getSessionWithSets(sessionId: Long): SessionWithSets?
    suspend fun startSession(): Long
    suspend fun endSession(sessionId: Long, durationSeconds: Long, totalCalories: Float)
    suspend fun addExerciseSet(set: ExerciseSet): Long
    suspend fun updateExerciseSet(set: ExerciseSet)
    suspend fun deleteExerciseSet(setId: Long)
    suspend fun deleteSession(sessionId: Long)
    suspend fun getAllSessionsWithSets(): List<SessionWithSets>
    suspend fun importSessionWithSets(sessionWithSets: SessionWithSets): Long
    suspend fun deleteAllSessions()
}
