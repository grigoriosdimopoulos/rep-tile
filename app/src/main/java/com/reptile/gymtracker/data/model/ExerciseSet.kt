package com.reptile.gymtracker.data.model

data class ExerciseSet(
    val id: Long = 0,
    val sessionId: Long = 0,
    val exerciseType: ExerciseType = ExerciseType.UNKNOWN,
    val setNumber: Int = 1,
    val repCount: Int = 0,
    val durationSeconds: Long = 0L,
    val caloriesForSet: Float = 0f,
    val avgConfidence: Float = 0f
)
