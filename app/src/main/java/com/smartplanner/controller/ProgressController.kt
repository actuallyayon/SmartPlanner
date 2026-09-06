package com.smartplanner.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartplanner.model.ProgressPoint
import com.smartplanner.model.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ProgressUiState(
    val points: List<ProgressPoint> = emptyList(),
    val isEmptyState: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class ProgressController @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _isEmptyState = MutableStateFlow(false)
    val isEmptyState = _isEmptyState.asStateFlow()

    val uiState: StateFlow<ProgressUiState> = combine(
        repository.getProgressPoints(),
        _isEmptyState
    ) { points, isEmpty ->
        ProgressUiState(
            points = if (isEmpty) emptyList() else points,
            isEmptyState = isEmpty,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProgressUiState(isLoading = true)
    )

    fun toggleEmptyState() {
        _isEmptyState.value = !_isEmptyState.value
    }
}
