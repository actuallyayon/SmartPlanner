package com.smartplanner.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.smartplanner.model.MockRepository
import com.smartplanner.model.Repository
import com.smartplanner.model.ai.coach.DefaultCoachTools
import com.smartplanner.model.ai.coach.RuleBasedFallbackReviewer

/**
 * Background WorkManager worker that runs weekly to prepare routine suggestions
 * and posts a gentle, guilt-free notification.
 */
class WeeklyReviewWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val repository: Repository = MockRepository()
            val tools = DefaultCoachTools(repository)
            val reviewer = RuleBasedFallbackReviewer(tools)
            val review = reviewer.generateWeeklyReview()

            // Post gentle notification
            val notificationManager = SmartPlannerNotificationManager(applicationContext)
            notificationManager.sendWeeklyReviewNotification(
                title = "🌱 Weekly Reset & Suggestions Ready",
                message = "We prepared ${review.proposedChanges.size} gentle tweaks for your routines. Take a look when you have a moment."
            )

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
