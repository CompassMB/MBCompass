// SPDX-License-Identifier: GPL-3.0-or-later

/*
*
* Copyright (c) 2024 Mubarak Basha. All Rights Reserved.
This project is licensed under GPL-3.0. Any derivative work must keep the same license,
retain this copyright notice, and provide proper attribution.
*
* */

package com.mubarak.mbcompass.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.mubarak.mbcompass.data.preferences.PreferenceDataSource
import com.mubarak.mbcompass.data.preferences.PreferenceLocalDataStore
import com.mubarak.mbcompass.data.preferences.UserPreferenceRepository
import com.mubarak.mbcompass.data.preferences.UserPreferencesRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
private const val PREF_NAME = "settings"

val Context.preferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = PREF_NAME)

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return context.preferencesDataStore
    }

    @Provides
    @Singleton
    fun provideLocalDataStore(
        dataStore: DataStore<Preferences>
    ): PreferenceDataSource {
        return PreferenceLocalDataStore(dataStore)
    }

    @Provides
    @Singleton
    fun provideUserPreferenceRepository(
        dataStore: PreferenceDataSource
    ): UserPreferenceRepository {
        return UserPreferencesRepositoryImpl(dataStore)
    }
}