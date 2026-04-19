package com.reptile.gymtracker.data.model

enum class ExerciseType(
    val displayName: String,
    val metValue: Float
) {
    SQUAT("Squat", 5.0f),
    PUSH_UP("Push-up", 8.0f),
    BICEP_CURL("Bicep Curl", 3.5f),
    SHOULDER_PRESS("Shoulder Press", 4.0f),
    DEADLIFT("Deadlift", 6.0f),
    LUNGE("Lunge", 4.5f),
    UNKNOWN("Unknown", 3.0f);

    companion object {
        fun fromName(name: String): ExerciseType =
            entries.firstOrNull { it.name == name } ?: UNKNOWN
    }
}
