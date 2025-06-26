package com.example.storyapp.User

data class UserModel(
    val email: String,
    val token: String,
    val isLogin: Boolean = false,
)