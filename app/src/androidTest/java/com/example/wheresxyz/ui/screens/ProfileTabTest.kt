package com.example.wheresxyz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.wheresxyz.data.model.User
import com.example.wheresxyz.ui.theme.WheresXYZTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileTabTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val user = User(
        id = "uid_1",
        userCode = 8051,
        name = "Jan",
        lastname = "Kowalski",
        email = "jan@example.com"
    )

    @Test
    fun profileTab_displaysUserInfoAndMemberCode() {
        composeTestRule.setContent {
            WheresXYZTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0B0F19))
                ) {
                    ProfileTab(
                        user = user,
                        onLogoutClick = {},
                        onSaveProfileClick = { _, _, _ -> }
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Jan Kowalski").assertIsDisplayed()
        composeTestRule.onNodeWithText("jan@example.com").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mój Kod Członka").assertIsDisplayed()
        composeTestRule.onNodeWithText("8051").assertIsDisplayed()
        composeTestRule.onNodeWithText("Edytuj profil").assertIsDisplayed()
        composeTestRule.onNodeWithText("Wyloguj się").assertIsDisplayed()
    }

    @Test
    fun profileTab_editMode_showsSaveAndCancelButtons() {
        composeTestRule.setContent {
            WheresXYZTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0B0F19))
                ) {
                    ProfileTab(
                        user = user,
                        onLogoutClick = {},
                        onSaveProfileClick = { _, _, _ -> }
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Edytuj profil").performClick()
        composeTestRule.onNodeWithText("Zapisz").assertIsDisplayed()
        composeTestRule.onNodeWithText("Anuluj").assertIsDisplayed()
        composeTestRule.onNodeWithText("Wybierz awatar:").assertIsDisplayed()
    }

    @Test
    fun profileTab_logoutButton_triggersCallback() {
        var logoutClicked = false

        composeTestRule.setContent {
            WheresXYZTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0B0F19))
                ) {
                    ProfileTab(
                        user = user,
                        onLogoutClick = { logoutClicked = true },
                        onSaveProfileClick = { _, _, _ -> }
                    )
                }
            }
        }

        composeTestRule.onNodeWithContentDescription("Wyloguj się").performClick()
        assert(logoutClicked)
    }
}
