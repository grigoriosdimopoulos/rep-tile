package com.reptile.gymtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.reptile.gymtracker.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY startTimestamp DESC")
    fun getAllSessionsFlow(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions ORDER BY startTimestamp DESC LIMIT :limit")
    fun getRecentSessionsFlow(limit: Int = 10): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Delete
    suspend fun deleteSession(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE startTimestamp BETWEEN :from AND :to ORDER BY startTimestamp DESC")
    fun getSessionsInRange(from: Long, to: Long): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE startTimestamp >= :since ORDER BY startTimestamp DESC")
    fun getSessionsSince(since: Long): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions")
    suspend fun getAllSessionsOnce(): List<SessionEntity>
}
