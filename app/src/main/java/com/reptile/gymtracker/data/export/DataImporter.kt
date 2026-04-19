package com.reptile.gymtracker.data.export

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.reptile.gymtracker.data.model.ActivityLevel
import com.reptile.gymtracker.data.model.ExerciseSet
import com.reptile.gymtracker.data.model.ExerciseType
import com.reptile.gymtracker.data.model.Gender
import com.reptile.gymtracker.data.model.Session
import com.reptile.gymtracker.data.model.SessionWithSets
import com.reptile.gymtracker.data.model.UserProfile
import com.reptile.gymtracker.data.repository.SessionRepository
import com.reptile.gymtracker.data.repository.UserProfileRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class ImportStrategy { MERGE, REPLACE }

@Singleton
class DataImporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson,
    private val sessionRepository: SessionRepository,
    private val userProfileRepository: UserProfileRepository
) {
    suspend fun import(uri: Uri, strategy: ImportStrategy): ImportResult {
        return try {
            val json = context.contentResolver.openInputStream(uri)
                ?.bufferedReader()
                ?.readText()
                ?: return ImportResult.Failure("Could not read file")

            val envelope = gson.fromJson(json, ExportEnvelope::class.java)
                ?: return ImportResult.Failure("Invalid file format")

            if (envelope.exportVersion > 1) {
                return ImportResult.VersionMismatch
            }

            if (strategy == ImportStrategy.REPLACE) {
                sessionRepository.deleteAllSessions()
            }

            envelope.userProfile?.let { dto ->
                userProfileRepository.saveProfile(
                    UserProfile(
                        name = dto.name,
                        ageYears = dto.ageYears,
                        weightKg = dto.weightKg,
                        heightCm = dto.heightCm,
                        gender = Gender.fromName(dto.gender),
                        activityLevel = ActivityLevel.fromName(dto.activityLevel)
                    )
                )
            }

            var imported = 0
            envelope.sessions.forEach { sessionDto ->
                val session = Session(
                    startTimestamp = sessionDto.startTimestamp,
                    endTimestamp = sessionDto.endTimestamp,
                    durationSeconds = sessionDto.durationSeconds,
                    totalCalories = sessionDto.totalCalories,
                    notes = sessionDto.notes
                )
                val sets = sessionDto.sets.mapIndexed { idx, setDto ->
                    ExerciseSet(
                        exerciseType = ExerciseType.fromName(setDto.exerciseType),
                        setNumber = setDto.setNumber.takeIf { it > 0 } ?: (idx + 1),
                        repCount = setDto.repCount,
                        durationSeconds = setDto.durationSeconds,
                        caloriesForSet = setDto.caloriesForSet,
                        avgConfidence = setDto.avgConfidence
                    )
                }
                sessionRepository.importSessionWithSets(SessionWithSets(session, sets))
                imported++
            }

            ImportResult.Success(imported)
        } catch (e: Exception) {
            ImportResult.Failure(e.message ?: "Unknown error")
        }
    }
}
