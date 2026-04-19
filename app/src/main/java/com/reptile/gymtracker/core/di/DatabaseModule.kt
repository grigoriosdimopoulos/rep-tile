package com.reptile.gymtracker.core.di

import android.content.Context
import androidx.room.Room
import com.reptile.gymtracker.data.local.GymTrackerDatabase
import com.reptile.gymtracker.data.local.dao.ExerciseSetDao
import com.reptile.gymtracker.data.local.dao.SessionDao
import com.reptile.gymtracker.data.local.dao.UserProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GymTrackerDatabase =
        Room.databaseBuilder(
            context,
            GymTrackerDatabase::class.java,
            "gymtracker.db"
        ).fallbackToDestructiveMigration().build()

    @Provides
    fun provideSessionDao(db: GymTrackerDatabase): SessionDao = db.sessionDao()

    @Provides
    fun provideExerciseSetDao(db: GymTrackerDatabase): ExerciseSetDao = db.exerciseSetDao()

    @Provides
    fun provideUserProfileDao(db: GymTrackerDatabase): UserProfileDao = db.userProfileDao()
}
