package com.smartplanner.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.smartplanner.R
import com.smartplanner.model.CheckInStatus
import com.smartplanner.model.Habit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartPlannerNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID = "smart_planner_anchors"
        const val CHANNEL_NAME = "Habit Anchors & Reminders"
        const val CHANNEL_DESC = "Context-aware gentle reminders triggered by real-world anchors"

        const val ACTION_HABIT_CHECK_IN = "com.smartplanner.ACTION_HABIT_CHECK_IN"
        const val EXTRA_HABIT_ID = "extra_habit_id"
        const val EXTRA_CHECK_IN_STATUS = "extra_check_in_status"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_DATE = "extra_date"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Shows a gentle, guilt-free context-aware habit reminder with 3 one-tap action buttons.
     */
    fun showHabitAnchorNotification(habit: Habit): Boolean {
        if (!hasNotificationPermission()) return false

        val notificationId = habit.id.hashCode()
        val todayStr = LocalDate.now().toString()

        // Create PendingIntents for Done, Partial, Skipped
        val donePendingIntent = createCheckInPendingIntent(habit.id, CheckInStatus.DONE, notificationId, todayStr, 1)
        val partialPendingIntent = createCheckInPendingIntent(habit.id, CheckInStatus.PARTIAL, notificationId, todayStr, 2)
        val skippedPendingIntent = createCheckInPendingIntent(habit.id, CheckInStatus.SKIPPED, notificationId, todayStr, 3)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Gentle Anchor: ${habit.title}")
            .setContentText("Trigger: ${habit.trigger} • Min version: ${habit.minVersion}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Trigger: ${habit.trigger}\nMin fallback: ${habit.minVersion}\n\nTake a moment when you're ready — no pressure, any progress counts!")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(0, "✓ Done", donePendingIntent)
            .addAction(0, "⚡ Partial", partialPendingIntent)
            .addAction(0, "⏭ Skipped", skippedPendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            return true
        } catch (e: SecurityException) {
            return false
        }
    }

    private fun createCheckInPendingIntent(
        habitId: String,
        status: CheckInStatus,
        notificationId: Int,
        date: String,
        requestCode: Int
    ): PendingIntent {
        val intent = Intent(context, HabitCheckInActionReceiver::class.java).apply {
            action = ACTION_HABIT_CHECK_IN
            putExtra(EXTRA_HABIT_ID, habitId)
            putExtra(EXTRA_CHECK_IN_STATUS, status.name)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(EXTRA_DATE, date)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(
            context,
            habitId.hashCode() * 10 + requestCode,
            intent,
            flags
        )
    }
}
