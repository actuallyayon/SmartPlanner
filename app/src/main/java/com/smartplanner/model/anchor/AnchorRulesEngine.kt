package com.smartplanner.model.anchor

import com.smartplanner.model.Habit
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Interface abstracting time operations for deterministic testing.
 */
interface TimeProvider {
    fun currentTimeMillis(): Long
    fun currentDateString(): String
    fun currentHourOfDay(): Int
}

/**
 * Default system time provider using Java 8 Time API.
 */
class SystemTimeProvider : TimeProvider {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
    override fun currentDateString(): String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    override fun currentHourOfDay(): Int = LocalTime.now().hour
}

/**
 * Result of evaluating whether an anchor reminder is permitted to fire.
 */
sealed class ReminderEvaluationResult {
    object Allowed : ReminderEvaluationResult()
    data class SuppressedAlreadyFiredToday(val date: String) : ReminderEvaluationResult()
    data class SuppressedCooldown(val remainingMinutes: Long) : ReminderEvaluationResult()
    data class SuppressedQuietHours(val currentHour: Int, val startHour: Int, val endHour: Int) : ReminderEvaluationResult()
}

/**
 * Pure rule engine verifying anchor firing constraints.
 */
class AnchorRulesEngine(
    private val timeProvider: TimeProvider = SystemTimeProvider()
) {
    /**
     * Evaluates whether a habit reminder is allowed to trigger right now.
     */
    fun evaluate(habit: Habit): ReminderEvaluationResult {
        val config = habit.anchorConfig
        val currentDate = timeProvider.currentDateString()
        val currentMillis = timeProvider.currentTimeMillis()
        val currentHour = timeProvider.currentHourOfDay()

        // Rule 1: At most one reminder per habit per day
        if (habit.lastReminderDate == currentDate) {
            return ReminderEvaluationResult.SuppressedAlreadyFiredToday(currentDate)
        }

        // Rule 2: Cooldown check
        val cooldownMinutes = config?.cooldownMinutes ?: 120L
        val lastTimestamp = habit.lastReminderTimestamp
        if (lastTimestamp != null && lastTimestamp > 0) {
            val elapsedMinutes = (currentMillis - lastTimestamp) / (60 * 1000)
            if (elapsedMinutes < cooldownMinutes) {
                val remainingMinutes = cooldownMinutes - elapsedMinutes
                return ReminderEvaluationResult.SuppressedCooldown(remainingMinutes)
            }
        }

        // Rule 3: Quiet Hours check
        val quietStart = config?.quietHoursStartHour ?: 22
        val quietEnd = config?.quietHoursEndHour ?: 7
        if (isQuietHour(currentHour, quietStart, quietEnd)) {
            return ReminderEvaluationResult.SuppressedQuietHours(currentHour, quietStart, quietEnd)
        }

        return ReminderEvaluationResult.Allowed
    }

    /**
     * Determines if a given hour falls inside the quiet window.
     * Supports overnight windows (e.g. 22:00 -> 07:00).
     */
    fun isQuietHour(hour: Int, startHour: Int, endHour: Int): Boolean {
        return if (startHour > endHour) {
            // Overnight window (e.g., 22:00 to 07:00)
            hour >= startHour || hour < endHour
        } else if (startHour < endHour) {
            // Same-day window (e.g., 13:00 to 15:00)
            hour in startHour until endHour
        } else {
            // start == end means quiet hours disabled
            false
        }
    }
}
