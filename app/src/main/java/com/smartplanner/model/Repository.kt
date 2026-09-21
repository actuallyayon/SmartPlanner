package com.smartplanner.model

import kotlinx.coroutines.flow.Flow

interface Repository {
    // Auth State
    fun isUserLoggedIn(): Flow<Boolean>
    fun loginDemoUser()
    fun logout()

    // User Operations
    fun getCurrentUser(): Flow<User?>
    fun updateCurrentUser(name: String, email: String)

    // Habit Plan Operations
    fun getActivePlan(): Flow<HabitPlan?>
    fun getActiveHabits(): Flow<List<Habit>>
    fun getAllPlans(): Flow<List<HabitPlan>>
    fun createPlan(goals: List<String>, knownObstacles: String, dailyCommitmentMinutes: Int): HabitPlan
    fun deletePlan(planId: String)
    fun setActivePlan(planId: String)

    // Check-In Operations
    fun getCheckInsForDate(date: String): Flow<List<CheckIn>>
    fun saveCheckIn(habitId: String, date: String, status: CheckInStatus, note: String?)

    // Anchor & Habit Operations
    fun updateHabitAnchor(habitId: String, anchorType: AnchorType, anchorConfig: AnchorConfig?)
    fun recordHabitReminderFired(habitId: String, timestamp: Long, dateStr: String)
    fun getHabitById(habitId: String): Habit?
    fun updateHabit(habit: Habit)

    // Progress Operations
    fun getProgressPoints(): Flow<List<ProgressPoint>>

    // AI Daily Feedback Operations
    fun getLatestAiFeedback(): Flow<com.smartplanner.model.ai.AiFeedback?>
    fun saveAiFeedback(feedback: com.smartplanner.model.ai.AiFeedback)
    fun dismissAiFeedback()
}
