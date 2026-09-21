package com.smartplanner.twin

import com.smartplanner.model.CheckIn
import com.smartplanner.model.CheckInStatus
import com.smartplanner.model.Habit
import com.smartplanner.model.twin.CandidatePlan
import com.smartplanner.model.twin.HabitDigitalTwinEngine
import com.smartplanner.model.twin.MonteCarloSimulator
import com.smartplanner.model.twin.SituationPreset
import com.smartplanner.model.twin.TwinAiExplainer
import com.smartplanner.model.twin.TwinBacktester
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.random.Random

class DigitalTwinSuiteTest {

    private lateinit var twinEngine: HabitDigitalTwinEngine
    private lateinit var simulator: MonteCarloSimulator
    private lateinit var backtester: TwinBacktester
    private lateinit var explainer: TwinAiExplainer

    @Before
    fun setup() {
        twinEngine = HabitDigitalTwinEngine()
        // Use fixed random seed for reproducible testing
        simulator = MonteCarloSimulator(twinEngine) { Random(42) }
        backtester = TwinBacktester(twinEngine)
        explainer = TwinAiExplainer()
    }

    @Test
    fun `test probability learner extracts base probability and weekday weights`() {
        val habits = listOf(
            Habit(id = "h1", title = "Code", trigger = "Morning", minVersion = "1 commit", aiReasoning = "Focus")
        )
        val checkIns = listOf(
            CheckIn("1", "h1", "2026-09-14", CheckInStatus.DONE, null), // Mon
            CheckIn("2", "h1", "2026-09-15", CheckInStatus.DONE, null), // Tue
            CheckIn("3", "h1", "2026-09-16", CheckInStatus.SKIPPED, null), // Wed
            CheckIn("4", "h1", "2026-09-17", CheckInStatus.DONE, null)  // Thu
        )

        val profiles = twinEngine.buildProbabilityProfiles(habits, checkIns)
        val profile = profiles["h1"]

        assertNotNull(profile)
        assertTrue(profile!!.baseProbability > 0.5)
        assertTrue(profile.weekdayMultipliers.isNotEmpty())
    }

    @Test
    fun `test situation modifier boosts min version probability during sick preset`() {
        val habits = listOf(
            Habit(id = "h2", title = "Exercise", trigger = "Afternoon", minVersion = "5 pushups", aiReasoning = "Health")
        )
        val checkIns = (1..10).map {
            CheckIn(it.toString(), "h2", "2026-09-${10 + it}", CheckInStatus.DONE, null)
        }
        val profiles = twinEngine.buildProbabilityProfiles(habits, checkIns)
        val profile = profiles["h2"]!!

        val pNormal = twinEngine.calculateAdjustedProbability(
            profile, DayOfWeek.MONDAY, SituationPreset.DEFAULT, isMinVersionOnly = false, completedSiblingIds = emptySet()
        )
        val pSickMin = twinEngine.calculateAdjustedProbability(
            profile, DayOfWeek.MONDAY, SituationPreset.SICK, isMinVersionOnly = true, completedSiblingIds = emptySet()
        )

        assertTrue("Min-version during sick state should remain resilient", pSickMin > 0.6)
    }

    @Test
    fun `test Monte Carlo simulator produces 30 day trajectory and percentile bounds`() {
        val candidate = CandidatePlan(
            id = "plan-1",
            name = "Morning Flow",
            description = "Standard habits",
            habitIds = listOf("h1", "h2")
        )
        val habits = listOf(
            Habit(id = "h1", title = "Water", trigger = "Wake up", minVersion = "1 glass", aiReasoning = "Hydration"),
            Habit(id = "h2", title = "Walk", trigger = "Evening", minVersion = "50 steps", aiReasoning = "Cardio")
        )
        val checkIns = (1..14).flatMap { day ->
            listOf(
                CheckIn("w-$day", "h1", "2026-09-${10 + day}", CheckInStatus.DONE, null),
                CheckIn("k-$day", "h2", "2026-09-${10 + day}", CheckInStatus.DONE, null)
            )
        }
        val profiles = twinEngine.buildProbabilityProfiles(habits, checkIns)

        val result = simulator.simulatePlan(candidate, profiles, SituationPreset.DEFAULT, iterations = 500)

        assertEquals(30, result.trajectory.size)
        assertTrue(result.expectedCompletionRate > 0.0)
        assertTrue(result.percentile5th <= result.percentile95th)
        assertTrue(result.expectedConsistencyScore in 0.0..100.0)
    }

    @Test
    fun `test backtester computes Brier score on holdout samples`() {
        val habits = listOf(
            Habit(id = "h1", title = "Journal", trigger = "Night", minVersion = "1 sentence", aiReasoning = "Reflection")
        )
        val checkIns = (1..28).map { day ->
            val status = if (day % 4 == 0) CheckInStatus.SKIPPED else CheckInStatus.DONE
            val dateStr = LocalDate.of(2026, 9, 1).plusDays(day.toLong()).toString()
            CheckIn("j-$day", "h1", dateStr, status, null)
        }

        val report = backtester.runBacktest(habits, checkIns, today = LocalDate.of(2026, 9, 29), holdoutDays = 14)

        assertTrue(report.evaluatedSamplesCount > 0)
        assertTrue(report.brierScore in 0.0..1.0)
        assertTrue(report.calibrationAccuracyPercent in 0.0..100.0)
    }

    @Test
    fun `test AI explainer translates simulation to plain guilt-free language`() {
        val candidate = CandidatePlan(name = "Core Routine", description = "Test", habitIds = listOf("h1"))
        val result = com.smartplanner.model.twin.TwinSimulationResult(
            candidatePlan = candidate,
            situationPreset = SituationPreset.DEFAULT,
            iterationsCount = 1000,
            expectedCompletionRate = 0.88,
            percentile5th = 0.78,
            percentile95th = 0.94,
            expectedConsistencyScore = 88.0,
            riskOfFrictionScore = 0.12,
            trajectory = emptyList()
        )

        val text = explainer.explainSimulation(result)
        assertTrue(text.contains("Digital Twin Analysis"))
        assertTrue(text.contains("88%"))
    }
}
