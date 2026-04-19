package com.reptile.gymtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.reptile.gymtracker.data.model.Session

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTimestamp: Long,
    val endTimestamp: Long?,
    val durationSeconds: Long,
    val totalCalories: Float,
    val notes: String = ""
)

fun SessionEntity.toDomain() = Session(
    id = id,
    startTimestamp = startTimestamp,
    endTimestamp = endTimestamp,
    durationSeconds = durationSeconds,
    totalCalories = totalCalories,
    notes = notes
)

fun Session.toEntity() = SessionEntity(
    id = id,
    startTimestamp = startTimestamp,
    endTimestamp = endTimestamp,
    durationSeconds = durationSeconds,
    totalCalories = totalCalories,
    notes = notes
)
