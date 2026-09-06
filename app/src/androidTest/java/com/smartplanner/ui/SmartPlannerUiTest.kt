package com.smartplanner.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.smartplanner.controller.AuthUiState
import com.smartplanner.controller.CheckInUiState
import com.smartplanner.controller.DashboardUiState
import com.smartplanner.controller.OnboardingUiState
import com.smartplanner.controller.ProgressUiState
import com.smartplanner.model.CheckInStatus
import com.smartplanner.model.Habit
import com.smartplanner.model.HabitPlan
import com.smartplanner.model.User
import com.smartplanner.view.CheckInHabitCard
import com.smartplanner.view.HabitCard
import com.smartplanner.view.PlanProfileCard
import com.smartplanner.view.SmartPlannerLogo
import com.smartplanner.view.SplashScreen
import com.smartplanner.view.StepIndicator
import com.smartplanner.view.Step1Content
import com.smartplanner.view.Step2Content
import com.smartplanner.view.Step3Content
import com.smartplanner.view.StatItem
import org.junit.Rule
import org.junit.Test

class SmartPlannerUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLogoDisplaysCorrectly() {
        composeTestRule.setContent {
            SmartPlannerLogo()
        }
        composeTestRule.onNodeWithText("Smart Planner", substring = true).assertIsDisplayed()
    }

    @Test
    fun testSplashScreenDisplaysLogoAndTriggersTimeout() {
        var timeoutCalled = false
        composeTestRule.setContent {
            SplashScreen(onTimeout = { timeoutCalled = true })
        }
        
        // Verify that the centered logo is displayed
        composeTestRule.onNodeWithText("Smart Planner", substring = true).assertIsDisplayed()
        
        // Advance clock and verify that timeout is triggered
        composeTestRule.mainClock.advanceTimeBy(2000)
        assert(timeoutCalled)
    }

    @Test
    fun testDashboardHabitCardDisplaysCorrectly() {
        val mockHabit = Habit(
            id = "test_h",
            title = "Practice Coding",
            trigger = "After lunch",
            minVersion = "5 minutes",
            aiReasoning = "Build neural pathways"
        )

        composeTestRule.setContent {
            HabitCard(habit = mockHabit)
        }

        composeTestRule.onNodeWithText("Practice Coding").assertIsDisplayed()
        composeTestRule.onNodeWithText("After lunch").assertIsDisplayed()
        composeTestRule.onNodeWithText("5 minutes").assertIsDisplayed()
        composeTestRule.onNodeWithText("AI REASONING: Build neural pathways", substring = true).assertIsDisplayed()
    }

    @Test
    fun testDashboardPlanProfileCardDisplaysCorrectly() {
        val mockPlan = HabitPlan(
            id = "test_p",
            goals = listOf("Be a great developer"),
            knownObstacles = "Lack of focus",
            dailyCommitmentMinutes = 60,
            createdAt = "2026-08-31",
            habits = emptyList()
        )

        composeTestRule.setContent {
            PlanProfileCard(plan = mockPlan)
        }

        composeTestRule.onNodeWithText("Plan Profile").assertIsDisplayed()
        composeTestRule.onNodeWithText("• Be a great developer").assertIsDisplayed()
        composeTestRule.onNodeWithText("Lack of focus").assertIsDisplayed()
        composeTestRule.onNodeWithText("60 minutes per day").assertIsDisplayed()
    }

    @Test
    fun testCheckInHabitCardInteractions() {
        val mockHabit = Habit(
            id = "test_h",
            title = "Drink Water",
            trigger = "Right after waking up",
            minVersion = "1 glass",
            aiReasoning = "Hydration booster"
        )
        
        var selectedStatus: CheckInStatus? = null
        var noteChangedText = ""

        composeTestRule.setContent {
            CheckInHabitCard(
                habit = mockHabit,
                selectedStatus = selectedStatus,
                noteText = noteChangedText,
                onStatusSelected = { selectedStatus = it },
                onNoteChanged = { noteChangedText = it }
            )
        }

        composeTestRule.onNodeWithText("Drink Water").assertIsDisplayed()
        composeTestRule.onNodeWithText("Min version: 1 glass").assertIsDisplayed()

        // Tap Done button
        composeTestRule.onNodeWithText("Done").performClick()
        assert(selectedStatus == CheckInStatus.DONE)

        // Type in Note
        composeTestRule.onNodeWithText("Add optional note...").performTextInput("Feeling hydrated")
        assert(noteChangedText == "Feeling hydrated")
    }

    @Test
    fun testOnboardingStepIndicator() {
        composeTestRule.setContent {
            StepIndicator(currentStep = 2)
        }
        // Verify steps numbers exist
        composeTestRule.onNodeWithText("1").assertIsDisplayed()
        composeTestRule.onNodeWithText("2").assertIsDisplayed()
        composeTestRule.onNodeWithText("3").assertIsDisplayed()
    }

    @Test
    fun testOnboardingStep1Input() {
        val goals = listOf("Learn Compose", "")
        var indexChanged = -1
        var valueChanged = ""
        var addGoalCalled = false

        composeTestRule.setContent {
            Step1Content(
                goals = goals,
                onGoalChanged = { idx, v -> 
                    indexChanged = idx
                    valueChanged = v 
                },
                onAddGoal = { addGoalCalled = true },
                onRemoveGoal = {}
            )
        }

        composeTestRule.onNodeWithText("Step 1: What do you want to achieve?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Learn Compose").assertIsDisplayed()
        
        // Tap add goal
        composeTestRule.onNodeWithText("+ Add another goal").performClick()
        assert(addGoalCalled)
    }

    @Test
    fun testStatItemsOnProgressScreen() {
        composeTestRule.setContent {
            StatItem(label = "Consistency Rate", value = "92.5%")
        }
        composeTestRule.onNodeWithText("Consistency Rate").assertIsDisplayed()
        composeTestRule.onNodeWithText("92.5%").assertIsDisplayed()
    }
}
