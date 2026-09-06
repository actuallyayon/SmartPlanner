package com.smartplanner.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartplanner.model.HabitPlan
import com.smartplanner.model.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlanUiState(
    val plans: List<HabitPlan> = emptyList(),
    val activePlanId: String? = null,
    val showDeleteConfirmDialogFor: HabitPlan? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class PlanController @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    val uiState: StateFlow<PlanUiState> = combine(
        repository.getAllPlans(),
        repository.getActivePlan()
    ) { allPlans, activePlan ->
        PlanUiState(
            plans = allPlans,
            activePlanId = activePlan?.id,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlanUiState(isLoading = true)
    )

    private val _dialogState = MutableStateFlow<HabitPlan?>(null)
    val deleteConfirmPlan: StateFlow<HabitPlan?> = _dialogState.asStateFlow()

    fun selectPlan(planId: String) {
        viewModelScope.launch {
            repository.setActivePlan(planId)
        }
    }

    fun requestDeletePlan(plan: HabitPlan) {
        _dialogState.value = plan
    }

    fun cancelDeletePlan() {
        _dialogState.value = null
    }

    fun confirmDeletePlan() {
        val plan = _dialogState.value
        if (plan != null) {
            viewModelScope.launch {
                repository.deletePlan(plan.id)
                _dialogState.value = null
            }
        }
    }
}
