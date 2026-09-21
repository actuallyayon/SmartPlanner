package com.smartplanner.model.twin

import java.time.LocalDate
import kotlin.random.Random

/**
 * Monte Carlo Simulation Engine that forecasts 30-day routine trajectories.
 * Evaluates candidate habit plans stochastically under various situation presets.
 */
class MonteCarloSimulator(
    private val twinEngine: HabitDigitalTwinEngine = HabitDigitalTwinEngine(),
    private val randomProvider: () -> Random = { Random.Default }
) {

    /**
     * Simulates a candidate plan over 30 days across N Monte Carlo trials.
     */
    fun simulatePlan(
        plan: CandidatePlan,
        profiles: Map<String, HabitProbabilityProfile>,
        situationPreset: SituationPreset = plan.preferredSituation,
        startDate: LocalDate = LocalDate.now(),
        iterations: Int = 1000
    ): TwinSimulationResult {
        val random = randomProvider()
        val daysCount = 30

        // Per-iteration total completion rates
        val iterationRates = DoubleArray(iterations)
        val dayCompletedTotals = DoubleArray(daysCount)
        val dayPartialTotals = DoubleArray(daysCount)

        for (iter in 0 until iterations) {
            var iterTotalHabitEvents = 0
            var iterSuccessfulEvents = 0

            for (dayIdx in 0 until daysCount) {
                val currentDate = startDate.plusDays(dayIdx.toLong())
                val dayOfWeek = currentDate.dayOfWeek
                val completedInDay = mutableSetOf<String>()

                var dayCompletedCount = 0
                var dayPartialCount = 0

                for (habitId in plan.habitIds) {
                    val profile = profiles[habitId] ?: HabitProbabilityProfile(habitId, "Habit", 0.70)
                    val p = twinEngine.calculateAdjustedProbability(
                        profile = profile,
                        dayOfWeek = dayOfWeek,
                        situationPreset = situationPreset,
                        isMinVersionOnly = plan.usesMinVersionByDefault,
                        completedSiblingIds = completedInDay
                    )

                    iterTotalHabitEvents++
                    val draw = random.nextDouble()

                    if (draw < p) {
                        // Full completion
                        completedInDay.add(habitId)
                        iterSuccessfulEvents++
                        dayCompletedCount++
                    } else if (draw < (p + (1.0 - p) * 0.40)) {
                        // Partial (min-version fallback)
                        iterSuccessfulEvents += 1
                        dayPartialCount++
                    }
                }

                dayCompletedTotals[dayIdx] += dayCompletedCount.toDouble()
                dayPartialTotals[dayIdx] += dayPartialCount.toDouble()
            }

            iterationRates[iter] = if (iterTotalHabitEvents > 0) {
                iterSuccessfulEvents.toDouble() / iterTotalHabitEvents.toDouble()
            } else 0.0
        }

        // Sort to calculate percentiles
        iterationRates.sort()
        val expectedRate = iterationRates.average()
        val p5 = iterationRates[(iterations * 0.05).toInt().coerceIn(0, iterations - 1)]
        val p95 = iterationRates[(iterations * 0.95).toInt().coerceIn(0, iterations - 1)]

        val trajectory = (0 until daysCount).map { dayIdx ->
            val avgDone = dayCompletedTotals[dayIdx] / iterations.toDouble()
            val avgPartial = dayPartialTotals[dayIdx] / iterations.toDouble()
            SimulationDayPoint(
                dayIndex = dayIdx + 1,
                averageCompletedCount = avgDone,
                averagePartialCount = avgPartial,
                confidenceLower = (avgDone * 0.85).coerceAtLeast(0.0),
                confidenceUpper = avgDone * 1.15
            )
        }

        val riskOfFriction = (1.0 - expectedRate).coerceIn(0.0, 1.0)
        val consistencyScore = (expectedRate * 100.0).coerceIn(0.0, 100.0)

        return TwinSimulationResult(
            candidatePlan = plan,
            situationPreset = situationPreset,
            iterationsCount = iterations,
            expectedCompletionRate = expectedRate,
            percentile5th = p5,
            percentile95th = p95,
            expectedConsistencyScore = consistencyScore,
            riskOfFrictionScore = riskOfFriction,
            trajectory = trajectory
        )
    }

    /**
     * Ranks candidate plans and picks the one with the highest expected consistency score.
     */
    fun findBestPlan(
        candidatePlans: List<CandidatePlan>,
        profiles: Map<String, HabitProbabilityProfile>,
        situationPreset: SituationPreset = SituationPreset.DEFAULT,
        startDate: LocalDate = LocalDate.now(),
        iterations: Int = 1000
    ): Pair<CandidatePlan, List<TwinSimulationResult>> {
        val results = candidatePlans.map { plan ->
            simulatePlan(plan, profiles, situationPreset, startDate, iterations)
        }
        val best = results.maxByOrNull { it.expectedConsistencyScore }?.candidatePlan
            ?: candidatePlans.first()
        return Pair(best, results)
    }
}
