package com.example.wheresxyz.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wheresxyz.ui.biometric.BiometricAuthenticator
import com.example.wheresxyz.ui.screens.*
import com.example.wheresxyz.ui.viewmodel.AuthUiState
import com.example.wheresxyz.ui.viewmodel.AuthViewModel

object Destinations {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN = "main"
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    viewModel: AuthViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    // Observe state to perform redirection/navigation changes
    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.LoggedIn -> {
                navController.navigate(Destinations.MAIN) {
                    popUpTo(Destinations.SPLASH) { inclusive = true }
                    popUpTo(Destinations.LOGIN) { inclusive = true }
                }
            }
            is AuthUiState.LoggedOut -> {
                navController.navigate(Destinations.LOGIN) {
                    popUpTo(Destinations.SPLASH) { inclusive = true }
                    // Prevent backing back into Main
                    popUpTo(Destinations.MAIN) { inclusive = true }
                }
            }
            else -> {}
        }
    }

    // Launch Biometric authentication flow when requested
    if (activity != null && uiState is AuthUiState.BiometricPromptRequired) {
        val authenticator = remember(context) { BiometricAuthenticator(context) }
        LaunchedEffect(uiState) {
            if (authenticator.isBiometricAvailable()) {
                authenticator.authenticate(
                    activity = activity,
                    onSuccess = {
                        viewModel.onBiometricSuccess()
                    },
                    onError = { _, err ->
                        viewModel.onBiometricFailed(err.toString())
                    },
                    onFailed = {
                        viewModel.onBiometricFailed("Biometric credentials did not match")
                    }
                )
            } else {
                // Fallback: If biometrics are not set up or configured, go straight to Main View
                // because the OAuth token is valid.
                viewModel.onBiometricSuccess()
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Destinations.SPLASH
    ) {
        composable(Destinations.SPLASH) {
            SplashScreen(
                onCheckAuth = {
                    val authenticator = BiometricAuthenticator(context)
                    viewModel.checkInitialAuthState(authenticator.isBiometricAvailable())
                }
            )
        }

        composable(Destinations.LOGIN) {
            val error = (uiState as? AuthUiState.Error)?.message
            val isLoading = uiState is AuthUiState.Loading

            LoginScreen(
                onLoginClick = { email, password ->
                    viewModel.login(email, password)
                },
                onOAuthClick = { provider ->
                    // Simulate standard OAuth callback flow with a mocked OAuth Token
                    viewModel.loginWithOAuth(provider, "secure_oauth_token_from_${provider.lowercase()}_provider")
                },
                onNavigateToRegister = {
                    navController.navigate(Destinations.REGISTER)
                },
                errorMessage = error,
                onClearError = { viewModel.clearError() },
                isLoading = isLoading
            )
        }

        composable(Destinations.REGISTER) {
            val error = (uiState as? AuthUiState.Error)?.message
            val isLoading = uiState is AuthUiState.Loading

            RegisterScreen(
                onRegisterClick = { name, lastname, email, password ->
                    viewModel.register(name, lastname, email, password)
                },
                onNavigateToLogin = {
                    navController.navigate(Destinations.LOGIN) {
                        popUpTo(Destinations.REGISTER) { inclusive = true }
                    }
                },
                errorMessage = error,
                onClearError = { viewModel.clearError() },
                isLoading = isLoading
            )
        }

        composable(Destinations.MAIN) {
            val currentUser = (uiState as? AuthUiState.LoggedIn)?.user
            if (currentUser != null) {
                MainScreen(
                    user = currentUser,
                    onLogoutClick = {
                        viewModel.logout()
                    },
                    onSaveProfileClick = { name, lastname, photo ->
                        viewModel.updateProfile(name, lastname, photo)
                    }
                )
            }
        }
    }
}
