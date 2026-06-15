package com.example.wheresxyz.ui.viewmodel

import com.example.wheresxyz.data.local.TokenManager
import com.example.wheresxyz.data.model.AuthResponse
import com.example.wheresxyz.data.model.User
import com.example.wheresxyz.data.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val tokenManager = mockk<TokenManager>(relaxed = true)

    private lateinit var viewModel: AuthViewModel

    private val sampleUser = User(
        id = "uid_1",
        userCode = 1234,
        name = "Jan",
        lastname = "Kowalski",
        email = "jan@example.com"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AuthViewModel(authRepository, tokenManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun checkInitialAuthState_withValidTokenAndBiometrics_requiresBiometricPrompt() = runTest {
        every { tokenManager.isTokenValid() } returns true

        viewModel.checkInitialAuthState(isBiometricAvailable = true)
        advanceTimeBy(2_001)
        advanceUntilIdle()

        assertEquals(AuthUiState.BiometricPromptRequired, viewModel.uiState.value)
    }

    @Test
    fun checkInitialAuthState_withValidTokenWithoutBiometrics_logsIn() = runTest {
        every { tokenManager.isTokenValid() } returns true
        coEvery { authRepository.getCurrentUser() } returns Result.success(sampleUser)

        viewModel.checkInitialAuthState(isBiometricAvailable = false)
        advanceTimeBy(2_001)
        advanceUntilIdle()

        assertEquals(AuthUiState.LoggedIn(sampleUser), viewModel.uiState.value)
    }

    @Test
    fun checkInitialAuthState_withoutValidToken_logsOut() = runTest {
        every { tokenManager.isTokenValid() } returns false

        viewModel.checkInitialAuthState(isBiometricAvailable = true)
        advanceTimeBy(2_001)
        advanceUntilIdle()

        assertEquals(AuthUiState.LoggedOut, viewModel.uiState.value)
    }

    @Test
    fun login_failure_localizesInvalidCredentialsMessage() = runTest {
        coEvery { authRepository.login(any(), any()) } returns Result.failure(
            Exception("INVALID_LOGIN_CREDENTIALS")
        )

        viewModel.login("jan@example.com", "wrong-password")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Error)
        assertEquals("Niepoprawny e-mail lub hasło.", (state as AuthUiState.Error).message)
    }

    @Test
    fun login_success_setsLoggedInState() = runTest {
        val authResponse = AuthResponse("token", "refresh", 3600L, sampleUser)
        coEvery { authRepository.login(any(), any()) } returns Result.success(authResponse)

        viewModel.login("jan@example.com", "password123")
        advanceUntilIdle()

        assertEquals(AuthUiState.LoggedIn(sampleUser), viewModel.uiState.value)
    }

    @Test
    fun logout_clearsSessionAndSetsLoggedOut() = runTest {
        coEvery { authRepository.logout() } returns Unit

        viewModel.logout()
        advanceUntilIdle()

        coVerify { authRepository.logout() }
        assertEquals(AuthUiState.LoggedOut, viewModel.uiState.value)
    }

    @Test
    fun register_success_setsLoggedInState() = runTest {
        val authResponse = AuthResponse("token", "refresh", 3600L, sampleUser)
        coEvery { authRepository.register(any(), any(), any(), any()) } returns Result.success(authResponse)

        viewModel.register("Jan", "Kowalski", "jan@example.com", "password123")
        advanceUntilIdle()

        assertEquals(AuthUiState.LoggedIn(sampleUser), viewModel.uiState.value)
    }

    @Test
    fun register_failure_localizesEmailAlreadyInUseMessage() = runTest {
        coEvery { authRepository.register(any(), any(), any(), any()) } returns Result.failure(
            Exception("The email address is already in use by another account.")
        )

        viewModel.register("Jan", "Kowalski", "jan@example.com", "password123")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Error)
        assertEquals("Ten adres e-mail jest już zarejestrowany.", (state as AuthUiState.Error).message)
    }

    @Test
    fun register_failure_localizesWeakPasswordMessage() = runTest {
        coEvery { authRepository.register(any(), any(), any(), any()) } returns Result.failure(
            Exception("WEAK_PASSWORD: Password should be at least 6 characters")
        )

        viewModel.register("Jan", "Kowalski", "jan@example.com", "123")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Error)
        assertEquals("Hasło jest za słabe. Musi mieć co najmniej 6 znaków.", (state as AuthUiState.Error).message)
    }

    @Test
    fun updateProfile_success_updatesLoggedInUser() = runTest {
        val updatedUser = sampleUser.copy(name = "Janusz", lastname = "Nowak")
        coEvery { authRepository.updateProfile("Janusz", "Nowak", null) } returns Result.success(updatedUser)

        viewModel.updateProfile("Janusz", "Nowak", null)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.LoggedIn)
        assertEquals("Janusz", (state as AuthUiState.LoggedIn).user.name)
        assertEquals("Nowak", state.user.lastname)
    }

    @Test
    fun updateProfile_failure_setsErrorState() = runTest {
        coEvery { authRepository.updateProfile(any(), any(), any()) } returns Result.failure(
            Exception("Not logged in")
        )

        viewModel.updateProfile("Jan", "Kowalski", null)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Error)
        assertEquals("Not logged in", (state as AuthUiState.Error).message)
    }

    @Test
    fun onBiometricFailed_setsLoggedOutState() = runTest {
        viewModel.onBiometricFailed("User cancelled")
        assertEquals(AuthUiState.LoggedOut, viewModel.uiState.value)
    }

    @Test
    fun clearError_fromErrorState_setsLoggedOut() = runTest {
        coEvery { authRepository.login(any(), any()) } returns Result.failure(Exception("fail"))
        viewModel.login("jan@example.com", "wrong")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is AuthUiState.Error)

        viewModel.clearError()
        assertEquals(AuthUiState.LoggedOut, viewModel.uiState.value)
    }
}
