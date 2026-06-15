package com.example.wheresxyz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.wheresxyz.data.model.GroupItem
import com.example.wheresxyz.data.model.GroupMember
import com.example.wheresxyz.data.model.User
import com.example.wheresxyz.ui.theme.WheresXYZTheme
import org.junit.Assert.assertEquals
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

    @Test
    fun groupsTab_createGroupDialog_submitsGroupName() {
        var createdName: String? = null

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
                        onCreateGroup = { createdName = it },
                        onJoinGroup = {},
                        onUpdateGroupName = { _, _ -> },
                        onUpdateMemberPermissions = { _, _, _, _, _ -> },
                        onRemoveMember = { _, _ -> },
                        onClearError = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Nowa Grupa").performClick()
        composeTestRule.onNodeWithText("Utwórz nową grupę").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nazwa grupy").performClick()
        composeTestRule.onNodeWithText("Nazwa grupy").performTextInput("Wycieczka górska")
        composeTestRule.onNodeWithText("Utwórz").performClick()

        assertEquals("Wycieczka górska", createdName)
    }

    @Test
    fun groupsTab_joinGroupDialog_submitsFourDigitCode() {
        var joinedCode: String? = null

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
                        onJoinGroup = { joinedCode = it },
                        onUpdateGroupName = { _, _ -> },
                        onUpdateMemberPermissions = { _, _, _, _, _ -> },
                        onRemoveMember = { _, _ -> },
                        onClearError = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Dołącz do grupy przez kod").performClick()
        composeTestRule.onNodeWithText("Dołącz do grupy").assertIsDisplayed()
        composeTestRule.onNodeWithText("Kod grupy").performClick()
        composeTestRule.onNodeWithText("Kod grupy").performTextInput("5678")
        composeTestRule.onNodeWithText("Dołącz").performClick()

        assertEquals("5678", joinedCode)
    }

    @Test
    fun groupsTab_clickGroup_opensDetailsWithMembers() {
        val groups = listOf(
            GroupItem(
                id = "g1",
                name = "Znajomi z pracy",
                code = "1234",
                isAdmin = true,
                members = listOf(
                    GroupMember(
                        name = "Jan",
                        lastname = "Kowalski",
                        email = "jan@example.com",
                        isMe = true
                    )
                )
            )
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

        composeTestRule.onNodeWithText("Znajomi z pracy").performClick()
        composeTestRule.onNodeWithText("KOD GRUPY").assertIsDisplayed()
        composeTestRule.onNodeWithText("#1234").assertIsDisplayed()
        composeTestRule.onNodeWithText("Członkowie (1)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jan Kowalski").assertIsDisplayed()
    }
}
