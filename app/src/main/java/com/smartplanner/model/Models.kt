package com.smartplanner.model

/**
 * Represents an authenticated or guest user profile in SmartPlanner.
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val subscriptionTier: String, // "Free" | "Pro"
)

/**
 * Defines a user's overarching habit plan containing goals, constraints, and habits.
 */
data class HabitPlan(
    val id: String,
    val goals: List<String>,
    val knownObstacles: String,
    val dailyCommitmentMinutes: Int,
    val createdAt: String,
    val habits: List<Habit>,
)

/**
 * Anchor types representing context-aware device signals or scheduled time.
 */
enum class AnchorType(val displayName: String, val description: String) {
    CHARGING_STARTED("Phone Charging Started", "Triggers when device is plugged into power"),
    HEADPHONES_CONNECTED("Headphones Connected", "Triggers when headphones/earbuds are plugged in"),
    ARRIVED_HOME("Arrived Home", "Triggers when entering your home geofence area"),
    FIRST_UNLOCK("First Screen Unlock", "Triggers when unlocking your device in the morning"),
    CLOCK_TIME("Clock Time (Fallback)", "Triggers at a specific scheduled time")
}

/**
 * Configuration options for context-aware habit anchors and rule enforcement.
 */
data class AnchorConfig(
    val clockTimeString: String = "08:00", // "HH:mm"
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radiusMeters: Float = 100f,
    val quietHoursStartHour: Int = 22, // 10 PM (22:00)
    val quietHoursEndHour: Int = 7,   // 7 AM (07:00)
    val cooldownMinutes: Long = 120L   // 2 hours minimum cooldown
)

/**
 * Individual habit item anchored to a specific real-world trigger.
 */
data class Habit(
    val id: String,
    val title: String,
    val trigger: String,
    val minVersion: String,
    val aiReasoning: String,
    val anchorType: AnchorType = AnchorType.CLOCK_TIME,
    val anchorConfig: AnchorConfig? = null,
    val lastReminderTimestamp: Long? = null,
    val lastReminderDate: String? = null
)

/**
 * Record of daily habit completion status and optional notes.
 */
data class CheckIn(
    val id: String,
    val habitId: String,
    val date: String, // "YYYY-MM-DD"
    val status: CheckInStatus, // DONE | PARTIAL | SKIPPED
    val note: String?,
)

/**
 * Status representation for daily habit check-in.
 */
enum class CheckInStatus { DONE, PARTIAL, SKIPPED }

/**
 * Data point for tracking consistency score over time.
 */
data class ProgressPoint(
    val date: String, // "YYYY-MM-DD"
    val consistencyScore: Float, // 0.0f to 1.0f or percentage
)

