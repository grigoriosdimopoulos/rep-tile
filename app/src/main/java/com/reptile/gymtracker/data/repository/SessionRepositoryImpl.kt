package com.reptile.gymtracker.data.repository

import com.reptile.gymtracker.data.local.dao.ExerciseSetDao
import com.reptile.gymtracker.data.local.dao.SessionDao
import com.reptile.gymtracker.data.local.entity.toDomain
import com.reptile.gymtracker.data.local.entity.toEntity
import com.reptile.gymtracker.data.model.ExerciseSet
import com.reptile.gymtracker.data.model.Session
import com.reptile.gymtracker.data.model.SessionWithSets
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao,
    private val exerciseSetDao: ExerciseSetDao
) : SessionRepository {

    override fun getAllSessionsFlow(): Flow<List<Session>> =
        sessionDao.getAllSessionsFlow().map { list -> list.map { it.toDomain() } }

    override fun getRecentSessionsFlow(limit: Int): Flow<List<Session>> =
        sessionDao.getRecentSessionsFlow(limit).map { list -> list.map { it.toDomain() } }

    override fun getSessionWithSetsFlow(sessionId: Long): Flow<SessionWithSets?> =
        combine(
            sessionDao.getAllSessionsFlow().map { list ->
                list.firstOrNull { it.id == sessionId }?.toDomain()
            },
            exerciseSetDao.getSetsForSessionFlow(sessionId).map { list ->
                list.map { it.toDomain() }
            }
        ) { session, sets ->
            session?.let { SessionWithSets(it, sets) }
        }

    override fun getSessionsSinceFlow(since: Long): Flow<List<Session>> =
        sessionDao.getSessionsSince(since).map { list -> list.map { it.toDomain() } }

    override suspend fun getSessionWithSets(sessionId: Long): SessionWithSets? {
        val session = sessionDao.getSessionById(sessionId)?.toDomain() ?: return null
        val sets = exerciseSetDao.getSetsForSession(sessionId).map { it.toDomain() }
        return SessionWithSets(session, sets)
    }

    override suspend fun startSession(): Long {
        val entity = Session(startTimestamp = System.currentTimeMillis()).toEntity()
        return sessionDao.insertSession(entity)
    }

    override suspend fun endSession(sessionId: Long, durationSeconds: Long, totalCalories: Float) {
        val entity = sessionDao.getSessionById(sessionId) ?: return
        sessionDao.updateSession(
            entity.copy(
                endTimestamp = System.currentTimeMillis(),
                durationSeconds = durationSeconds,
                totalCalories = totalCalories
            )
        )
    }

    override suspend fun addExerciseSet(set: ExerciseSet): Long =
        exerciseSetDao.insertSet(set.toEntity())

    override suspend fun updateExerciseSet(set: ExerciseSet) =
        exerciseSetDao.updateSet(set.toEntity())

    override suspend fun deleteExerciseSet(setId: Long) =
        exerciseSetDao.deleteSetById(setId)

    override suspend fun deleteSession(sessionId: Long) {
        val entity = sessionDao.getSessionById(sessionId) ?: return
        sessionDao.deleteSession(entity)
    }

    override suspend fun getAllSessionsWithSets(): List<SessionWithSets> {
        val sessions = sessionDao.getAllSessionsOnce()
        return sessions.map { sessionEntity ->
            val sets = exerciseSetDao.getSetsForSession(sessionEntity.id).map { it.toDomain() }
            SessionWithSets(sessionEntity.toDomain(), sets)
        }
    }

    override suspend fun importSessionWithSets(sessionWithSets: SessionWithSets): Long {
        val sessionId = sessionDao.insertSession(
            sessionWithSets.session.copy(id = 0).toEntity()
        )
        val sets = sessionWithSets.sets.map {
            it.copy(id = 0, sessionId = sessionId).toEntity()
        }
        exerciseSetDao.insertSets(sets)
        return sessionId
    }

    override suspend fun deleteAllSessions() {
        val sessions = sessionDao.getAllSessionsOnce()
        sessions.forEach { sessionDao.deleteSession(it) }
    }
}
