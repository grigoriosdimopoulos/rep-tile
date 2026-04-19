package com.reptile.gymtracker.data.export

data class ExportEnvelope(
    val exportVersion: Int = 1,
    val exportedAt: Long,
    val userProfile: UserProfileDto?,
    val sessions: List<SessionExportDto>
)

data class UserProfileDto(
    val name: String,
    val ageYears: Int,
    val weightKg: Float,
    val heightCm: Float,
    val gender: String,
    val activityLevel: String
)

data class SessionExportDto(
    val startTimestamp: Long,
    val endTimestamp: Long?,
    val durationSeconds: Long,
    val totalCalories: Float,
    val notes: String,
    val sets: List<ExerciseSetDto>
)

data class ExerciseSetDto(
    val exerciseType: String,
    val setNumber: Int,
    val repCount: Int,
    val durationSeconds: Long,
    val caloriesForSet: Float,
    val avgConfidence: Float
)

sealed class ImportResult {
    data class Success(val sessionsImported: Int) : ImportResult()
    data class Failure(val reason: String) : ImportResult()
    object VersionMismatch : ImportResult()
}
