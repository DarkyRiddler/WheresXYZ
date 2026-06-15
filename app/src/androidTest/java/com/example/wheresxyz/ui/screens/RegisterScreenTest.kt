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
class RegisterScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun registerScreen_displaysCoreFieldsAndActions() {
        composeTestRule.setContent {
            WheresXYZTheme {
                RegisterScreen(
                    onRegisterClick = { _, _, _, _ -> },
                    onNavigateToLogin = {},
                    errorMessage = null,
                    onClearError = {},
                    isLoading = false
                )
            }
        }

        composeTestRule.onNodeWithText("Załóż konto").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Imię").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Nazwisko").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("E-mail").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Hasło").assertIsDisplayed()
        composeTestRule.onNodeWithText("Zarejestruj się").assertIsDisplayed()
        composeTestRule.onNodeWithText("Zaloguj się").assertIsDisplayed()
    }
}
