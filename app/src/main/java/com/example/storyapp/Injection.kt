package com.example.storyapp

import Repository
import android.content.Context
import com.example.storyapp.User.UserPreference
import com.example.storyapp.User.dataStore
import com.example.storyapp.api.ApiConfig
import com.example.storyapp.db.StoryDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object Injection {
    fun provideRepository(context: Context): Repository {
        val pref = UserPreference.getInstance(context.dataStore)
        val user = runBlocking { pref.getSession().first() }
        val apiService = ApiConfig.getApiService(user.token)
        val database = StoryDatabase.getDatabase(context)
        return Repository.getInstance(pref, apiService, database)
    }
}