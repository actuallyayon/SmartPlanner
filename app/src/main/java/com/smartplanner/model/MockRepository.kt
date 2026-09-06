package com.smartplanner.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockRepository @Inject constructor() : Repository {

    private val loggedInFlow = MutableStateFlow(false)
    
    private val currentUserFlow = MutableStateFlow<User?>(
        User(
            id = "user_1",
            name = "Demo User",
            email = "demo@smartplanner.com",
            subscriptionTier = "Pro"
        )
    )

    // Predefined Habit Lists
    private val spanishHabits = listOf(
        Habit(
            id = "h_vocab",
            title = "Daily Spanish Vocabulary",
            trigger = "Right after breakfast",
            minVersion = "5 minutes of flashcard review",
            aiReasoning = "Building vocabulary is essential for learning Spanish, and starting small helps create a consistent habit."
        ),
        Habit(
            id = "h_app",
            title = "Spanish Language Learning App",
            trigger = "During daily commute",
            minVersion = "10 minutes of interactive lessons",
            aiReasoning = "Utilizing commute time for learning Spanish helps maximize available time and creates a consistent daily routine."
        ),
        Habit(
            id = "h_podcast",
            title = "Spanish Podcast Listening",
            trigger = "Right before bed",
            minVersion = "10 minutes of listening to a beginner-friendly podcast",
            aiReasoning = "Consistent input of spoken Spanish right before sleep helps reinforce vocabulary and listening comprehension."
        ),
        Habit(
            id = "h_speaking",
            title = "Spanish Speaking Practice",
            trigger = "While preparing dinner",
            minVersion = "5 minutes of speaking aloud to yourself",
            aiReasoning = "Speaking aloud even for a few minutes daily builds confidence and speaking fluency."
        )
    )

    private val examHabits = listOf(
        Habit(
            id = "h_ex1",
            title = "Review Lecture Notes",
            trigger = "At 10:00 AM",
            minVersion = "15 minutes review",
            aiReasoning = "Immediate review of daily concepts prevents long-term cramming and solidifies knowledge."
        ),
        Habit(
            id = "h_ex2",
            title = "Solve Practice Questions",
            trigger = "Right after lunch",
            minVersion = "2 problems",
            aiReasoning = "Application-based learning is the highest-leverage way to prepare for technical exams."
        ),
        Habit(
            id = "h_ex3",
            title = "Active Recall Session",
            trigger = "Before dinner",
            minVersion = "10 minutes of self-testing",
            aiReasoning = "Forcing active recall strengthens neural pathways more than passive reading."
        ),
        Habit(
            id = "h_ex4",
            title = "Clarify Unresolved Topics",
            trigger = "At the end of the day",
            minVersion = "Write down 1 question to look up",
            aiReasoning = "Cataloging uncertainty prevents conceptual blind spots from compounding."
        ),
        Habit(
            id = "h_ex5",
            title = "Deep Work Session",
            trigger = "9:00 AM in the library",
            minVersion = "30 minutes focused study",
            aiReasoning = "Establishing a specific study environment primes the brain for deep concentration."
        )
    )

    private val defaultPlansList = mutableListOf(
        HabitPlan(
            id = "plan_spanish",
            goals = listOf("Learn Spanish"),
            knownObstacles = "I'm beginner. How to start?",
            dailyCommitmentMinutes = 30,
            createdAt = LocalDate.now().minusWeeks(2).toString(),
            habits = spanishHabits
        ),
        HabitPlan(
            id = "plan_exam",
            goals = listOf("Good result in exam"),
            knownObstacles = "Hard to focus, easily distracted.",
            dailyCommitmentMinutes = 120,
            createdAt = LocalDate.now().minusWeeks(1).toString(),
            habits = examHabits
        )
    )

    private val plansFlow = MutableStateFlow<List<HabitPlan>>(defaultPlansList)
    private val activePlanIdFlow = MutableStateFlow<String?>("plan_spanish")
    private val checkInsFlow = MutableStateFlow<Map<String, CheckIn>>(emptyMap())

    init {
        // Pre-populate mock check-ins for the last 14 days
        val mockCheckInsMap = mutableMapOf<String, CheckIn>()
        val today = LocalDate.now()
        
        // Let's populate check-ins for plan_spanish habits
        for (i in 1..14) {
            val dateStr = today.minusDays(i.toLong()).toString()
            spanishHabits.forEachIndexed { index, habit ->
                // Make a mix of DONE, PARTIAL, SKIPPED
                val status = when {
                    (i + index) % 7 == 0 -> CheckInStatus.SKIPPED
                    (i + index) % 3 == 0 -> CheckInStatus.PARTIAL
                    else -> CheckInStatus.DONE
                }
                val note = when (status) {
                    CheckInStatus.DONE -> if (i % 4 == 0) "Felt great and energetic today!" else null
                    CheckInStatus.PARTIAL -> "Ran out of time, did the bare minimum."
                    CheckInStatus.SKIPPED -> "Really busy today, stack got neglected."
                }
                val checkInId = "${dateStr}_${habit.id}"
                mockCheckInsMap[checkInId] = CheckIn(
                    id = checkInId,
                    habitId = habit.id,
                    date = dateStr,
                    status = status,
                    note = note
                )
            }
        }
        checkInsFlow.value = mockCheckInsMap
    }

    override fun isUserLoggedIn(): Flow<Boolean> = loggedInFlow

    override fun loginDemoUser() {
        loggedInFlow.value = true
    }

    override fun logout() {
        loggedInFlow.value = false
    }

    override fun getCurrentUser(): Flow<User?> = currentUserFlow

    override fun updateCurrentUser(name: String, email: String) {
        val current = currentUserFlow.value
        if (current != null) {
            currentUserFlow.value = current.copy(name = name, email = email)
        }
    }

    override fun getActivePlan(): Flow<HabitPlan?> {
        return activePlanIdFlow.map { activeId ->
            plansFlow.value.find { it.id == activeId }
        }
    }

    override fun getActiveHabits(): Flow<List<Habit>> {
        return getActivePlan().map { plan ->
            plan?.habits ?: emptyList()
        }
    }

    override fun getAllPlans(): Flow<List<HabitPlan>> = plansFlow

    override fun createPlan(goals: List<String>, knownObstacles: String, dailyCommitmentMinutes: Int): HabitPlan {
        // Generate a new plan simulating the AI stacked plan
        // We will generate the habits dynamically based on the goal description or use the default stack
        val newPlanId = "plan_gen_${System.currentTimeMillis()}"
        
        // Simulating different goals by creating mock habits based on prompt
        val promptText = goals.firstOrNull() ?: "General Routine"
        val generatedHabits = when {
            promptText.contains("spanish", ignoreCase = true) || promptText.contains("language", ignoreCase = true) -> spanishHabits
            promptText.contains("exam", ignoreCase = true) || promptText.contains("study", ignoreCase = true) || promptText.contains("learn", ignoreCase = true) -> examHabits
            else -> {
                // Default generic adapt routine habits
                listOf(
                    Habit(
                        id = "h_gen_1",
                        title = "Focus Block",
                        trigger = "At 9:00 AM",
                        minVersion = "10 minutes of reading",
                        aiReasoning = "Establishing a consistent start to your day build focus."
                    ),
                    Habit(
                        id = "h_gen_2",
                        title = "Physical Movement",
                        trigger = "After lunch",
                        minVersion = "5 minute walk",
                        aiReasoning = "Brief activity combats afternoon slumps and improves cardiovascular health."
                    ),
                    Habit(
                        id = "h_gen_3",
                        title = "Reflection & Journaling",
                        trigger = "Before going to sleep",
                        minVersion = "Write 1 line in journal",
                        aiReasoning = "Reflecting on your day builds self-awareness and triggers planning for tomorrow."
                    )
                )
            }
        }

        val newPlan = HabitPlan(
            id = newPlanId,
            goals = goals,
            knownObstacles = knownObstacles,
            dailyCommitmentMinutes = dailyCommitmentMinutes,
            createdAt = LocalDate.now().toString(),
            habits = generatedHabits
        )

        // Add to plans list
        val currentPlans = plansFlow.value.toMutableList()
        currentPlans.add(newPlan)
        plansFlow.value = currentPlans
        
        // Set as active plan
        activePlanIdFlow.value = newPlanId
        
        return newPlan
    }

    override fun deletePlan(planId: String) {
        val currentPlans = plansFlow.value.toMutableList()
        val index = currentPlans.indexOfFirst { it.id == planId }
        if (index != -1) {
            currentPlans.removeAt(index)
            plansFlow.value = currentPlans
            
            // If the deleted plan was active, reset to another plan if available
            if (activePlanIdFlow.value == planId) {
                activePlanIdFlow.value = currentPlans.firstOrNull()?.id
            }
        }
    }

    override fun setActivePlan(planId: String) {
        if (plansFlow.value.any { it.id == planId }) {
            activePlanIdFlow.value = planId
        }
    }

    override fun getCheckInsForDate(date: String): Flow<List<CheckIn>> {
        return checkInsFlow.map { map ->
            map.values.filter { it.date == date }
        }
    }

    override fun saveCheckIn(habitId: String, date: String, status: CheckInStatus, note: String?) {
        val checkInId = "${date}_${habitId}"
        val currentMap = checkInsFlow.value.toMutableMap()
        currentMap[checkInId] = CheckIn(
            id = checkInId,
            habitId = habitId,
            date = date,
            status = status,
            note = note
        )
        checkInsFlow.value = currentMap
    }

    override fun getProgressPoints(): Flow<List<ProgressPoint>> {
        // Compute consistency score over the last 14 days based on actual checkInsFlow
        return checkInsFlow.map { checkInsMap ->
            val today = LocalDate.now()
            val points = mutableListOf<ProgressPoint>()
            
            for (i in (0..13).reversed()) {
                val dateStr = today.minusDays(i.toLong()).toString()
                
                // Get all active habits for that day (mocking the active habits as the current active plan's habits)
                val activeHabitIds = getActivePlanHabitsSync()
                
                if (activeHabitIds.isEmpty()) {
                    points.add(ProgressPoint(dateStr, 0f))
                    continue
                }
                
                var pointsEarned = 0f
                var habitsTracked = 0
                
                activeHabitIds.forEach { habitId ->
                    val checkIn = checkInsMap["${dateStr}_${habitId}"]
                    if (checkIn != null) {
                        habitsTracked++
                        pointsEarned += when (checkIn.status) {
                            CheckInStatus.DONE -> 1.0f
                            CheckInStatus.PARTIAL -> 0.5f
                            CheckInStatus.SKIPPED -> 0.0f
                        }
                    }
                }
                
                // Consistency is percentage of total achievable score (1.0 per habit)
                val score = if (habitsTracked > 0) (pointsEarned / habitsTracked) * 100f else 0f
                points.add(ProgressPoint(dateStr, score))
            }
            points
        }
    }

    // Helper to get active habits synchronously for progress calculation
    private fun getActivePlanHabitsSync(): List<String> {
        val activeId = activePlanIdFlow.value
        val activePlan = plansFlow.value.find { it.id == activeId }
        return activePlan?.habits?.map { it.id } ?: emptyList()
    }
}
