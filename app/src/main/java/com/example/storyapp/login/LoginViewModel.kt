package com.example.storyapp.login

import Repository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyapp.User.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: Repository) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> get() = _loginState

    suspend fun userLogin(email: String, password: String): LoginResponse {
        return repository.authenticateUser(email, password)
            .onStart {
                _loginState.value = LoginState.Loading
            }
            .catch { e ->
                _loginState.value = LoginState.Error(e.message ?: "An error occurred")
            }
            .first()
    }

    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            repository.storeUserSession(user)
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val response: LoginResponse) : LoginState()
    data class Error(val message: String) : LoginState()
}