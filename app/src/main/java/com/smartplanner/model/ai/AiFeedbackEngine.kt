package com.smartplanner.model.ai

import com.smartplanner.model.CheckIn
import com.smartplanner.model.CheckInStatus
import com.smartplanner.model.Habit
import java.time.LocalDate

data class AiFeedback(
    val title: String,
    val message: String,
    val date: String = LocalDate.now().toString(),
    val tag: String = "AI Habit Coach"
)

/**
 * Pure Kotlin AI feedback generator that reviews daily check-ins and produces
 * guilt-free, encouraging, and adaptive motivational feedback.
 */
class AiFeedbackEngine {

    fun generateFeedback(habits: List<Habit>, checkIns: List<CheckIn>): AiFeedback {
        if (checkIns.isEmpty()) {
            return AiFeedback(
                title = "✨ Ready for Today",
                message = "Welcome back! Check in as you complete your anchors today — remember, even 1 minute counts."
            )
        }

        val doneCount = checkIns.count { it.status == CheckInStatus.DONE }
        val partialCount = checkIns.count { it.status == CheckInStatus.PARTIAL }
        val skippedCount = checkIns.count { it.status == CheckInStatus.SKIPPED }
        val total = checkIns.size

        val doneHabitTitles = checkIns.filter { it.status == CheckInStatus.DONE }
            .mapNotNull { ci -> habits.find { it.id == ci.habitId }?.title }

        return when {
            doneCount == total && total > 0 -> {
                val highlight = if (doneHabitTitles.isNotEmpty()) " especially with ${doneHabitTitles.first()}" else ""
                AiFeedback(
                    title = "🌟 Incredible Consistency!",
                    message = "You nailed every single habit today${highlight}! Your anchor routines are fitting seamlessly into your day. Keep up the fantastic momentum!"
                )
            }
            doneCount > 0 && partialCount > 0 -> {
                AiFeedback(
                    title = "⚡ Great Adaptation Today!",
                    message = "You completed $doneCount full habits and smartly used your mini-versions for $partialCount. Adapting to your energy is true habit mastery!"
                )
            }
            doneCount > 0 && skippedCount > 0 -> {
                AiFeedback(
                    title = "🌱 Solid Progress!",
                    message = "Great job finishing $doneCount habits today! Don't worry about the skipped ones — real routines flex around real life. Tomorrow is a fresh start."
                )
            }
            partialCount > 0 && doneCount == 0 -> {
                AiFeedback(
                    title = "💪 Showing Up Matters!",
                    message = "You completed your mini-versions today! Showing up for even 2 minutes keeps the neural pathway alive without burnout. Proud of you!"
                )
            }
            skippedCount == total && total > 0 -> {
                AiFeedback(
                    title = "💙 Rest is Part of Growth",
                    message = "You acknowledged today with honesty. Busy or tiring days happen to everyone — be kind to yourself. Your anchors will be right here whenever you're ready."
                )
            }
            else -> {
                AiFeedback(
                    title = "✨ Progress Saved!",
                    message = "Your check-ins are logged! You are doing great taking small, consistent steps towards your goals."
                )
            }
        }
    }
}
