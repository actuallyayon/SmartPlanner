package com.smartplanner.model.twin

import java.time.DayOfWeek
import java.util.UUID

/**
 * Real-world situation modifier affecting completion probabilities.
 */
enum class SituationPreset(
    val displayName: String,
    val description: String,
    val baseModifier: Double,
    val minVersionBoost: Double
) {
    DEFAULT("Normal Routine", "Standard daily rhythm and energy levels", 1.0, 1.0),
    SICK("Feeling Sick / Low Energy", "Low stamina; boosts min-version habit completion", 0.4, 2.2),
    TRAVELING("Traveling / On The Move", "Altered environment and unfamiliar daily cues", 0.6, 1.8),
    CRUNCH_DAY("Work Crunch / High Stress", "Tight schedule with high cognitive load", 0.5, 2.0),
    RECOVERY("Rest & Reset Day", "Gentle momentum focus without heavy targets", 0.8, 1.5)
}

/**
 * Habit time slots for temporal probability adjustments.
 */
enum class TimeSlot(val label: String) {
    EARLY_MORNING("06:00 - 09:00"),
    MORNING("09:00 - 12:00"),
    AFTERNOON("12:00 - 17:00"),
    EVENING("17:00 - 21:00"),
    NIGHT("21:00 - 00:00")
}

/**
 * Learned historical probability model for an individual habit.
 */
data class HabitProbabilityProfile(
    val habitId: String,
    val habitTitle: String,
    val baseProbability: Double, // 0.0 to 1.0
    val weekdayMultipliers: Map<DayOfWeek, Double> = emptyMap(),
    val timeSlotMultipliers: Map<TimeSlot, Double> = emptyMap(),
    val coCompletionBonus: Map<String, Double> = emptyMap() // habitId -> boost
)

/**
 * Candidate routine plan evaluated by the Monte Carlo simulator.
 */
data class CandidatePlan(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val habitIds: List<String>,
    val usesMinVersionByDefault: Boolean = false,
    val preferredSituation: SituationPreset = SituationPreset.DEFAULT
)

/**
 * Daily aggregated trajectory point inside a 30-day Monte Carlo run.
 */
data class SimulationDayPoint(
    val dayIndex: Int, // 1 to 30
    val averageCompletedCount: Double,
    val averagePartialCount: Double,
    val confidenceLower: Double,
    val confidenceUpper: Double
)

/**
 * Full output of a 30-day Monte Carlo Twin Simulation.
 */
data class TwinSimulationResult(
    val candidatePlan: CandidatePlan,
    val situationPreset: SituationPreset,
    val iterationsCount: Int = 1000,
    val expectedCompletionRate: Double, // e.g. 0.84 (84%)
    val percentile5th: Double,
    val percentile95th: Double,
    val expectedConsistencyScore: Double,
    val riskOfFrictionScore: Double, // 0.0 to 1.0 (lower is smoother)
    val trajectory: List<SimulationDayPoint>,
    val aiExplanation: String? = null
)

/**
 * Backtesting accuracy report obtained by hiding the last 14 days of historical check-ins.
 */
data class BacktestReport(
    val holdoutDays: Int = 14,
    val evaluatedSamplesCount: Int,
    val brierScore: Double, // 0.0 is perfect calibration, 1.0 is total error
    val meanAbsoluteError: Double,
    val calibrationAccuracyPercent: Double,
    val summaryEvaluation: String
)
