package com.smartplanner.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.smartplanner.model.AnchorConfig
import com.smartplanner.model.AnchorType
import com.smartplanner.model.Habit
import com.smartplanner.view.AnchorBadge
import com.smartplanner.view.AnchorConfigDialog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AnchorPickerUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testAnchorBadgeDisplaysCorrectAnchorType() {
        var clicked = false
        composeTestRule.setContent {
            AnchorBadge(
                anchorType = AnchorType.HEADPHONES_CONNECTED,
                onClick = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Headphones Connected").assertIsDisplayed()
        composeTestRule.onNodeWithText("Headphones Connected").performClick()
        assertTrue("Badge click should trigger callback", clicked)
    }

    @Test
    fun testAnchorConfigDialogDisplaysAllOptionsAndSavesSelection() {
        val testHabit = Habit(
            id = "test_anchor_habit",
            title = "Read Tech Article",
            trigger = "After brewing tea",
            minVersion = "1 paragraph",
            aiReasoning = "Continuous learning",
            anchorType = AnchorType.CLOCK_TIME,
            anchorConfig = AnchorConfig(clockTimeString = "08:30")
        )

        var savedType: AnchorType? = null
        var savedClockTime: String? = null
        var dismissCalled = false

        composeTestRule.setContent {
            AnchorConfigDialog(
                habit = testHabit,
                onDismiss = { dismissCalled = true },
                onSave = { type, clockTime, _, _, _ ->
                    savedType = type
                    savedClockTime = clockTime
                }
            )
        }

        // Verify Title and Subtitle
        composeTestRule.onNodeWithText("Configure Real Anchor").assertIsDisplayed()
        composeTestRule.onNodeWithText("Read Tech Article").assertIsDisplayed()

        // Verify anchor option cards exist
        composeTestRule.onNodeWithText("Phone Charging Started").assertIsDisplayed()
        composeTestRule.onNodeWithText("Headphones Connected").assertIsDisplayed()
        composeTestRule.onNodeWithText("Arrived Home").assertIsDisplayed()
        composeTestRule.onNodeWithText("First Screen Unlock").assertIsDisplayed()
        composeTestRule.onNodeWithText("Clock Time (Fallback)").assertIsDisplayed()

        // Select "Phone Charging Started"
        composeTestRule.onNodeWithText("Phone Charging Started").performClick()

        // Tap Save
        composeTestRule.onNodeWithText("Save Anchor").performClick()

        assertEquals(AnchorType.CHARGING_STARTED, savedType)
    }

    @Test
    fun testArrivedHomeSelectionShowsLocationNotice() {
        val testHabit = Habit(
            id = "test_home_habit",
            title = "Evening Stretch",
            trigger = "When getting home",
            minVersion = "2 stretches",
            aiReasoning = "Decompress after commute",
            anchorType = AnchorType.CLOCK_TIME
        )

        composeTestRule.setContent {
            AnchorConfigDialog(
                habit = testHabit,
                onDismiss = {},
                onSave = { _, _, _, _, _ -> }
            )
        }

        // Select "Arrived Home"
        composeTestRule.onNodeWithText("Arrived Home").performClick()

        // Verify Location Permission Notice is displayed
        composeTestRule.onNodeWithText("📍 Location Permission Notice", substring = true).assertIsDisplayed()
    }
}
