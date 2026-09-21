package com.smartplanner.model.twin

import com.smartplanner.model.CheckIn
import com.smartplanner.model.CheckInStatus
import com.smartplanner.model.Habit
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Learns per-habit completion probability distributions from check-in history.
 * Pure Kotlin statistical engine decoupled from Android framework.
 */
class HabitDigitalTwinEngine {

    /**
     * Builds a learned statistical profile for each habit based on check-in history.
     */
    fun buildProbabilityProfiles(
        habits: List<Habit>,
        checkIns: List<CheckIn>
    ): Map<String, HabitProbabilityProfile> {
        val profileMap = mutableMapOf<String, HabitProbabilityProfile>()

        for (habit in habits) {
            val habitLogs = checkIns.filter { it.habitId == habit.id }
            val total = habitLogs.size

            if (total == 0) {
                // Default prior when no history exists
                profileMap[habit.id] = HabitProbabilityProfile(
                    habitId = habit.id,
                    habitTitle = habit.title,
                    baseProbability = 0.70
                )
                continue
            }

            val doneCount = habitLogs.count { it.status == CheckInStatus.DONE }
            val partialCount = habitLogs.count { it.status == CheckInStatus.PARTIAL }
            val baseProb = ((doneCount + (partialCount * 0.6)) / total.toDouble()).coerceIn(0.15, 0.95)

            // Day of Week adjustments
            val weekdayMultipliers = mutableMapOf<DayOfWeek, Double>()
            val logsByDayOfWeek = habitLogs.groupBy {
                runCatching { LocalDate.parse(it.date).dayOfWeek }.getOrDefault(DayOfWeek.MONDAY)
            }

            for (day in DayOfWeek.values()) {
                val dayLogs = logsByDayOfWeek[day] ?: emptyList()
                if (dayLogs.isNotEmpty()) {
                    val dayDone = dayLogs.count { it.status == CheckInStatus.DONE || it.status == CheckInStatus.PARTIAL }
                    val dayRate = dayDone / dayLogs.size.toDouble()
                    // Multiplier around base probability
                    val mult = (dayRate / baseProb).coerceIn(0.6, 1.4)
                    weekdayMultipliers[day] = mult
                } else {
                    weekdayMultipliers[day] = 1.0
                }
            }

            // Co-completion bonus with other habits on the same date
            val coCompletionBonus = mutableMapOf<String, Double>()
            val datesWhenHabitDone = habitLogs
                .filter { it.status == CheckInStatus.DONE || it.status == CheckInStatus.PARTIAL }
                .map { it.date }
                .toSet()

            for (other in habits.filter { it.id != habit.id }) {
                val otherLogs = checkIns.filter { it.habitId == other.id && it.date in datesWhenHabitDone }
                val otherDoneCount = otherLogs.count { it.status == CheckInStatus.DONE }
                if (datesWhenHabitDone.isNotEmpty()) {
                    val coRate = otherDoneCount / datesWhenHabitDone.size.toDouble()
                    if (coRate > 0.5) {
                        coCompletionBonus[other.id] = 0.15 // +15% boost when sibling habit is completed
                    }
                }
            }

            profileMap[habit.id] = HabitProbabilityProfile(
                habitId = habit.id,
                habitTitle = habit.title,
                baseProbability = baseProb,
                weekdayMultipliers = weekdayMultipliers,
                coCompletionBonus = coCompletionBonus
            )
        }

        return profileMap
    }

    /**
     * Computes the adjusted probability of completing a habit given the day, preset, and co-completed siblings.
     */
    fun calculateAdjustedProbability(
        profile: HabitProbabilityProfile,
        dayOfWeek: DayOfWeek,
        situationPreset: SituationPreset,
        isMinVersionOnly: Boolean,
        completedSiblingIds: Set<String>
    ): Double {
        var p = profile.baseProbability

        // 1. Weekday multiplier
        val dayMult = profile.weekdayMultipliers[dayOfWeek] ?: 1.0
        p *= dayMult

        // 2. Co-completion synergy bonus
        for (siblingId in completedSiblingIds) {
            val bonus = profile.coCompletionBonus[siblingId] ?: 0.0
            p += bonus
        }

        // 3. Situation modifier
        if (isMinVersionOnly) {
            p = (p * situationPreset.baseModifier * situationPreset.minVersionBoost).coerceAtMost(0.98)
        } else {
            p = (p * situationPreset.baseModifier).coerceAtMost(0.95)
        }

        return p.coerceIn(0.05, 0.98)
    }
}
