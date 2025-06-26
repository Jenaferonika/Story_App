package com.example.storyapp.addStory

import com.google.gson.annotations.SerializedName

data class addStoryResponse(

    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String,
)