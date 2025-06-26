package com.example.storyapp.addStory

import Repository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyapp.addStory.addStoryHelper.Companion.compressImageToFile
import kotlinx.coroutines.launch
import java.io.File

class addStoryViewModel(private val repository: Repository) : ViewModel() {

    fun addStory(
        file: File,
        description: String,
        lat: Double? = null,
        lon: Double? = null,
        onResult: (addStoryResponse) -> Unit
    ) {
        val compressedFile = file.compressImageToFile()
        viewModelScope.launch {
            onResult(addStoryResponse(error = false, message = "Loading..."))

            repository.addStory(compressedFile, description, lat, lon).collect { result ->
                onResult(result)
            }
        }
    }
}
