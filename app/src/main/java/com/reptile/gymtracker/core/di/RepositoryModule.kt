package com.reptile.gymtracker.core.di

import com.reptile.gymtracker.data.repository.SessionRepository
import com.reptile.gymtracker.data.repository.SessionRepositoryImpl
import com.reptile.gymtracker.data.repository.UserProfileRepository
import com.reptile.gymtracker.data.repository.UserProfileRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(impl: UserProfileRepositoryImpl): UserProfileRepository
}
