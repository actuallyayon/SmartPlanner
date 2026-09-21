package com.smartplanner.ai

import com.smartplanner.model.AnchorConfig
import com.smartplanner.model.AnchorType
import com.smartplanner.model.ChangeType
import com.smartplanner.model.CheckIn
import com.smartplanner.model.CheckInStatus
import com.smartplanner.model.Habit
import com.smartplanner.model.HabitPlan
import com.smartplanner.model.ProgressPoint
import com.smartplanner.model.ProposalStatus
import com.smartplanner.model.Repository
import com.smartplanner.model.User
import com.smartplanner.model.ai.AiFeedback
import com.smartplanner.model.ai.coach.DefaultCoachTools
import com.smartplanner.model.ai.coach.RuleBasedFallbackReviewer
import com.smartplanner.model.ai.coach.WeeklyCoachAgent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class WeeklyCoachAgentTest {

    private lateinit var fakeRepository: FakeRepository
    private lateinit var coachTools: DefaultCoachTools
    private lateinit var fallbackReviewer: RuleBasedFallbackReviewer
    private lateinit var agent: WeeklyCoachAgent

    @Before
    fun setup() {
        fakeRepository = FakeRepository()
        coachTools = DefaultCoachTools(fakeRepository)
        fallbackReviewer = RuleBasedFallbackReviewer(coachTools)
        agent = WeeklyCoachAgent(coachTools, fallbackReviewer)
    }

    @Test
    fun `test fallback reviewer proposes shrink to min when habit is frequently skipped`() = runBlocking {
        fakeRepository.habits = listOf(
            Habit(
                id = "h1",
                title = "Meditate 20 mins",
                trigger = "Morning",
                minVersion = "1 deep breath",
                aiReasoning = "Helps center focus",
                anchorType = AnchorType.CLOCK_TIME
            )
        )
        fakeRepository.checkIns = listOf(
            CheckIn("1", "h1", "2026-09-15", CheckInStatus.SKIPPED, null),
            CheckIn("2", "h1", "2026-09-16", CheckInStatus.SKIPPED, null),
            CheckIn("3", "h1", "2026-09-17", CheckInStatus.SKIPPED, null)
        )

        val review = fallbackReviewer.generateWeeklyReview(LocalDate.of(2026, 9, 21))

        assertNotNull(review)
        assertEquals(1, review.proposedChanges.size)
        val proposal = review.proposedChanges[0]
        assertEquals(ChangeType.SHRINK_TO_MIN, proposal.changeType)
        assertEquals("1 deep breath", proposal.newValue)
        assertEquals(ProposalStatus.PENDING, proposal.status)
        assertTrue(review.agentTrace.isNotEmpty())
    }

    @Test
    fun `test fallback reviewer proposes grow target when habit is consistently completed`() = runBlocking {
        fakeRepository.habits = listOf(
            Habit(
                id = "h2",
                title = "Read 5 pages",
                trigger = "Night",
                minVersion = "1 page",
                aiReasoning = "Compound learning",
                anchorType = AnchorType.HEADPHONES_CONNECTED
            )
        )
        fakeRepository.checkIns = (1..6).map {
            CheckIn(it.toString(), "h2", "2026-09-${14 + it}", CheckInStatus.DONE, null)
        }

        val review = fallbackReviewer.generateWeeklyReview(LocalDate.of(2026, 9, 21))

        assertNotNull(review)
        assertEquals(1, review.proposedChanges.size)
        assertEquals(ChangeType.GROW_TARGET, review.proposedChanges[0].changeType)
        assertFalse(review.summaryPraise.contains("streak lost", ignoreCase = true))
    }

    @Test
    fun `test agent conducts review gracefully offline with zero crash`() = runBlocking {
        fakeRepository.habits = listOf(
            Habit(id = "h3", title = "Walk", trigger = "Noon", minVersion = "100 steps", aiReasoning = "Movement")
        )
        fakeRepository.checkIns = emptyList()

        val review = agent.conductWeeklyReview(apiKey = null)
        assertNotNull(review)
        assertTrue(review.summaryPraise.isNotBlank())
    }

    @Test
    fun `test human-in-the-loop guarantee does not mutate repository until approved`() = runBlocking {
        fakeRepository.habits = listOf(
            Habit(id = "h4", title = "Yoga", trigger = "7am", minVersion = "1 stretch", aiReasoning = "Flexibility", anchorType = AnchorType.CLOCK_TIME)
        )
        fakeRepository.checkIns = listOf(
            CheckIn("1", "h4", "2026-09-18", CheckInStatus.SKIPPED, null),
            CheckIn("2", "h4", "2026-09-19", CheckInStatus.SKIPPED, null)
        )

        val review = agent.conductWeeklyReview()
        // Ensure repository anchor was NOT changed behind the user's back
        val habitInRepo = fakeRepository.getHabitById("h4")
        assertEquals(AnchorType.CLOCK_TIME, habitInRepo?.anchorType)
        assertEquals(ProposalStatus.PENDING, review.proposedChanges.firstOrNull()?.status ?: ProposalStatus.PENDING)
    }

    private class FakeRepository : Repository {
        var habits: List<Habit> = emptyList()
        var checkIns: List<CheckIn> = emptyList()

        override fun isUserLoggedIn(): Flow<Boolean> = MutableStateFlow(true)
        override fun loginDemoUser() {}
        override fun logout() {}
        override fun getCurrentUser(): Flow<User?> = MutableStateFlow(null)
        override fun updateCurrentUser(name: String, email: String) {}
        override fun getActivePlan(): Flow<HabitPlan?> = MutableStateFlow(null)
        override fun getActiveHabits(): Flow<List<Habit>> = MutableStateFlow(habits)
        override fun getAllPlans(): Flow<List<HabitPlan>> = MutableStateFlow(emptyList())
        override fun createPlan(goals: List<String>, knownObstacles: String, dailyCommitmentMinutes: Int): HabitPlan {
            return HabitPlan("1", goals, knownObstacles, dailyCommitmentMinutes, "today", habits)
        }
        override fun deletePlan(planId: String) {}
        override fun setActivePlan(planId: String) {}
        override fun getCheckInsForDate(date: String): Flow<List<CheckIn>> = MutableStateFlow(checkIns)
        override fun saveCheckIn(habitId: String, date: String, status: CheckInStatus, note: String?) {
            checkIns = checkIns + CheckIn("new", habitId, date, status, note)
        }
        override fun updateHabitAnchor(habitId: String, anchorType: AnchorType, anchorConfig: AnchorConfig?) {
            habits = habits.map { if (it.id == habitId) it.copy(anchorType = anchorType, anchorConfig = anchorConfig) else it }
        }
        override fun recordHabitReminderFired(habitId: String, timestamp: Long, dateStr: String) {}
        override fun getHabitById(habitId: String): Habit? = habits.find { it.id == habitId }
        override fun updateHabit(habit: Habit) {
            habits = habits.map { if (it.id == habit.id) habit else it }
        }
        override fun getProgressPoints(): Flow<List<ProgressPoint>> = MutableStateFlow(emptyList())
        override fun getLatestAiFeedback(): Flow<AiFeedback?> = MutableStateFlow(null)
        override fun saveAiFeedback(feedback: AiFeedback) {}
        override fun dismissAiFeedback() {}
    }
}
