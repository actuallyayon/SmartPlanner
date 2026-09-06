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
 * Individual habit item anchored to a specific real-world trigger.
 */
data class Habit(
    val id: String,
    val title: String,
    val trigger: String,
    val minVersion: String,
    val aiReasoning: String,
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

