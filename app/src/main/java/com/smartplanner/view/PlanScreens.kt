package com.smartplanner.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartplanner.controller.PlanController
import com.smartplanner.model.HabitPlan
import com.smartplanner.view.theme.DestructiveRed
import com.smartplanner.view.theme.PrimaryBlue

@Composable
fun ManagePlansScreen(
    planController: PlanController,
    onNavigateToOnboarding: () -> Unit,
    onViewPlanSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by planController.uiState.collectAsState()
    val deleteConfirmPlan by planController.deleteConfirmPlan.collectAsState()

    if (state.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryBlue)
        }
        return
    }

    // Delete Confirmation Dialog
    if (deleteConfirmPlan != null) {
        AlertDialog(
            onDismissRequest = { planController.cancelDeletePlan() },
            title = { Text("Delete Habit Plan", fontWeight = FontWeight.Bold) },
            text = { 
                Text("Are you sure you want to delete the plan for \"${deleteConfirmPlan?.goals?.firstOrNull() ?: ""}\"? This action cannot be undone.") 
            },
            confirmButton = {
                TextButton(
                    onClick = { planController.confirmDeletePlan() }
                ) {
                    Text("Delete", color = DestructiveRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { planController.cancelDeletePlan() }
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = "Manage Plans",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "View or modify plan",
                    fontSize = 13.sp,
                    maxLines = 1,
                    softWrap = false,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Button(
                onClick = onNavigateToOnboarding,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("New Custom Plan", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
            }
        }

        if (state.plans.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No plans found. Build a custom plan using the button above!",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            state.plans.forEach { plan ->
                val isActive = plan.id == state.activePlanId
                PlanItemCard(
                    plan = plan,
                    isActive = isActive,
                    onView = { 
                        planController.selectPlan(plan.id)
                        onViewPlanSuccess()
                    },
                    onDelete = { planController.requestDeletePlan(plan) }
                )
            }
        }
    }
}

@Composable
fun PlanItemCard(
    plan: HabitPlan,
    isActive: Boolean,
    onView: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 3.dp else 1.dp),
        border = if (isActive) androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryBlue)
                 else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = plan.goals.firstOrNull() ?: "General Plan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Created: ${plan.createdAt}",
                        fontSize = 12.sp,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                if (isActive) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Active",
                            color = PrimaryBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "HABITS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "${plan.habits.size} stacked",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DAILY COMMITMENT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "${plan.dailyCommitmentMinutes} min/day",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onView,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("View Dashboard", fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                }

                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DestructiveRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DestructiveRed.copy(alpha = 0.5f))
                ) {
                    Text("Delete Plan", fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                }
            }
        }
    }
}
