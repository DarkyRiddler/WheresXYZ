package com.example.wheresxyz.ui.navigation

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

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

    // Build Google Sign-In client ONCE at NavGraph level
    val googleSignInOptions = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("595256018529-h3rvguoqb6125g1cmnan27b56297bmfr.apps.googleusercontent.com")
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember(context) {
        GoogleSignIn.getClient(context, googleSignInOptions)
    }

    // Launcher is at TOP LEVEL — properly registered with the Activity's result registry
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    .getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    viewModel.loginWithGoogle(idToken)
                } else {
                    viewModel.onBiometricFailed("Nie udało się pobrać tokenu Google.")
                }
            } catch (e: ApiException) {
                viewModel.onBiometricFailed("Błąd Google Sign-In: kod ${e.statusCode}")
            }
        }
        // If resultCode != RESULT_OK the user simply cancelled — do nothing
    }

    // Observe state to perform navigation
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
                    popUpTo(Destinations.MAIN) { inclusive = true }
                }
            }
            else -> {}
        }
    }

    // Biometric prompt
    if (activity != null && uiState is AuthUiState.BiometricPromptRequired) {
        val authenticator = remember(context) { BiometricAuthenticator(context) }
        LaunchedEffect(uiState) {
            if (authenticator.isBiometricAvailable()) {
                authenticator.authenticate(
                    activity = activity,
                    onSuccess = { viewModel.onBiometricSuccess() },
                    onError = { _, err -> viewModel.onBiometricFailed(err.toString()) },
                    onFailed = { viewModel.onBiometricFailed("Biometric credentials did not match") }
                )
            } else {
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
                onGoogleSignInClick = {
                    // Force account chooser to show every time
                    googleSignInClient.signOut().addOnCompleteListener {
                        googleSignInLauncher.launch(googleSignInClient.signInIntent)
                    }
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
                    onLogoutClick = { viewModel.logout() },
                    onSaveProfileClick = { name, lastname, photo ->
                        viewModel.updateProfile(name, lastname, photo)
                    }
                )
            }
        }
    }
}
