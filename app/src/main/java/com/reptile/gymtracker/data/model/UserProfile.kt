package com.reptile.gymtracker.data.model

data class UserProfile(
    val name: String = "",
    val ageYears: Int = 25,
    val weightKg: Float = 70f,
    val heightCm: Float = 170f,
    val gender: Gender = Gender.OTHER,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE
)

enum class Gender(val displayName: String) {
    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other");

    companion object {
        fun fromName(name: String): Gender =
            entries.firstOrNull { it.name == name } ?: OTHER
    }
}

enum class ActivityLevel(val displayName: String) {
    SEDENTARY("Sedentary"),
    LIGHT("Light"),
    MODERATE("Moderate"),
    ACTIVE("Active"),
    VERY_ACTIVE("Very Active");

    companion object {
        fun fromName(name: String): ActivityLevel =
            entries.firstOrNull { it.name == name } ?: MODERATE
    }
}
