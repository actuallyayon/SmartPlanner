package com.smartplanner.model.twin

/**
 * Generates plain-language, guilt-free explanations from simulation and backtest results.
 * Principle: The algorithmic engine computes probabilities and Monte Carlo trajectories;
 * the AI explains the meaning clearly without hallucination.
 */
class TwinAiExplainer {

    fun explainSimulation(result: TwinSimulationResult): String {
        val pct = (result.expectedCompletionRate * 100).toInt()
        val p5 = (result.percentile5th * 100).toInt()
        val p95 = (result.percentile95th * 100).toInt()
        val preset = result.situationPreset

        val presetContext = when (preset) {
            SituationPreset.DEFAULT -> "under your standard daily rhythm"
            SituationPreset.SICK -> "during low-energy or recovery periods"
            SituationPreset.TRAVELING -> "while navigating travel and changing environments"
            SituationPreset.CRUNCH_DAY -> "during busy high-stress work crunch days"
            SituationPreset.RECOVERY -> "on rest and reset days"
        }

        return "🤖 **Digital Twin Analysis**: For candidate plan '**${result.candidatePlan.name}**' $presetContext, " +
                "the 30-day Monte Carlo simulation (1,000 runs) projects an expected consistency of **$pct%** " +
                "(90% confidence interval: $p5% – $p95%). " +
                if (preset == SituationPreset.SICK || preset == SituationPreset.CRUNCH_DAY) {
                    "Falling back to minimum versions keeps momentum high with minimal friction."
                } else {
                    "Your habit anchors provide strong habit cues across weekdays."
                }
    }

    fun explainBacktest(report: BacktestReport): String {
        val acc = "%.1f".format(report.calibrationAccuracyPercent)
        val brier = "%.3f".format(report.brierScore)
        return "🎯 **Historical Backtest (${report.holdoutDays}-Day Holdout)**: Evaluated ${report.evaluatedSamplesCount} check-in events. " +
                "The twin achieved **$acc% directional accuracy** with a Brier Score of **$brier**. ${report.summaryEvaluation}"
    }
}
