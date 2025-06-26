package com.example.storyapp.detail

import Repository
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.first

class DetailViewModel(private val repository: Repository) : ViewModel() {
    suspend fun fetchStoryById(id: String): DetailResponse {
        return repository.getDetailStories(id).first()
    }
}

