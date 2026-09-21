package com.smartplanner.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartplanner.model.AnchorConfig
import com.smartplanner.model.AnchorType
import com.smartplanner.model.Habit
import com.smartplanner.model.Repository
import com.smartplanner.model.anchor.AnchorRulesEngine
import com.smartplanner.model.anchor.ReminderEvaluationResult
import com.smartplanner.notification.SmartPlannerNotificationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AnchorUiState(
    val habits: List<Habit> = emptyList(),
    val selectedHabitForEdit: Habit? = null,
    val showEditDialog: Boolean = false,
    val lastTriggerMessage: String? = null,
    val lastTriggerAllowed: Boolean = true,
    val locationPermissionNeeded: Boolean = false,
    val notificationPermissionNeeded: Boolean = false
)

@HiltViewModel
class AnchorController @Inject constructor(
    private val repository: Repository,
    private val notificationManager: SmartPlannerNotificationManager
) : ViewModel() {

    private val rulesEngine = AnchorRulesEngine()

    private val _uiState = MutableStateFlow(AnchorUiState())
    val uiState: StateFlow<AnchorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getActiveHabits().collect { habitsList ->
                _uiState.value = _uiState.value.copy(habits = habitsList)
            }
        }
    }

    fun selectHabitForEdit(habit: Habit) {
        val locationNeeded = habit.anchorType == AnchorType.ARRIVED_HOME
        _uiState.value = _uiState.value.copy(
            selectedHabitForEdit = habit,
            showEditDialog = true,
            locationPermissionNeeded = locationNeeded
        )
    }

    fun dismissEditDialog() {
        _uiState.value = _uiState.value.copy(
            selectedHabitForEdit = null,
            showEditDialog = false
        )
    }

    fun onAnchorTypeChangedInEditor(newType: AnchorType) {
        val current = _uiState.value.selectedHabitForEdit ?: return
        val locationNeeded = newType == AnchorType.ARRIVED_HOME
        _uiState.value = _uiState.value.copy(
            selectedHabitForEdit = current.copy(anchorType = newType),
            locationPermissionNeeded = locationNeeded
        )
    }

    fun saveAnchorConfig(
        habitId: String,
        anchorType: AnchorType,
        clockTime: String,
        cooldownMinutes: Long,
        quietStartHour: Int,
        quietEndHour: Int,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        val config = AnchorConfig(
            clockTimeString = clockTime,
            cooldownMinutes = cooldownMinutes,
            quietHoursStartHour = quietStartHour,
            quietHoursEndHour = quietEndHour,
            latitude = latitude,
            longitude = longitude
        )

        repository.updateHabitAnchor(habitId, anchorType, config)
        dismissEditDialog()
    }

    /**
     * Simulates triggering the hardware/context signal for a given habit.
     * Passes through AnchorRulesEngine to evaluate daily limit, cooldown, and quiet hours.
     */
    fun testSignalTrigger(habitId: String) {
        val habit = repository.getHabitById(habitId) ?: return

        when (val result = rulesEngine.evaluate(habit)) {
            is ReminderEvaluationResult.Allowed -> {
                val now = System.currentTimeMillis()
                val todayStr = LocalDate.now().toString()
                repository.recordHabitReminderFired(habitId, now, todayStr)

                val posted = notificationManager.showHabitAnchorNotification(habit)
                val msg = if (posted) {
                    "✓ Anchor triggered! Gentle notification sent with Done / Partial / Skipped buttons."
                } else {
                    "✓ Anchor permitted! (Enable notification permissions to see the system pop-up)."
                }

                _uiState.value = _uiState.value.copy(
                    lastTriggerMessage = msg,
                    lastTriggerAllowed = true
                )
            }
            is ReminderEvaluationResult.SuppressedAlreadyFiredToday -> {
                _uiState.value = _uiState.value.copy(
                    lastTriggerMessage = "Reminder suppressed: already triggered today (${result.date}) to prevent notification fatigue.",
                    lastTriggerAllowed = false
                )
            }
            is ReminderEvaluationResult.SuppressedCooldown -> {
                _uiState.value = _uiState.value.copy(
                    lastTriggerMessage = "Reminder suppressed by cooldown: please wait ${result.remainingMinutes} more minutes.",
                    lastTriggerAllowed = false
                )
            }
            is ReminderEvaluationResult.SuppressedQuietHours -> {
                _uiState.value = _uiState.value.copy(
                    lastTriggerMessage = "Reminder suppressed: current time is within quiet hours (${result.startHour}:00 - ${result.endHour}:00).",
                    lastTriggerAllowed = false
                )
            }
        }
    }

    fun clearTriggerMessage() {
        _uiState.value = _uiState.value.copy(lastTriggerMessage = null)
    }
}
