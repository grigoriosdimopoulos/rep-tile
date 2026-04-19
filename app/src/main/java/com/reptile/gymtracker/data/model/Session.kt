package com.reptile.gymtracker.data.model

data class Session(
    val id: Long = 0,
    val startTimestamp: Long = 0L,
    val endTimestamp: Long? = null,
    val durationSeconds: Long = 0L,
    val totalCalories: Float = 0f,
    val notes: String = ""
)

data class SessionWithSets(
    val session: Session,
    val sets: List<ExerciseSet>
) {
    val totalReps: Int get() = sets.sumOf { it.repCount }
    val totalSets: Int get() = sets.size
    val exerciseTypes: List<ExerciseType> get() = sets.map { it.exerciseType }.distinct()
}
