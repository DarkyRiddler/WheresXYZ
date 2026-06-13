package com.example.wheresxyz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wheresxyz.data.local.TokenManager
import com.example.wheresxyz.data.model.User
import com.example.wheresxyz.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthUiState {
    object Idle : AuthUiState
    object SplashChecking : AuthUiState
    object BiometricPromptRequired : AuthUiState
    object LoggedOut : AuthUiState
    object Loading : AuthUiState
    data class LoggedIn(val user: User) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun checkInitialAuthState(isBiometricAvailable: Boolean) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.SplashChecking
            
            // Artificial delay for splash screen animation to finish
            kotlinx.coroutines.delay(2000)

            if (tokenManager.isTokenValid()) {
                if (isBiometricAvailable) {
                    _uiState.value = AuthUiState.BiometricPromptRequired
                } else {
                    // If biometrics are not available or set up, but we have a valid token,
                    // we log them straight in as per requirements.
                    fetchCurrentUser()
                }
            } else {
                _uiState.value = AuthUiState.LoggedOut
            }
        }
    }

    fun onBiometricSuccess() {
        fetchCurrentUser()
    }

    fun onBiometricFailed(errorMsg: String?) {
        // Biometrics failed or user cancelled, let them log in manually
        _uiState.value = AuthUiState.LoggedOut
    }

    private fun fetchCurrentUser() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.getCurrentUser()
                .onSuccess { user ->
                    _uiState.value = AuthUiState.LoggedIn(user)
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Failed to get current user info")
                }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.login(email, password)
                .onSuccess { authResponse ->
                    _uiState.value = AuthUiState.LoggedIn(authResponse.user)
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Login failed")
                }
        }
    }

    fun register(name: String, lastname: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.register(name, lastname, email, password)
                .onSuccess { authResponse ->
                    _uiState.value = AuthUiState.LoggedIn(authResponse.user)
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Registration failed")
                }
        }
    }

    fun loginWithOAuth(provider: String, oAuthToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.loginWithOAuth(provider, oAuthToken)
                .onSuccess { authResponse ->
                    _uiState.value = AuthUiState.LoggedIn(authResponse.user)
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "OAuth login failed")
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState.LoggedOut
        }
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.LoggedOut
        }
    }
}
