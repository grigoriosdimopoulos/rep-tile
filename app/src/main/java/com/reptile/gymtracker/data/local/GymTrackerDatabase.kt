package com.reptile.gymtracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.reptile.gymtracker.data.local.dao.ExerciseSetDao
import com.reptile.gymtracker.data.local.dao.SessionDao
import com.reptile.gymtracker.data.local.dao.UserProfileDao
import com.reptile.gymtracker.data.local.entity.ExerciseSetEntity
import com.reptile.gymtracker.data.local.entity.SessionEntity
import com.reptile.gymtracker.data.local.entity.UserProfileEntity

@Database(
    entities = [
        SessionEntity::class,
        ExerciseSetEntity::class,
        UserProfileEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class GymTrackerDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun exerciseSetDao(): ExerciseSetDao
    abstract fun userProfileDao(): UserProfileDao
}
