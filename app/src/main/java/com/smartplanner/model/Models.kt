package com.smartplanner.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val subscriptionTier: String, // "Free" | "Pro"
)

data class HabitPlan(
    val id: String,
    val goals: List<String>,
    val knownObstacles: String,
    val dailyCommitmentMinutes: Int,
    val createdAt: String,
    val habits: List<Habit>,
)

data class Habit(
    val id: String,
    val title: String,
    val trigger: String,
    val minVersion: String,
    val aiReasoning: String,
)

data class CheckIn(
    val id: String,
    val habitId: String,
    val date: String, // "YYYY-MM-DD"
    val status: CheckInStatus, // DONE | PARTIAL | SKIPPED
    val note: String?,
)

enum class CheckInStatus { DONE, PARTIAL, SKIPPED }

data class ProgressPoint(
    val date: String, // "YYYY-MM-DD"
    val consistencyScore: Float, // 0.0f to 1.0f or percentage
)
