package com.example.wheresxyz.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.wheresxyz.ui.theme.WheresXYZTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_displaysCoreFieldsAndActions() {
        composeTestRule.setContent {
            WheresXYZTheme {
                LoginScreen(
                    onLoginClick = { _, _ -> },
                    onGoogleSignInClick = {},
                    onNavigateToRegister = {},
                    errorMessage = null,
                    onClearError = {},
                    isLoading = false
                )
            }
        }

        composeTestRule.onNodeWithText("Witaj ponownie").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("E-mail").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Hasło").assertIsDisplayed()
        composeTestRule.onNodeWithText("Zaloguj się").assertIsDisplayed()
        composeTestRule.onNodeWithText("Zarejestruj się").assertIsDisplayed()
    }
}
