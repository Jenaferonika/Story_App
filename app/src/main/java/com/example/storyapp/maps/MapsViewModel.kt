package com.example.storyapp.maps

import Repository
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.storyapp.story.StoryResponse

class MapsViewModel(private val repository: Repository) : ViewModel() {
    fun getLocation(): LiveData<StoryResponse> {
        return repository.getLocation().asLiveData()
    }
}
