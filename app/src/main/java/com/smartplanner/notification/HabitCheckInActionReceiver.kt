package com.smartplanner.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.smartplanner.model.CheckInStatus
import com.smartplanner.model.Repository
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import javax.inject.Inject

/**
 * BroadcastReceiver for handling notification action button clicks (Done, Partial, Skipped).
 * Logs the check-in to the Repository directly without opening the full application UI.
 */
@AndroidEntryPoint
class HabitCheckInActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: Repository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == SmartPlannerNotificationManager.ACTION_HABIT_CHECK_IN) {
            val habitId = intent.getStringExtra(SmartPlannerNotificationManager.EXTRA_HABIT_ID) ?: return
            val statusStr = intent.getStringExtra(SmartPlannerNotificationManager.EXTRA_CHECK_IN_STATUS) ?: return
            val notificationId = intent.getIntExtra(SmartPlannerNotificationManager.EXTRA_NOTIFICATION_ID, -1)
            val dateStr = intent.getStringExtra(SmartPlannerNotificationManager.EXTRA_DATE) ?: LocalDate.now().toString()

            val status = try {
                CheckInStatus.valueOf(statusStr)
            } catch (e: Exception) {
                CheckInStatus.DONE
            }

            // Save check-in via Repository
            val note = when (status) {
                CheckInStatus.DONE -> "Checked in via anchor reminder"
                CheckInStatus.PARTIAL -> "Completed min version via anchor reminder"
                CheckInStatus.SKIPPED -> "Acknowledged via anchor reminder"
            }
            repository.saveCheckIn(habitId, dateStr, status, note)

            // Dismiss the notification
            if (notificationId != -1) {
                NotificationManagerCompat.from(context).cancel(notificationId)
            }
        }
    }
}
