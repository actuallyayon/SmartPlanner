package com.smartplanner.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartplanner.controller.OnboardingController
import com.smartplanner.view.theme.PrimaryBlue

@Composable
fun OnboardingScreen(
    onboardingController: OnboardingController,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by onboardingController.uiState.collectAsState()

    if (state.isCompleted) {
        onboardingController.reset()
        onComplete()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title block
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Setup Your Routine",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Let's build a plan tailored to your lifestyle",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        // Step Indicator Row
        StepIndicator(currentStep = state.currentStep)

        // Error message if any
        if (state.error != null) {
            Text(
                text = state.error ?: "",
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        // Content Area Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (state.currentStep) {
                    1 -> Step1Content(
                        goals = state.goals,
                        onGoalChanged = { idx, valStr -> onboardingController.onGoalChanged(idx, valStr) },
                        onAddGoal = { onboardingController.addGoalField() },
                        onRemoveGoal = { idx -> onboardingController.removeGoalField(idx) }
                    )
                    2 -> Step2Content(
                        obstacles = state.obstacles,
                        onObstaclesChanged = { onboardingController.onObstaclesChanged(it) }
                    )
                    3 -> Step3Content(
                        commitmentMinutes = state.commitmentMinutes,
                        onCommitmentMinutesChanged = { onboardingController.onCommitmentMinutesChanged(it) }
                    )
                }
            }
        }

        // Navigation Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (state.currentStep > 1) {
                TextButton(
                    onClick = { onboardingController.previousStep() }
                ) {
                    Text("Back", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            Button(
                onClick = { onboardingController.nextStep() },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text(
                    text = if (state.currentStep == 3) "Generate Habit Stack" else "Next Step",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StepIndicator(currentStep: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (step in 1..3) {
            val isActive = step == currentStep
            val isPassed = step < currentStep

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isActive -> PrimaryBlue
                            isPassed -> PrimaryBlue.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.outline
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = step.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            if (step < 3) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(3.dp)
                        .background(
                            if (isPassed) PrimaryBlue.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
                        )
                )
            }
        }
    }
}

@Composable
fun Step1Content(
    goals: List<String>,
    onGoalChanged: (Int, String) -> Unit,
    onAddGoal: () -> Unit,
    onRemoveGoal: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Step 1: What do you want to achieve?",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Define the goals you want to stack habits for (e.g. Learn Spanish, Exercise regularly).",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        goals.forEachIndexed { index, goal ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = goal,
                    onValueChange = { onGoalChanged(index, it) },
                    placeholder = { Text("e.g. Speak Spanish conversationally") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        focusedLabelColor = PrimaryBlue
                    ),
                    modifier = Modifier.weight(1f)
                )

                if (goals.size > 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Remove",
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onRemoveGoal(index) }
                    )
                }
            }
        }

        TextButton(
            onClick = onAddGoal,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Text("+ Add another goal", color = PrimaryBlue, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun Step2Content(
    obstacles: String,
    onObstaclesChanged: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Step 2: Know your obstacles",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "What usually stops you from achieving these goals? (e.g. 'Too tired after work', 'Don't know where to start'). The AI will use this to write triggers and min-versions.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        OutlinedTextField(
            value = obstacles,
            onValueChange = onObstaclesChanged,
            placeholder = { Text("e.g. I have a hectic work schedule and lose motivation in the evenings.") },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                focusedLabelColor = PrimaryBlue
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            maxLines = 5
        )
    }
}

@Composable
fun Step3Content(
    commitmentMinutes: String,
    onCommitmentMinutesChanged: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Step 3: Daily commitment time",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "How much total time can you reasonably dedicate to these goals each day (in minutes)?",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        OutlinedTextField(
            value = commitmentMinutes,
            onValueChange = onCommitmentMinutesChanged,
            label = { Text("Time (Minutes)") },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                focusedLabelColor = PrimaryBlue
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}
