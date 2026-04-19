package com.reptile.gymtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.reptile.gymtracker.data.model.ActivityLevel
import com.reptile.gymtracker.data.model.Gender
import com.reptile.gymtracker.data.model.UserProfile

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val ageYears: Int,
    val weightKg: Float,
    val heightCm: Float,
    val gender: String,
    val activityLevel: String
)

fun UserProfileEntity.toDomain() = UserProfile(
    name = name,
    ageYears = ageYears,
    weightKg = weightKg,
    heightCm = heightCm,
    gender = Gender.fromName(gender),
    activityLevel = ActivityLevel.fromName(activityLevel)
)

fun UserProfile.toEntity() = UserProfileEntity(
    id = 1,
    name = name,
    ageYears = ageYears,
    weightKg = weightKg,
    heightCm = heightCm,
    gender = gender.name,
    activityLevel = activityLevel.name
)
