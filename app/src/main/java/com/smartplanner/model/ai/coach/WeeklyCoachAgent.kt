package com.smartplanner.model.ai.coach

import com.smartplanner.model.AgentStepLog
import com.smartplanner.model.ChangeType
import com.smartplanner.model.ProposedChange
import com.smartplanner.model.WeeklyReview
import java.time.LocalDate

/**
 * Agentic Weekly Coach that reasons step-by-step over check-in history,
 * runs Monte Carlo / simulation twins, and proposes non-destructive routine improvements.
 */
class WeeklyCoachAgent(
    private val coachTools: CoachTools,
    private val fallbackReviewer: RuleBasedFallbackReviewer
) {

    suspend fun conductWeeklyReview(
        apiKey: String? = null,
        today: LocalDate = LocalDate.now()
    ): WeeklyReview {
        // If no API key or network disabled, run deterministic rule-based fallback
        if (apiKey.isNullOrBlank()) {
            return fallbackReviewer.generateWeeklyReview(today)
        }

        return try {
            runAgentLoop(today)
        } catch (e: Exception) {
            // Fallback gracefully on any failure
            fallbackReviewer.generateWeeklyReview(today)
        }
    }

    private suspend fun runAgentLoop(today: LocalDate): WeeklyReview {
        val trace = mutableListOf<AgentStepLog>()
        val maxSteps = 6
        var currentStep = 1

        // Step 1: Query Check-Ins
        val checkIns = coachTools.getWeeklyCheckIns(7)
        trace.add(
            AgentStepLog(
                stepNumber = currentStep++,
                toolName = "getWeeklyCheckIns",
                inputSummary = "days=7",
                outputSummary = "Retrieved ${checkIns.size} check-ins for the past 7 days",
                rationale = "Analyzing consistency and energy trends over the week."
            )
        )

        // Step 2: Query Habits
        val habits = coachTools.getHabits()
        trace.add(
            AgentStepLog(
                stepNumber = currentStep++,
                toolName = "getHabits",
                inputSummary = "status=ACTIVE",
                outputSummary = "Retrieved ${habits.size} registered habits",
                rationale = "Mapping check-in trends against minimum versions and anchor triggers."
            )
        )

        val proposedChanges = mutableListOf<ProposedChange>()

        // Step 3-5: Run simulation and propose changes within step limit
        for (habit in habits) {
            if (currentStep >= maxSteps) break

            val habitCheckIns = checkIns.filter { it.habitId == habit.id }
            val skippedCount = habitCheckIns.count { it.status.name == "SKIPPED" }

            if (skippedCount >= 2 && habit.minVersion.isNotBlank()) {
                val simResult = coachTools.runSimulation(habit.id, ChangeType.SHRINK_TO_MIN)
                trace.add(
                    AgentStepLog(
                        stepNumber = currentStep++,
                        toolName = "runSimulation",
                        inputSummary = "habitId=${habit.id}, type=SHRINK_TO_MIN",
                        outputSummary = simResult.summary,
                        rationale = "Evaluating if falling back to the 2-minute version reduces friction."
                    )
                )

                if (currentStep <= maxSteps) {
                    val proposal = coachTools.proposeChange(
                        habitId = habit.id,
                        changeType = ChangeType.SHRINK_TO_MIN,
                        newValue = habit.minVersion,
                        reason = "Scaling down removes resistance so you can show up effortlessly.",
                        expectedImpact = "High consistency forecast (${(simResult.projectedSuccessRate * 100).toInt()}%)"
                    )
                    proposedChanges.add(proposal)
                }
            }
        }

        val completed = checkIns.count { it.status.name == "DONE" }
        val partial = checkIns.count { it.status.name == "PARTIAL" }
        val skipped = checkIns.count { it.status.name == "SKIPPED" }

        return WeeklyReview(
            startDate = today.minusDays(6).toString(),
            endDate = today.toString(),
            summaryPraise = "Great job investing in yourself this week! You showed up for ${completed + partial} habit sessions.",
            gentleAdvice = if (proposedChanges.isNotEmpty()) {
                "Your AI Coach prepared ${proposedChanges.size} suggestions to simplify your routine."
            } else {
                "Your rhythm is well-balanced. Keep enjoying your daily rituals."
            },
            proposedChanges = proposedChanges,
            agentTrace = trace,
            completedCount = completed,
            partialCount = partial,
            skippedCount = skipped,
            isReviewed = false
        )
    }
}
