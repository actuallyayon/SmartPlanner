package com.smartplanner.ai

import com.smartplanner.model.CheckIn
import com.smartplanner.model.CheckInStatus
import com.smartplanner.model.Habit
import com.smartplanner.model.ai.AiFeedbackEngine
import org.junit.Assert.assertTrue
import org.junit.Test

class AiFeedbackEngineTest {

    private val feedbackEngine = AiFeedbackEngine()

    private val sampleHabits = listOf(
        Habit(
            id = "h1",
            title = "Morning Meditation",
            trigger = "After coffee",
            minVersion = "1 min",
            aiReasoning = "Calm focus"
        ),
        Habit(
            id = "h2",
            title = "Spanish Practice",
            trigger = "Commute",
            minVersion = "5 mins",
            aiReasoning = "Language skills"
        )
    )

    @Test
    fun `all DONE check-ins produce celebratory feedback`() {
        val checkIns = listOf(
            CheckIn("c1", "h1", "2026-09-21", CheckInStatus.DONE, null),
            CheckIn("c2", "h2", "2026-09-21", CheckInStatus.DONE, null)
        )
        val feedback = feedbackEngine.generateFeedback(sampleHabits, checkIns)

        assertTrue(feedback.title.contains("Consistency") || feedback.title.contains("Incredible"))
        assertTrue(feedback.message.contains("momentum") || feedback.message.contains("seamlessly"))
    }

    @Test
    fun `mix of DONE and PARTIAL praises adaptive resilience`() {
        val checkIns = listOf(
            CheckIn("c1", "h1", "2026-09-21", CheckInStatus.DONE, null),
            CheckIn("c2", "h2", "2026-09-21", CheckInStatus.PARTIAL, "Did 2 mins")
        )
        val feedback = feedbackEngine.generateFeedback(sampleHabits, checkIns)

        assertTrue(feedback.title.contains("Adaptation"))
        assertTrue(feedback.message.contains("mini-versions"))
    }

    @Test
    fun `SKIPPED check-ins produce gentle guilt-free encouragement`() {
        val checkIns = listOf(
            CheckIn("c1", "h1", "2026-09-21", CheckInStatus.SKIPPED, "Too busy"),
            CheckIn("c2", "h2", "2026-09-21", CheckInStatus.SKIPPED, "Tired")
        )
        val feedback = feedbackEngine.generateFeedback(sampleHabits, checkIns)

        assertTrue(feedback.title.contains("Rest") || feedback.title.contains("Growth"))
        assertTrue(feedback.message.contains("kind to yourself") || feedback.message.contains("ready"))
    }
}
