package com.reptile.gymtracker.data.repository

import com.reptile.gymtracker.data.local.dao.UserProfileDao
import com.reptile.gymtracker.data.local.entity.toDomain
import com.reptile.gymtracker.data.local.entity.toEntity
import com.reptile.gymtracker.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val userProfileDao: UserProfileDao
) : UserProfileRepository {

    override fun getProfileFlow(): Flow<UserProfile?> =
        userProfileDao.getProfileFlow().map { it?.toDomain() }

    override suspend fun getProfile(): UserProfile? =
        userProfileDao.getProfile()?.toDomain()

    override suspend fun saveProfile(profile: UserProfile) =
        userProfileDao.upsertProfile(profile.toEntity())
}
