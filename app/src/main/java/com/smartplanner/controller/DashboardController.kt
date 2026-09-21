package com.smartplanner.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartplanner.model.Habit
import com.smartplanner.model.HabitPlan
import com.smartplanner.model.Repository
import com.smartplanner.model.User
import com.smartplanner.model.ai.AiFeedback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val user: User? = null,
    val activePlan: HabitPlan? = null,
    val habits: List<Habit> = emptyList(),
    val aiFeedback: AiFeedback? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class DashboardController @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getCurrentUser(),
        repository.getActivePlan(),
        repository.getActiveHabits(),
        repository.getLatestAiFeedback()
    ) { user, plan, habits, feedback ->
        DashboardUiState(
            user = user,
            activePlan = plan,
            habits = habits,
            aiFeedback = feedback,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )

    fun dismissAiFeedback() {
        repository.dismissAiFeedback()
    }
}
