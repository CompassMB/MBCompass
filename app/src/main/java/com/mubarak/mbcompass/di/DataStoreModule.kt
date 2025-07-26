// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.di

import android.content.Context
import com.mubarak.mbcompass.data.local.PreferenceDataSource
import com.mubarak.mbcompass.data.local.PreferenceLocalDataStore
import com.mubarak.mbcompass.data.local.UserPreferenceRepository
import com.mubarak.mbcompass.data.local.UserPreferencesRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideLocalDataStore(@ApplicationContext context: Context): PreferenceDataSource {
        return PreferenceLocalDataStore(context)
    }

    @Provides
    @Singleton
    fun provideUserPreferenceRepository(dataStore: PreferenceDataSource): UserPreferenceRepository {
        return UserPreferencesRepositoryImpl(dataStore)
    }
}