package com.reptile.gymtracker.data.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.reptile.gymtracker.data.model.ActivityLevel
import com.reptile.gymtracker.data.model.Gender
import com.reptile.gymtracker.data.model.SessionWithSets
import com.reptile.gymtracker.data.model.UserProfile
import com.reptile.gymtracker.data.repository.SessionRepository
import com.reptile.gymtracker.data.repository.UserProfileRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataExporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson,
    private val sessionRepository: SessionRepository,
    private val userProfileRepository: UserProfileRepository
) {
    suspend fun export(): Uri? {
        val profile = userProfileRepository.getProfile()
        val sessions = sessionRepository.getAllSessionsWithSets()

        val envelope = ExportEnvelope(
            exportedAt = System.currentTimeMillis(),
            userProfile = profile?.toDto(),
            sessions = sessions.map { it.toDto() }
        )

        val json = gson.toJson(envelope)
        val file = getExportFile()
        file.writeText(json)

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun shareExport(uri: Uri): Intent =
        Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "rep-tile workout data export")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

    private fun getExportFile(): File {
        val dir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.getExternalFilesDir("exports") ?: context.filesDir.resolve("exports")
        } else {
            @Suppress("DEPRECATION")
            Environment.getExternalStoragePublicDirectory("Downloads")
        }
        dir.mkdirs()
        return File(dir, "reptile_export_${System.currentTimeMillis()}.json")
    }

    private fun UserProfile.toDto() = UserProfileDto(
        name = name,
        ageYears = ageYears,
        weightKg = weightKg,
        heightCm = heightCm,
        gender = gender.name,
        activityLevel = activityLevel.name
    )

    private fun SessionWithSets.toDto() = SessionExportDto(
        startTimestamp = session.startTimestamp,
        endTimestamp = session.endTimestamp,
        durationSeconds = session.durationSeconds,
        totalCalories = session.totalCalories,
        notes = session.notes,
        sets = sets.map { set ->
            ExerciseSetDto(
                exerciseType = set.exerciseType.name,
                setNumber = set.setNumber,
                repCount = set.repCount,
                durationSeconds = set.durationSeconds,
                caloriesForSet = set.caloriesForSet,
                avgConfidence = set.avgConfidence
            )
        }
    )
}
