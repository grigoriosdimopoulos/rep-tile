package com.reptile.gymtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.reptile.gymtracker.data.local.entity.ExerciseSetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseSetDao {
    @Query("SELECT * FROM exercise_sets WHERE sessionId = :sessionId ORDER BY setNumber ASC")
    fun getSetsForSessionFlow(sessionId: Long): Flow<List<ExerciseSetEntity>>

    @Query("SELECT * FROM exercise_sets WHERE sessionId = :sessionId ORDER BY setNumber ASC")
    suspend fun getSetsForSession(sessionId: Long): List<ExerciseSetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: ExerciseSetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(sets: List<ExerciseSetEntity>)

    @Update
    suspend fun updateSet(set: ExerciseSetEntity)

    @Query("DELETE FROM exercise_sets WHERE id = :setId")
    suspend fun deleteSetById(setId: Long)

    @Query("DELETE FROM exercise_sets WHERE sessionId = :sessionId")
    suspend fun deleteSetsForSession(sessionId: Long)

    @Query("SELECT * FROM exercise_sets")
    suspend fun getAllSetsOnce(): List<ExerciseSetEntity>

    @Query("SELECT COUNT(*) FROM exercise_sets WHERE sessionId = :sessionId")
    suspend fun getSetCountForSession(sessionId: Long): Int
}
