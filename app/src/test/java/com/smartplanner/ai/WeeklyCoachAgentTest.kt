package com.smartplanner.ai

import com.smartplanner.model.AnchorConfig
import com.smartplanner.model.AnchorType
import com.smartplanner.model.ChangeType
import com.smartplanner.model.CheckInRecord
import com.smartplanner.model.CheckInStatus
import com.smartplanner.model.Habit
import com.smartplanner.model.ProposalStatus
import com.smartplanner.model.Repository
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
                anchorConfig = AnchorConfig(AnchorType.CLOCK_TIME)
            )
        )
        fakeRepository.checkIns = listOf(
            CheckInRecord("1", "h1", "2026-09-15", CheckInStatus.SKIPPED),
            CheckInRecord("2", "h1", "2026-09-16", CheckInStatus.SKIPPED),
            CheckInRecord("3", "h1", "2026-09-17", CheckInStatus.SKIPPED)
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
                anchorConfig = AnchorConfig(AnchorType.HEADPHONES_PLUGGED)
            )
        )
        fakeRepository.checkIns = (1..6).map {
            CheckInRecord(it.toString(), "h2", "2026-09-${14 + it}", CheckInStatus.DONE)
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
            Habit(id = "h3", title = "Walk", trigger = "Noon", minVersion = "100 steps")
        )
        fakeRepository.checkIns = emptyList()

        val review = agent.conductWeeklyReview(apiKey = null)
        assertNotNull(review)
        assertTrue(review.summaryPraise.isNotBlank())
    }

    @Test
    fun `test human-in-the-loop guarantee does not mutate repository until approved`() = runBlocking {
        val originalAnchor = AnchorConfig(AnchorType.CLOCK_TIME)
        fakeRepository.habits = listOf(
            Habit(id = "h4", title = "Yoga", trigger = "7am", anchorConfig = originalAnchor)
        )
        fakeRepository.checkIns = listOf(
            CheckInRecord("1", "h4", "2026-09-18", CheckInStatus.SKIPPED),
            CheckInRecord("2", "h4", "2026-09-19", CheckInStatus.SKIPPED)
        )

        val review = agent.conductWeeklyReview()
        // Ensure repository anchor was NOT changed behind the user's back
        val habitInRepo = fakeRepository.getHabitById("h4")
        assertEquals(AnchorType.CLOCK_TIME, habitInRepo?.anchorConfig?.type)
        assertEquals(ProposalStatus.PENDING, review.proposedChanges.firstOrNull()?.status ?: ProposalStatus.PENDING)
    }

    private class FakeRepository : Repository {
        var habits: List<Habit> = emptyList()
        var checkIns: List<CheckInRecord> = emptyList()

        override suspend fun getHabits(): List<Habit> = habits
        override suspend fun getHabitById(id: String): Habit? = habits.find { it.id == id }
        override suspend fun updateHabitAnchor(habitId: String, config: AnchorConfig) {
            habits = habits.map { if (it.id == habitId) it.copy(anchorConfig = config) else it }
        }
        override suspend fun getCheckIns(): List<CheckInRecord> = checkIns
        override suspend fun logCheckIn(record: CheckInRecord) { checkIns = checkIns + record }
        override fun getHabitsFlow(): Flow<List<Habit>> = MutableStateFlow(habits)
        override fun getCheckInsFlow(): Flow<List<CheckInRecord>> = MutableStateFlow(checkIns)
    }
}
