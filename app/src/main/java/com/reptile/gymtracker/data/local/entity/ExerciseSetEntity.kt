package com.reptile.gymtracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.reptile.gymtracker.data.model.ExerciseSet
import com.reptile.gymtracker.data.model.ExerciseType

@Entity(
    tableName = "exercise_sets",
    foreignKeys = [ForeignKey(
        entity = SessionEntity::class,
        parentColumns = ["id"],
        childColumns = ["sessionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("sessionId")]
)
data class ExerciseSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseType: String,
    val setNumber: Int,
    val repCount: Int,
    val durationSeconds: Long,
    val caloriesForSet: Float,
    val avgConfidence: Float
)

fun ExerciseSetEntity.toDomain() = ExerciseSet(
    id = id,
    sessionId = sessionId,
    exerciseType = ExerciseType.fromName(exerciseType),
    setNumber = setNumber,
    repCount = repCount,
    durationSeconds = durationSeconds,
    caloriesForSet = caloriesForSet,
    avgConfidence = avgConfidence
)

fun ExerciseSet.toEntity() = ExerciseSetEntity(
    id = id,
    sessionId = sessionId,
    exerciseType = exerciseType.name,
    setNumber = setNumber,
    repCount = repCount,
    durationSeconds = durationSeconds,
    caloriesForSet = caloriesForSet,
    avgConfidence = avgConfidence
)
