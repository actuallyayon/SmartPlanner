package com.smartplanner.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartplanner.model.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val currentStep: Int = 1,
    val goals: List<String> = listOf(""), // starts with one empty goal input
    val obstacles: String = "",
    val commitmentMinutes: String = "30",
    val error: String? = null,
    val isCompleted: Boolean = false
)

@HiltViewModel
class OnboardingController @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState = _uiState.asStateFlow()

    fun nextStep() {
        val currentState = _uiState.value
        when (currentState.currentStep) {
            1 -> {
                val nonBlankGoals = currentState.goals.filter { it.isNotBlank() }
                if (nonBlankGoals.isEmpty()) {
                    _uiState.value = currentState.copy(error = "Please add at least one goal")
                    return
                }
                _uiState.value = currentState.copy(
                    goals = nonBlankGoals,
                    currentStep = 2,
                    error = null
                )
            }
            2 -> {
                if (currentState.obstacles.isBlank()) {
                    _uiState.value = currentState.copy(error = "Please describe your obstacles")
                    return
                }
                _uiState.value = currentState.copy(currentStep = 3, error = null)
            }
            3 -> {
                val minutes = currentState.commitmentMinutes.toIntOrNull()
                if (minutes == null || minutes <= 0) {
                    _uiState.value = currentState.copy(error = "Please enter a valid duration in minutes")
                    return
                }
                completeOnboarding()
            }
        }
    }

    fun previousStep() {
        val current = _uiState.value.currentStep
        if (current > 1) {
            _uiState.value = _uiState.value.copy(currentStep = current - 1, error = null)
        }
    }

    fun onGoalChanged(index: Int, value: String) {
        val updatedGoals = _uiState.value.goals.toMutableList()
        if (index in updatedGoals.indices) {
            updatedGoals[index] = value
            _uiState.value = _uiState.value.copy(goals = updatedGoals, error = null)
        }
    }

    fun addGoalField() {
        val updatedGoals = _uiState.value.goals.toMutableList()
        updatedGoals.add("")
        _uiState.value = _uiState.value.copy(goals = updatedGoals)
    }

    fun removeGoalField(index: Int) {
        val updatedGoals = _uiState.value.goals.toMutableList()
        if (updatedGoals.size > 1 && index in updatedGoals.indices) {
            updatedGoals.removeAt(index)
            _uiState.value = _uiState.value.copy(goals = updatedGoals)
        }
    }

    fun onObstaclesChanged(value: String) {
        _uiState.value = _uiState.value.copy(obstacles = value, error = null)
    }

    fun onCommitmentMinutesChanged(value: String) {
        _uiState.value = _uiState.value.copy(commitmentMinutes = value, error = null)
    }

    fun reset() {
        _uiState.value = OnboardingUiState()
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            val state = _uiState.value
            val minutes = state.commitmentMinutes.toIntOrNull() ?: 30
            
            repository.createPlan(
                goals = state.goals,
                knownObstacles = state.obstacles,
                dailyCommitmentMinutes = minutes
            )
            
            _uiState.value = state.copy(isCompleted = true)
        }
    }
}
