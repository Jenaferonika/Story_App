package com.example.storyapp.User

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreference private constructor(private val dataStore: DataStore<Preferences>) {

    suspend fun setUserSession(user: UserModel) {
        dataStore.edit { prefs ->
            prefs[USER_EMAIL] = user.email
            prefs[USER_TOKEN] = user.token
            prefs[USER_LOGGED_IN] = true
        }
    }

    fun getSession(): Flow<UserModel> {
        return dataStore.data.map { prefs ->
            UserModel(
                prefs[USER_EMAIL] ?: "",
                prefs[USER_TOKEN] ?: "",
                prefs[USER_LOGGED_IN] ?: false
            )
        }
    }

    suspend fun logout() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    companion object {
        @Volatile
        private var instance: UserPreference? = null

        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_TOKEN = stringPreferencesKey("user_token")
        private val USER_LOGGED_IN = booleanPreferencesKey("user_logged_in")

        fun getInstance(dataStore: DataStore<Preferences>): UserPreference {
            return instance ?: synchronized(this) {
                val newInstance = UserPreference(dataStore)
                instance = newInstance
                newInstance
            }
        }
    }
}