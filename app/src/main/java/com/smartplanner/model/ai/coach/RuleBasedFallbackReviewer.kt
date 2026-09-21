package com.smartplanner.model.ai.coach

import com.smartplanner.model.AgentStepLog
import com.smartplanner.model.AnchorType
import com.smartplanner.model.ChangeType
import com.smartplanner.model.CheckInStatus
import com.smartplanner.model.ProposedChange
import com.smartplanner.model.WeeklyReview
import java.time.LocalDate

/**
 * Deterministic offline coach that creates a WeeklyReview with guilt-free adjustments.
 * Operates with zero network dependency.
 */
class RuleBasedFallbackReviewer(
    private val coachTools: CoachTools
) {

    suspend fun generateWeeklyReview(today: LocalDate = LocalDate.now()): WeeklyReview {
        val startDate = today.minusDays(6).toString()
        val endDate = today.toString()
        val trace = mutableListOf<AgentStepLog>()

        // Step 1: Retrieve Check-Ins
        val checkIns = coachTools.getWeeklyCheckIns(7)
        trace.add(
            AgentStepLog(
                stepNumber = 1,
                toolName = "getWeeklyCheckIns",
                inputSummary = "days=7",
                outputSummary = "Retrieved ${checkIns.size} check-in entries",
                rationale = "Analyzing your recent check-in patterns without streak pressure."
            )
        )

        // Step 2: Retrieve Habits
        val habits = coachTools.getHabits()
        trace.add(
            AgentStepLog(
                stepNumber = 2,
                toolName = "getHabits",
                inputSummary = "all",
                outputSummary = "Found ${habits.size} active habits",
                rationale = "Evaluating current habit configurations and minimum versions."
            )
        )

        val completedCount = checkIns.count { it.status == CheckInStatus.DONE }
        val partialCount = checkIns.count { it.status == CheckInStatus.PARTIAL }
        val skippedCount = checkIns.count { it.status == CheckInStatus.SKIPPED }

        val proposedChanges = mutableListOf<ProposedChange>()

        for (habit in habits) {
            val habitLogs = checkIns.filter { it.habitId == habit.id }
            val done = habitLogs.count { it.status == CheckInStatus.DONE }
            val skipped = habitLogs.count { it.status == CheckInStatus.SKIPPED }

            // Heuristic A: 3+ Skipped -> Propose Shrink to Min
            if (skipped >= 3 && habit.minVersion.isNotBlank()) {
                val sim = coachTools.runSimulation(habit.id, ChangeType.SHRINK_TO_MIN)
                trace.add(
                    AgentStepLog(
                        stepNumber = trace.size + 1,
                        toolName = "runSimulation",
                        inputSummary = "habit=${habit.title}, change=SHRINK_TO_MIN",
                        outputSummary = sim.summary,
                        rationale = "Life gets busy. Shrinking to minimum version preserves momentum effortlessly."
                    )
                )

                val proposal = coachTools.proposeChange(
                    habitId = habit.id,
                    changeType = ChangeType.SHRINK_TO_MIN,
                    newValue = habit.minVersion,
                    reason = "You encountered friction on ${skipped} days. Focusing on '${habit.minVersion}' keeps the ritual alive without stress.",
                    expectedImpact = "Predicted consistency recovery: ${(sim.projectedSuccessRate * 100).toInt()}%"
                )
                proposedChanges.add(proposal)
            }
            // Heuristic B: Clock Time Anchor with High Misses -> Propose Context Anchor
            else if (skipped >= 2 && habit.anchorType == AnchorType.CLOCK_TIME) {
                val sim = coachTools.runSimulation(habit.id, ChangeType.CHANGE_ANCHOR)
                val proposal = coachTools.proposeChange(
                    habitId = habit.id,
                    changeType = ChangeType.CHANGE_ANCHOR,
                    newValue = AnchorType.HEADPHONES_CONNECTED.displayName,
                    reason = "Fixed time alarms are easily overlooked. Anchoring to your headphones creates a natural habit cue.",
                    expectedImpact = "Projected trigger match rate: ${(sim.projectedSuccessRate * 100).toInt()}%"
                )
                proposedChanges.add(proposal)
            }
            // Heuristic C: 5+ Done -> Gentle Growth
            else if (done >= 5) {
                val sim = coachTools.runSimulation(habit.id, ChangeType.GROW_TARGET)
                val proposal = coachTools.proposeChange(
                    habitId = habit.id,
                    changeType = ChangeType.GROW_TARGET,
                    newValue = "${habit.title} (+10% Target)",
                    reason = "Outstanding rhythm with ${done} completions! You have room to comfortably expand this habit.",
                    expectedImpact = "Projected stability score: ${(sim.projectedSuccessRate * 100).toInt()}%"
                )
                proposedChanges.add(proposal)
            }
        }

        val praise = if (completedCount + partialCount > 0) {
            "Wonderful effort this week! You engaged in ${completedCount + partialCount} habit sessions. Every small step counts toward lasting identity."
        } else {
            "Welcome to your fresh weekly reset! A clean slate ahead with gentle, adaptable routines designed around your real life."
        }

        val advice = if (proposedChanges.isNotEmpty()) {
            "We found ${proposedChanges.size} personalized tuning suggestions below to make next week smoother and more enjoyable."
        } else {
            "Your current routine balance looks harmonious. Keep listening to your energy levels."
        }

        return WeeklyReview(
            startDate = startDate,
            endDate = endDate,
            summaryPraise = praise,
            gentleAdvice = advice,
            proposedChanges = proposedChanges,
            agentTrace = trace,
            completedCount = completedCount,
            partialCount = partialCount,
            skippedCount = skippedCount,
            isReviewed = false
        )
    }
}
