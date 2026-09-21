package com.smartplanner.model.ai.coach

import com.smartplanner.model.AnchorType
import com.smartplanner.model.ChangeType
import com.smartplanner.model.CheckInRecord
import com.smartplanner.model.CheckInStatus
import com.smartplanner.model.Habit
import com.smartplanner.model.ProposedChange
import com.smartplanner.model.Repository

/**
 * Interface defining tools callable by the Weekly Coach Agent during its multi-step loop.
 */
interface CoachTools {
    suspend fun getWeeklyCheckIns(daysCount: Int = 7): List<CheckInRecord>
    suspend fun getHabits(): List<Habit>
    suspend fun runSimulation(habitId: String, proposedChangeType: ChangeType): SimulationResult
    suspend fun proposeChange(
        habitId: String,
        changeType: ChangeType,
        newValue: String,
        reason: String,
        expectedImpact: String
    ): ProposedChange
}

data class SimulationResult(
    val habitId: String,
    val currentEstimatedSuccessRate: Double,
    val projectedSuccessRate: Double,
    val summary: String
)

/**
 * Default implementation of CoachTools that queries the Repository and calculates projections.
 */
class DefaultCoachTools(
    private val repository: Repository
) : CoachTools {

    override suspend fun getWeeklyCheckIns(daysCount: Int): List<CheckInRecord> {
        return repository.getCheckIns()
    }

    override suspend fun getHabits(): List<Habit> {
        return repository.getHabits()
    }

    override suspend fun runSimulation(habitId: String, proposedChangeType: ChangeType): SimulationResult {
        val checkIns = repository.getCheckIns().filter { it.habitId == habitId }
        val doneCount = checkIns.count { it.status == CheckInStatus.DONE }
        val partialCount = checkIns.count { it.status == CheckInStatus.PARTIAL }
        val total = checkIns.size.coerceAtLeast(1)

        val currentRate = (doneCount + (partialCount * 0.5)) / total.toDouble()

        val projectedRate = when (proposedChangeType) {
            ChangeType.SHRINK_TO_MIN -> (currentRate + 0.35).coerceAtMost(0.95)
            ChangeType.CHANGE_ANCHOR -> (currentRate + 0.25).coerceAtMost(0.90)
            ChangeType.PAUSE_HABIT -> 1.0
            ChangeType.GROW_TARGET -> (currentRate * 0.90).coerceAtLeast(0.70)
        }

        return SimulationResult(
            habitId = habitId,
            currentEstimatedSuccessRate = currentRate,
            projectedSuccessRate = projectedRate,
            summary = "Simulated ${proposedChangeType.label}: expected success rate moves from ${(currentRate * 100).toInt()}% to ${(projectedRate * 100).toInt()}%"
        )
    }

    override suspend fun proposeChange(
        habitId: String,
        changeType: ChangeType,
        newValue: String,
        reason: String,
        expectedImpact: String
    ): ProposedChange {
        val habit = repository.getHabits().find { it.id == habitId }
        val habitTitle = habit?.title ?: "Unknown Habit"
        val oldValue = when (changeType) {
            ChangeType.SHRINK_TO_MIN -> habit?.minVersion ?: "None"
            ChangeType.CHANGE_ANCHOR -> habit?.anchorConfig?.type?.name ?: "CLOCK_TIME"
            ChangeType.PAUSE_HABIT -> "Active"
            ChangeType.GROW_TARGET -> habit?.title ?: ""
        }

        return ProposedChange(
            habitId = habitId,
            habitTitle = habitTitle,
            changeType = changeType,
            oldValue = oldValue,
            newValue = newValue,
            reason = reason,
            expectedImpact = expectedImpact
        )
    }
}
