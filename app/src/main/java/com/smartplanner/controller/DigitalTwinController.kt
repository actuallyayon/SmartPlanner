package com.smartplanner.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartplanner.model.CheckIn
import com.smartplanner.model.Habit
import com.smartplanner.model.Repository
import com.smartplanner.model.twin.BacktestReport
import com.smartplanner.model.twin.CandidatePlan
import com.smartplanner.model.twin.HabitDigitalTwinEngine
import com.smartplanner.model.twin.MonteCarloSimulator
import com.smartplanner.model.twin.SituationPreset
import com.smartplanner.model.twin.TwinAiExplainer
import com.smartplanner.model.twin.TwinBacktester
import com.smartplanner.model.twin.TwinSimulationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DigitalTwinUiState(
    val isLoading: Boolean = false,
    val selectedPreset: SituationPreset = SituationPreset.DEFAULT,
    val candidatePlans: List<CandidatePlan> = emptyList(),
    val selectedPlan: CandidatePlan? = null,
    val simulationResult: TwinSimulationResult? = null,
    val allCandidateResults: List<TwinSimulationResult> = emptyList(),
    val bestPlan: CandidatePlan? = null,
    val backtestReport: BacktestReport? = null,
    val aiExplanation: String? = null
)

@HiltViewModel
class DigitalTwinController @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DigitalTwinUiState())
    val uiState: StateFlow<DigitalTwinUiState> = _uiState.asStateFlow()

    private val twinEngine = HabitDigitalTwinEngine()
    private val simulator = MonteCarloSimulator(twinEngine)
    private val backtester = TwinBacktester(twinEngine)
    private val explainer = TwinAiExplainer()

    init {
        initializePlans()
    }

    private fun initializePlans() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val habits = repository.getActiveHabits().firstOrNull() ?: emptyList()
            val habitIds = habits.map { it.id }

            val defaultCandidates = listOf(
                CandidatePlan(
                    name = "Balanced Rhythm",
                    description = "Standard full-habit targets with balanced anchors",
                    habitIds = habitIds,
                    usesMinVersionByDefault = false,
                    preferredSituation = SituationPreset.DEFAULT
                ),
                CandidatePlan(
                    name = "Low-Friction Flow",
                    description = "Focuses primarily on 2-minute minimum versions to guarantee consistency",
                    habitIds = habitIds,
                    usesMinVersionByDefault = true,
                    preferredSituation = SituationPreset.SICK
                ),
                CandidatePlan(
                    name = "Core Essentials Only",
                    description = "Streamlined plan retaining only top 2 primary anchor habits",
                    habitIds = habitIds.take(2),
                    usesMinVersionByDefault = false,
                    preferredSituation = SituationPreset.CRUNCH_DAY
                )
            )

            _uiState.update {
                it.copy(
                    candidatePlans = defaultCandidates,
                    selectedPlan = defaultCandidates.firstOrNull()
                )
            }

            runSimulation()
            runBacktest()
        }
    }

    fun selectPreset(preset: SituationPreset) {
        _uiState.update { it.copy(selectedPreset = preset) }
        runSimulation()
    }

    fun selectPlan(plan: CandidatePlan) {
        _uiState.update { it.copy(selectedPlan = plan) }
        runSimulation()
    }

    fun runSimulation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val habits = repository.getActiveHabits().firstOrNull() ?: emptyList()
            val checkIns = collectRecentCheckIns(30)

            val profiles = twinEngine.buildProbabilityProfiles(habits, checkIns)
            val currentPreset = _uiState.value.selectedPreset
            val candidates = _uiState.value.candidatePlans

            if (candidates.isNotEmpty()) {
                val (best, allResults) = simulator.findBestPlan(
                    candidatePlans = candidates,
                    profiles = profiles,
                    situationPreset = currentPreset,
                    iterations = 1000
                )

                val selectedPlan = _uiState.value.selectedPlan ?: best
                val activeResult = allResults.find { it.candidatePlan.id == selectedPlan.id } ?: allResults.first()
                val explanation = explainer.explainSimulation(activeResult)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        simulationResult = activeResult.copy(aiExplanation = explanation),
                        allCandidateResults = allResults,
                        bestPlan = best,
                        aiExplanation = explanation
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun runBacktest() {
        viewModelScope.launch {
            val habits = repository.getActiveHabits().firstOrNull() ?: emptyList()
            val checkIns = collectRecentCheckIns(45)
            val report = backtester.runBacktest(habits, checkIns)

            _uiState.update { it.copy(backtestReport = report) }
        }
    }

    private suspend fun collectRecentCheckIns(days: Int): List<CheckIn> {
        val today = LocalDate.now()
        val all = mutableListOf<CheckIn>()
        for (i in 0 until days) {
            val dateStr = today.minusDays(i.toLong()).toString()
            val list = repository.getCheckInsForDate(dateStr).firstOrNull() ?: emptyList()
            all.addAll(list)
        }
        return all
    }
}
