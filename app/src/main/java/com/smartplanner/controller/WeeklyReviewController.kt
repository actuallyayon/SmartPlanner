package com.smartplanner.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartplanner.model.AnchorConfig
import com.smartplanner.model.AnchorType
import com.smartplanner.model.ChangeType
import com.smartplanner.model.ProposalStatus
import com.smartplanner.model.ProposedChange
import com.smartplanner.model.Repository
import com.smartplanner.model.WeeklyReview
import com.smartplanner.model.ai.coach.DefaultCoachTools
import com.smartplanner.model.ai.coach.RuleBasedFallbackReviewer
import com.smartplanner.model.ai.coach.WeeklyCoachAgent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeeklyReviewUiState(
    val isLoading: Boolean = false,
    val review: WeeklyReview? = null,
    val isTraceExpanded: Boolean = false,
    val appliedMessage: String? = null
)

@HiltViewModel
class WeeklyReviewController @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeeklyReviewUiState())
    val uiState: StateFlow<WeeklyReviewUiState> = _uiState.asStateFlow()

    private val coachTools = DefaultCoachTools(repository)
    private val fallbackReviewer = RuleBasedFallbackReviewer(coachTools)
    private val coachAgent = WeeklyCoachAgent(coachTools, fallbackReviewer)

    init {
        loadWeeklyReview()
    }

    fun loadWeeklyReview() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, appliedMessage = null) }
            val review = coachAgent.conductWeeklyReview()
            _uiState.update { it.copy(isLoading = false, review = review) }
        }
    }

    fun toggleTrace() {
        _uiState.update { it.copy(isTraceExpanded = !it.isTraceExpanded) }
    }

    fun acceptProposal(proposal: ProposedChange) {
        viewModelScope.launch {
            // Apply the change directly to the habit in the repository
            val habit = repository.getHabitById(proposal.habitId)
            if (habit != null) {
                when (proposal.changeType) {
                    ChangeType.SHRINK_TO_MIN -> {
                        // Keeps existing habit with minVersion focus
                    }
                    ChangeType.CHANGE_ANCHOR -> {
                        val anchorType = runCatching { AnchorType.valueOf(proposal.newValue) }.getOrDefault(AnchorType.HEADPHONES_CONNECTED)
                        repository.updateHabitAnchor(proposal.habitId, anchorType, habit.anchorConfig ?: AnchorConfig())
                    }
                    ChangeType.PAUSE_HABIT -> {
                        // Marked as inactive in future updates
                    }
                    ChangeType.GROW_TARGET -> {
                        // Level up target
                    }
                }
            }

            // Update UI state with accepted proposal
            _uiState.update { state ->
                val currentReview = state.review ?: return@update state
                val updatedProposals = currentReview.proposedChanges.map {
                    if (it.id == proposal.id) it.copy(status = ProposalStatus.ACCEPTED) else it
                }
                state.copy(
                    review = currentReview.copy(proposedChanges = updatedProposals),
                    appliedMessage = "Applied: ${proposal.changeType.label} for '${proposal.habitTitle}'"
                )
            }
        }
    }

    fun rejectProposal(proposalId: String) {
        _uiState.update { state ->
            val currentReview = state.review ?: return@update state
            val updatedProposals = currentReview.proposedChanges.map {
                if (it.id == proposalId) it.copy(status = ProposalStatus.REJECTED) else it
            }
            state.copy(
                review = currentReview.copy(proposedChanges = updatedProposals),
                appliedMessage = "Dismissed suggestion"
            )
        }
    }

    fun clearAppliedMessage() {
        _uiState.update { it.copy(appliedMessage = null) }
    }
}
