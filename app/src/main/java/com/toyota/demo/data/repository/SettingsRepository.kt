package com.toyota.demo.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
    private val SHOW_INFERENCE_STATS = booleanPreferencesKey("show_inference_stats")
    private val TEST_FEATURE_1_ENABLED = booleanPreferencesKey("test_feature_1_enabled")

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_DARK_MODE] ?: true
        }

    val showInferenceStats: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[SHOW_INFERENCE_STATS] ?: false
        }

    val isTestFeature1Enabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[TEST_FEATURE_1_ENABLED] ?: false
        }

    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = isDark
        }
    }

    suspend fun setShowInferenceStats(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHOW_INFERENCE_STATS] = show
        }
    }

    suspend fun setTestFeature1Enabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[TEST_FEATURE_1_ENABLED] = enabled
        }
    }
}
