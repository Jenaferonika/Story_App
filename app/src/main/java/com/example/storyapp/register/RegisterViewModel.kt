package com.example.storyapp.register

import Repository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyapp.User.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class RegisterViewModel(private val repository: Repository) : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> get() = _registerState

    fun userRegister(name: String, email: String, password: String) {
        viewModelScope.launch {
            repository.userRegister(name, email, password)
                .onStart {
                    _registerState.value = RegisterState.Loading
                }
                .catch { e ->
                    _registerState.value = RegisterState.Error(e.message ?: "An error occurred")
                }
                .collect { response ->
                    if (response.error == true) {
                        _registerState.value = RegisterState.Error(response.message ?: "Registrasi gagal")
                    } else {
                        _registerState.value = RegisterState.Success(response)
                    }
                }
        }
    }

    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            repository.storeUserSession(user)
        }
    }
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val response: RegisterResponse) : RegisterState()
    data class Error(val message: String) : RegisterState()
}
