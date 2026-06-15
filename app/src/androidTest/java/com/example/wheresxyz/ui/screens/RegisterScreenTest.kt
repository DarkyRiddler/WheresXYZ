package com.example.wheresxyz.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.wheresxyz.ui.theme.WheresXYZTheme
import org.junit.Assert.assertEquals
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

    @Test
    fun registerScreen_fillsFieldsAndSubmitsRegistration() {
        var submittedName: String? = null
        var submittedLastname: String? = null
        var submittedEmail: String? = null
        var submittedPassword: String? = null

        composeTestRule.setContent {
            WheresXYZTheme {
                RegisterScreen(
                    onRegisterClick = { name, lastname, email, password ->
                        submittedName = name
                        submittedLastname = lastname
                        submittedEmail = email
                        submittedPassword = password
                    },
                    onNavigateToLogin = {},
                    errorMessage = null,
                    onClearError = {},
                    isLoading = false
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Imię").performClick()
        composeTestRule.onNodeWithContentDescription("Imię").performTextInput("Anna")
        composeTestRule.onNodeWithContentDescription("Nazwisko").performClick()
        composeTestRule.onNodeWithContentDescription("Nazwisko").performTextInput("Nowak")
        composeTestRule.onNodeWithContentDescription("E-mail").performClick()
        composeTestRule.onNodeWithContentDescription("E-mail").performTextInput("anna@example.com")
        composeTestRule.onNodeWithContentDescription("Hasło").performClick()
        composeTestRule.onNodeWithContentDescription("Hasło").performTextInput("haslo123")
        composeTestRule.onNodeWithText("Zarejestruj się").performClick()

        assertEquals("Anna", submittedName)
        assertEquals("Nowak", submittedLastname)
        assertEquals("anna@example.com", submittedEmail)
        assertEquals("haslo123", submittedPassword)
    }

    @Test
    fun registerScreen_navigateToLogin_triggersCallback() {
        var navigated = false

        composeTestRule.setContent {
            WheresXYZTheme {
                RegisterScreen(
                    onRegisterClick = { _, _, _, _ -> },
                    onNavigateToLogin = { navigated = true },
                    errorMessage = null,
                    onClearError = {},
                    isLoading = false
                )
            }
        }

        composeTestRule.onNodeWithText("Zaloguj się").performClick()
        assert(navigated)
    }
}
