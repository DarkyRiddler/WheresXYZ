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
import com.example.wheresxyz.ui.theme.WheresXYZTheme
import com.example.wheresxyz.ui.viewmodel.LocationSyncState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventsTabHeaderSectionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun eventsTabHeaderSection_displaysEmptyState() {
        composeTestRule.setContent {
            WheresXYZTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0B0F19))
                ) {
                    EventsTabHeaderSection(
                        syncState = LocationSyncState.Idle,
                        isEventsLoading = false,
                        isEventsEmpty = true,
                        onStopSharing = {},
                        onAddEventClick = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Wydarzenia").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dodaj Wydarzenie").assertIsDisplayed()
        composeTestRule.onNodeWithText(
            "Brak nadchodzących wydarzeń.\nStwórz nowe, klikając przycisk u góry!",
            substring = true
        ).assertIsDisplayed()
    }

    @Test
    fun eventsTabHeaderSection_showsSharingBannerWhenActive() {
        composeTestRule.setContent {
            WheresXYZTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0B0F19))
                ) {
                    EventsTabHeaderSection(
                        syncState = LocationSyncState.Active,
                        isEventsLoading = false,
                        isEventsEmpty = true,
                        onStopSharing = {},
                        onAddEventClick = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Udostępniasz lokalizację w tle").assertIsDisplayed()
        composeTestRule.onNodeWithText("Zatrzymaj").assertIsDisplayed()
    }
}
