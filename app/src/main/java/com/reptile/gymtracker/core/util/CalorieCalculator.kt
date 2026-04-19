package com.reptile.gymtracker.core.util

import com.reptile.gymtracker.data.model.ExerciseType
import com.reptile.gymtracker.data.model.Gender
import com.reptile.gymtracker.data.model.UserProfile
import kotlin.math.roundToInt

object CalorieCalculator {
    private const val DEFAULT_WEIGHT_KG = 70f

    fun calculate(
        exerciseType: ExerciseType,
        durationSeconds: Long,
        weightKg: Float = DEFAULT_WEIGHT_KG
    ): Float {
        val minutes = durationSeconds / 60f
        return exerciseType.metValue * weightKg * 3.5f / 200f * minutes
    }

    fun estimateBmr(profile: UserProfile): Float {
        return when (profile.gender) {
            Gender.MALE ->
                10 * profile.weightKg + 6.25f * profile.heightCm - 5 * profile.ageYears + 5
            Gender.FEMALE ->
                10 * profile.weightKg + 6.25f * profile.heightCm - 5 * profile.ageYears - 161
            Gender.OTHER ->
                10 * profile.weightKg + 6.25f * profile.heightCm - 5 * profile.ageYears - 78
        }
    }

    fun formatCalories(calories: Float): String =
        "${calories.roundToInt()} cal"
}
