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

    private fun getLocalizedErrorMessage(message: String?): String {
        if (message == null) return "Wystąpił nieznany błąd."
        return when {
            message.contains("password is invalid", ignoreCase = true) ||
                    message.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
                    message.contains("wrong password", ignoreCase = true) ->
                "Niepoprawny e-mail lub hasło."
            message.contains("badly formatted", ignoreCase = true) ||
                    message.contains("INVALID_EMAIL", ignoreCase = true) ->
                "Niepoprawny format adresu e-mail."
            message.contains("already in use", ignoreCase = true) ||
                    message.contains("EMAIL_EXISTS", ignoreCase = true) ->
                "Ten adres e-mail jest już zarejestrowany."
            message.contains("weak password", ignoreCase = true) ||
                    message.contains("WEAK_PASSWORD", ignoreCase = true) ->
                "Hasło jest za słabe. Musi mieć co najmniej 6 znaków."
            message.contains("no user record", ignoreCase = true) ||
                    message.contains("USER_NOT_FOUND", ignoreCase = true) ->
                "Konto o podanym adresie e-mail nie istnieje."
            message.contains("network error", ignoreCase = true) ||
                    message.contains("timeout", ignoreCase = true) ->
                "Błąd połączenia z siecią. Spróbuj ponownie później."
            message.contains("too many attempts", ignoreCase = true) ||
                    message.contains("TOO_MANY_ATTEMPTS_TRY_LATER", ignoreCase = true) ->
                "Zbyt wiele nieudanych prób. Spróbuj ponownie później."
            else -> message
        }
    }

    private fun fetchCurrentUser() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.getCurrentUser()
                .onSuccess { user ->
                    _uiState.value = AuthUiState.LoggedIn(user)
                    syncFcmToken()
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(getLocalizedErrorMessage(error.message ?: "Failed to get current user info"))
                }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.login(email, password)
                .onSuccess { authResponse ->
                    _uiState.value = AuthUiState.LoggedIn(authResponse.user)
                    syncFcmToken()
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(getLocalizedErrorMessage(error.message ?: "Login failed"))
                }
        }
    }

    fun register(name: String, lastname: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.register(name, lastname, email, password)
                .onSuccess { authResponse ->
                    _uiState.value = AuthUiState.LoggedIn(authResponse.user)
                    syncFcmToken()
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(getLocalizedErrorMessage(error.message ?: "Registration failed"))
                }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.loginWithGoogle(idToken)
                .onSuccess { authResponse ->
                    _uiState.value = AuthUiState.LoggedIn(authResponse.user)
                    syncFcmToken()
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(getLocalizedErrorMessage(error.message ?: "Google login failed"))
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState.LoggedOut
        }
    }

    fun updateProfile(name: String, lastname: String, userPhoto: String?) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.updateProfile(name, lastname, userPhoto)
                .onSuccess { updatedUser ->
                    _uiState.value = AuthUiState.LoggedIn(updatedUser)
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(getLocalizedErrorMessage(error.message ?: "Failed to update profile"))
                }
        }
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.LoggedOut
        }
    }

    private fun syncFcmToken() {
        try {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    if (token != null) {
                        viewModelScope.launch {
                            authRepository.updateFcmToken(token)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
