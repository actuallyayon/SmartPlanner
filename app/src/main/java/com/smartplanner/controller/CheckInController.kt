package com.smartplanner.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartplanner.model.CheckInStatus
import com.smartplanner.model.Habit
import com.smartplanner.model.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class CheckInUiState(
    val habits: List<Habit> = emptyList(),
    val statuses: Map<String, CheckInStatus> = emptyMap(), // habitId -> Status
    val notes: Map<String, String> = emptyMap(), // habitId -> Note
    val showSuccess: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class CheckInController @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckInUiState())
    val uiState: StateFlow<CheckInUiState> = _uiState.asStateFlow()

    init {
        loadTodayCheckIns()
    }

    fun loadTodayCheckIns() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val dateStr = LocalDate.now().toString()
            
            // Get habits from active plan
            val habitsList = repository.getActiveHabits().first()
            
            // Get existing check-ins for today
            val todayCheckIns = repository.getCheckInsForDate(dateStr).first()
            
            val initialStatuses = mutableMapOf<String, CheckInStatus>()
            val initialNotes = mutableMapOf<String, String>()
            
            todayCheckIns.forEach { checkIn ->
                initialStatuses[checkIn.habitId] = checkIn.status
                checkIn.note?.let { initialNotes[checkIn.habitId] = it }
            }
            
            _uiState.value = CheckInUiState(
                habits = habitsList,
                statuses = initialStatuses,
                notes = initialNotes,
                showSuccess = false,
                isLoading = false
            )
        }
    }

    fun onStatusSelected(habitId: String, status: CheckInStatus) {
        val currentStatuses = _uiState.value.statuses.toMutableMap()
        currentStatuses[habitId] = status
        _uiState.value = _uiState.value.copy(statuses = currentStatuses, showSuccess = false)
    }

    fun onNoteChanged(habitId: String, note: String) {
        val currentNotes = _uiState.value.notes.toMutableMap()
        currentNotes[habitId] = note
        _uiState.value = _uiState.value.copy(notes = currentNotes, showSuccess = false)
    }

    fun saveCheckIn() {
        viewModelScope.launch {
            val dateStr = LocalDate.now().toString()
            val state = _uiState.value
            
            state.habits.forEach { habit ->
                val status = state.statuses[habit.id]
                if (status != null) {
                    val note = state.notes[habit.id]
                    repository.saveCheckIn(
                        habitId = habit.id,
                        date = dateStr,
                        status = status,
                        note = if (note.isNullOrBlank()) null else note
                    )
                }
            }
            
            _uiState.value = _uiState.value.copy(showSuccess = true)
        }
    }
}
