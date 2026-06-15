package com.example.wheresxyz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.wheresxyz.data.model.GroupItem
import com.example.wheresxyz.data.model.User
import com.example.wheresxyz.ui.theme.WheresXYZTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GroupsTabTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val currentUser = User(
        id = "uid_1",
        userCode = 1234,
        name = "Jan",
        lastname = "Kowalski",
        email = "jan@example.com"
    )

    @Test
    fun groupsTab_displaysHeaderAndActions() {
        composeTestRule.setContent {
            WheresXYZTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0B0F19))
                ) {
                    GroupsTab(
                        currentUser = currentUser,
                        groupsList = emptyList(),
                        isLoading = false,
                        error = null,
                        onCreateGroup = {},
                        onJoinGroup = {},
                        onUpdateGroupName = { _, _ -> },
                        onUpdateMemberPermissions = { _, _, _, _, _ -> },
                        onRemoveMember = { _, _ -> },
                        onClearError = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Moje Grupy").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nowa Grupa").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dołącz do grupy przez kod").assertIsDisplayed()
    }

    @Test
    fun groupsTab_displaysGroupNameFromList() {
        val groups = listOf(
            GroupItem(id = "g1", name = "Znajomi z pracy", code = "1234")
        )

        composeTestRule.setContent {
            WheresXYZTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0B0F19))
                ) {
                    GroupsTab(
                        currentUser = currentUser,
                        groupsList = groups,
                        isLoading = false,
                        error = null,
                        onCreateGroup = {},
                        onJoinGroup = {},
                        onUpdateGroupName = { _, _ -> },
                        onUpdateMemberPermissions = { _, _, _, _, _ -> },
                        onRemoveMember = { _, _ -> },
                        onClearError = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Znajomi z pracy").assertIsDisplayed()
    }
}
